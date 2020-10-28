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

(defn home [req res]
  (go 
    (let [clj-args (<! (toggl/detailed-report 
                         {:length metrics/report-length
                          :unique_projects metrics/unique-projects
                          :duration_bar_chart metrics/bar-chart}))
          args (clj->js clj-args)]
    (js/console.log args)
    (.render res "index" args))))

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
    (.get app "/" home)
    (.listen app 3000 (fn [] (println "Example app listening on port 3000!")))))

(defn start! []
  (reset! server (start-server)))

(defn restart! []
  (.close @server start!))

(set! *main-cli-fn* start!)
