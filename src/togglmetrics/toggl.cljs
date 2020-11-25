(ns togglmetrics.toggl
  (:require [toggl-api]
            [cljs.core.async :refer [chan put! close! <! >! take!]]
            [togglmetrics.metrics :as metrics]
      )
  (:require-macros [cljs.core.async.macros :as am :refer [go]])    
  )

(def default-params {:user_agent "togglmetrics" :since "2020-01-01"}) 

(defn params [default-params page workspace_id]
  (let [p (merge default-params {:page page :workspace_id workspace_id})
        js-p (clj->js p)]
    (js/console.log js-p)
    js-p))

(defn report-functions [entries key-functions]
  "calls functions from key-functions Map on report, nil if report-data empty"
  (let [call-report-function (fn [k] (if (seq entries) ((get key-functions k) entries) nil))]
  (map call-report-function (keys key-functions))))

(defn report-extract-data [entries key-functions]
    (zipmap (keys key-functions) (report-functions entries key-functions)))

(defn detailed-report [token page]
  (let [out (chan)
        toggl-client (toggl-api. #js {:apiToken token})
        workspace_id "4782539"]
    (js/console.log (str "detailed-report querying page " page))
    (go
        (.detailedReport toggl-client (params default-params page workspace_id)
                    (fn [err report]
                      (go 
                        (js/console.log err)
                        (js/console.log report)
                        (if err 
                        (>! out err) 
                        (>! out (metrics/report-data (js->clj report)))))))
      (<! out))))
