(ns togglmetrics.core
  (:require ["express"]
            ["path"]
            [clojure.string :as string]
            [togglmetrics.toggl :as toggl]
            [cljs.core.async :refer [chan put! close! <! >! take!]]
            [togglmetrics.metrics :as metrics]
            )
  (:require-macros [cljs.core.async.macros :as am :refer [go]])    
  )

(enable-console-print!)

(set! *warn-on-infer* true)

(defonce server (atom nil))

(def report-fields
  "which are the report fields to be computed for rendering"
  {:length metrics/report-length
  :unique_projects metrics/unique-projects
  :duration_bar_chart metrics/bar-chart
  :unique_dates metrics/unique-dates
  ;; :debug metrics/report-data
  })

(def report-fields-join
  "function to be called for joining two fields from different pages"
  {:length +
  :unique_projects first
  :duration_bar_chart first
  :unique_dates first
  ;; :debug metrics/report-data
  })

(defn join-args [prev-args args]
  (if prev-args
    (map (fn [k] ((get report-fields-join k) (get args k) (get prev-args k))) (keys args))
    args
   ))

(defn render-entrypoint 
  "recursive async function that calls detailed-report with different pages"
  [req res page prev-args]
  (go 
    (let [args (<! (toggl/detailed-report report-fields page))
          args (join-args prev-args args)]
      (js/console.log page)
      (if (> (:length, args) 0)
        (render-entrypoint req res (+ page 1) args)
        (.render res "index" (clj->js args))))))

(defn render-entrypoint-wrapper [req res] (render-entrypoint req res 1 nil))

(defn express-app []
 (let [app (express)]

   ;; ../../ is because this file gets compiled in /dev/togglmetrics
   ;; but views is at /views. Check project.clj ouput-to for more info.
   (.set app "views" (.join path js/__dirname "../../views"))
   (.set app "view engine" "pug")
   app
   ))

(defn start-server []
  (println "Starting server")
  (let [app (express-app)]
    (.get app "/" render-entrypoint-wrapper)
    (.listen app 3000 (fn [] (println "Example app listening on port 3000!")))))

(defn start! []
  (reset! server (start-server)))

(defn restart! []
  (.close @server start!))

(set! *main-cli-fn* start!)
