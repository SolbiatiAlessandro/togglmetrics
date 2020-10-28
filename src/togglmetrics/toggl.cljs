(ns togglmetrics.toggl
  (:require [toggl-api]
            [cljs.core.async :refer [chan put! close! <! >! take!]]
            [togglmetrics.metrics :as metrics]
      )
  (:require-macros [cljs.core.async.macros :as am :refer [go]])    
  )

(def toggl-client (toggl-api. #js {:apiToken "778134d90be17f0d492dd62529d7c1f5"}))
(def default-params #js {:user_agent "alessandro.solbiati@gmail.com" :workspace_id "4782539"}) 

(defn detailed-report [report-function]
  (let [out (chan)]
    (go
      (.detailedReport toggl-client default-params 
                    (fn [err report]
                      (go (if err 
                        (>! out err) 
                        (>! out (report-function (js->clj report)))))))
      (<! out))))
