(ns togglmetrics.core
  (:require ["express"]
            ["path"]
            ["body-parser"]
            [clojure.string :as string]
            [togglmetrics.toggl :as toggl]
            [togglmetrics.db :as db]
            [cljs.core.async :refer [chan put! close! <! >! take!]]
            [togglmetrics.metrics :as metrics]
            )
  (:require-macros [cljs.core.async.macros :as am :refer [go]])    
  )

(enable-console-print!)

(set! *warn-on-infer* true)

(defonce server (atom nil))

(def report-fields
  "which are the report fields to be computed for rendering, this function will
  be called with `entries` as argument from report data"
  {:length metrics/report-length
  :unique_projects metrics/unique-projects
  :duration_bar_chart metrics/bar-chart
  :unique_dates metrics/unique-dates
  :debug metrics/report-debug
  :token_data (fn [_ token-data] token-data)
  :categories (fn [_ _] metrics/categories)
  :categories_color (fn [_ _] metrics/categories-color)
  })

(defn render-entrypoint 
  "recursive async function that calls detailed-report with different pages"
  [token req res page prev-entries]
  (go 
    (let [queried-entries (<! (toggl/detailed-report token page))] 
      (if (seqable? queried-entries)
        ;; query was successful
        (let [new-entries queried-entries
            entries (into prev-entries new-entries)]
            (if (> (count new-entries) 0)
              (render-entrypoint token req res (+ page 1) entries)
              (.render res "index" (clj->js (toggl/report-extract-data entries (db/load-token token) report-fields)))))
        ;; query failed
        (.render res "error" (clj->js {:error queried-entries}))))))

(defn render-entrypoint-wrapper-get [req res] 
  "/?token=XXX for index.pug, / for landing.pug"
  (if-let [token (get (js->clj (.-query req)) "token")]
    (render-entrypoint token req res 1 nil) 
    (.render res "landing")))

(defn render-entrypoint-wrapper-post [req res] 
  "entrypoint post means user is sending new config data that need to be updated
  
  req.body -> {'toggl-api-key': '123',
  'workspace-id': '124',
  project_1 'undefined',
  project_2 category_2,
  ...}"
  (let [data (js->clj (.-body req))
        token (get data "toggl-api-key")] 
    (db/save-token-data token data)
    (.redirect res (str "/?token=" token))))

(defn express-app []
 (let [app (express)]

   ;; ../../ is because this file gets compiled in /dev/togglmetrics
   ;; but views is at /views. Check project.clj ouput-to for more info.
   (.set app "views" (.join path js/__dirname "../../views"))
   (.set app "view engine" "pug")
   (.use app (.json body_parser))
   (.use app (.urlencoded body_parser (clj->js {:extended true})))
   app
   ))

(defn start-server []
  (println "Starting server")
  (let [app (express-app)]
    (.get app "/" render-entrypoint-wrapper-get)
    (.post app "/" render-entrypoint-wrapper-post)
    (.listen app 3000 (fn [] (println "Example app listening on port 3000!")))))

(defn start! []
  (reset! server (start-server)))

(defn restart! []
  (.close @server start!))

(set! *main-cli-fn* start!)
