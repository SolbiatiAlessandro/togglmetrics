(ns togglmetrics.metrics
  "exports stateless functions that accept `entries, token-data`and return aggregated functions"
  (:require [cljs-time.core :as c]
            [cljs-time.format :as f]
            [cljs-time.coerce :as cc]
            [cljs-time.periodic :as p]
            [clojure.string :as s]
            [togglmetrics.db :as db]
            ))

(def token (atom "default-token"))
(def categories ["paid-work" "unpaid-work" "relax"])
(def categories-color ["red", "blue", "green"])

(defn report-data [report] (get report "data"))

(defn report-debug [entries _] 
  (map (fn [entry] 
         (str "\n" (get entry "project") " " (get entry "start") " " (get entry "end")))
       entries))

(defn report-length [entries _] (count entries))

(defn unique-field [entries field]
  (set (map (fn [entry] (get entry field)) entries)))

(defn unique-projects [entries _]
  (remove nil? (unique-field entries "project")))

(defn parse-datetime [string]
  ;; string = "20140401T145700-08:30"
  (let [ 
        string (s/replace string "-" "")
        string (s/replace string ":" "")
        ]
  (f/parse (:basic-date-time-no-ms f/formatters) string)))

(defn local-date [string]
  ;; string = "20140401T145700-08:30" -> "2014-03-31T23:00:00.000Z"
  (cc/to-local-date (parse-datetime string)) )

(defn local-date-string [string]
  (cc/to-string (local-date string)))

(defn unique-dates [entries _]
  (set (map (fn [entry] (local-date-string (get entry "start"))) entries)) )


(defn ms-to-h [ms] (/ (.round js.Math (* (/ ms (* 1000 60 60)) 100)) 100))

(defn duration-for-category-date [category date entries project-category]
  (reduce + (map (fn [entry] (ms-to-h (get entry "dur"))) 
          (filter (fn [entry] (and 
                                (= (get project-category (get entry "project")) (str category))
                                (= (local-date-string (get entry "start")) date))
                    ) entries))))

(defn bar-chart-category [category dates entries project-category]
  "category is an int (from the enum) 0, 1, 2"
  (clj->js {:label (get categories category)
   :backgroundColor (get categories-color category)
   :data (clj->js (vec  (map (fn [date] (duration-for-category-date category date entries project-category)) dates)))
   }))

(defn bar-chart-labels [entries]
  "labels are dates with time on midnight"
  (let [unique-labels (set (map (fn [entry] (local-date (get entry "start"))) entries))
       first-label (apply min unique-labels)
       last-label (apply max unique-labels)
       interval (c/days 1)
       labels-date-exclusive (p/periodic-seq first-label last-label interval)
       labels-date (into (vector last-label) labels-date-exclusive)    
       ]
    (sort (map cc/to-string labels-date))))

(defn bar-chart [entries project-category]
  (let [labels (bar-chart-labels entries)
        datasets (vec (map (fn [category] (bar-chart-category category labels entries project-category)) (range (count categories ))))]
  {:labels labels
   :datasets datasets}))
