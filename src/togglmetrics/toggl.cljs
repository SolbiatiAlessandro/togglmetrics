(ns togglmetrics.toggl
  (:require [toggl-api]
            [cljs.core.async :refer [chan put! close! <! >! take!]]
            [togglmetrics.metrics :as metrics]
            [togglmetrics.db :as db]
      )
  (:require-macros [cljs.core.async.macros :as am :refer [go]])    
  )

(def default-params {:user_agent "togglmetrics" :since "2020-01-01"}) 

(defn params [default-params page workspace-id]
  (clj->js (merge default-params {:page page :workspace_id workspace-id})))

(defn report-functions [entries token-data key-functions]
  "calls functions from key-functions Map on report, nil if report-data empty"
  (let [call-report-function (fn [k] (if (seq entries) ((get key-functions k) entries token-data) nil))]
  (map call-report-function (keys key-functions))))

(defn report-extract-data [entries token-data key-functions]
    (zipmap (keys key-functions) (report-functions entries token-data key-functions)))

(defn detailed-report [token page]
  (let [out (chan)
        toggl-client (toggl-api. #js {:apiToken token})
        workspace-id (db/load-token-value token "workspace-id")]
    (js/console.log (str "detailed-report querying page " page))
    (go
        (.detailedReport toggl-client (params default-params page workspace-id)
                    (fn [err report]
                      (go 
                        (js/console.log err)
                        (js/console.log report)
                        (if err 
                        (>! out err) 
                        (>! out (metrics/report-data (js->clj report)))))))
      (<! out))))
