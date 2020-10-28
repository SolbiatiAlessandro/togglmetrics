(ns togglmetrics.metrics)

(defn report-length [report]
  (let [data (get report "data")]
    (count data)))
