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

(defn render-entrypoint 
  "recursive async function that calls detailed-report with different pages"
  [req res page prev-entries]
  (go 
    (let [new-entries (<! (toggl/detailed-report page))
          entries (into prev-entries new-entries)]
      (if (> (count new-entries) 0)
        (render-entrypoint req res (+ page 1) entries)
        (.render res "index" (clj->js (toggl/report-extract-data entries report-fields)))))))

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
