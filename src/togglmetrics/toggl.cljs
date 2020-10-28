(ns togglmetrics.toggl
  (:require [toggl-api]
            [cljs.core.async :refer [chan put! close! <! >! take!]]
            [togglmetrics.metrics :as metrics]
      )
  (:require-macros [cljs.core.async.macros :as am :refer [go]])    
  )

(def toggl-client (toggl-api. #js {:apiToken "778134d90be17f0d492dd62529d7c1f5"}))
(def default-params #js {:user_agent "alessandro.solbiati@gmail.com" :workspace_id "4782539"}) 

(defn report-functions [report key-functions]
  (map (fn [k] ((get key-functions k) report)) (keys key-functions)))

(defn report-extract-data [report key-functions]
  (zipmap (keys key-functions) (report-functions report key-functions)))

(defn detailed-report [key-functions]
  (let [out (chan)]
    (go
      (.detailedReport toggl-client default-params 
                    (fn [err report]
                      (go (if err 
                        (>! out err) 
                        (>! out (report-extract-data (js->clj report) key-functions))))))
      (<! out))))
