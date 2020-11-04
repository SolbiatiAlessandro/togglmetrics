(ns togglmetrics.toggl
  (:require [toggl-api]
            [cljs.core.async :refer [chan put! close! <! >! take!]]
            [togglmetrics.metrics :as metrics]
      )
  (:require-macros [cljs.core.async.macros :as am :refer [go]])    
  )

(def toggl-client (toggl-api. #js {:apiToken "778134d90be17f0d492dd62529d7c1f5"}))
(def default-params {:user_agent "alessandro.solbiati@gmail.com" :workspace_id "4782539" :since "2020-01-01"}) 

(defn params [default-params page]
  (let [p (merge default-params {:page page})
        js-p (clj->js p)]
    (js/console.log js-p)
    js-p))

(defn report-functions [entries key-functions]
  "calls functions from key-functions Map on report, nil if report-data empty"
  (let [call-report-function (fn [k] (if (seq entries) ((get key-functions k) entries) nil))]
  (map call-report-function (keys key-functions))))

(defn report-extract-data [entries key-functions]
    (zipmap (keys key-functions) (report-functions entries key-functions)))

(defn detailed-report [page]
  (let [out (chan)]
    (js/console.log (str "detailed-report querying page " page))
    (go
        (.detailedReport toggl-client (params default-params page)
                    (fn [err report]
                      (go 
                        (js/console.log err)
                        (js/console.log report)
                        (if err 
                        (>! out err) 
                        (>! out (metrics/report-data (js->clj report)))))))
      (<! out))))
