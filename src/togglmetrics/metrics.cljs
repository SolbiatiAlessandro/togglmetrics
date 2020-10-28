(ns togglmetrics.metrics
  (:require [cljs-time.core :as c]
            [cljs-time.format :as f]
            [cljs-time.coerce :as cc]
            [clojure.string :as s]
            ))

(defn report-data [report] (get report "data"))

(defn report-length [report] (count (report-data report)))

(defn unique-field [report field]
  (set (map (fn [entry] (get entry field)) (report-data report))))

(defn unique-projects [report]
  (unique-field report "project"))

(defn parse-datetime [string]
  ;; string = "20140401T145700-08:30"
  (let [ 
        string (s/replace string "-" "")
        string (s/replace string ":" "")
        ]
  (f/parse (:basic-date-time-no-ms f/formatters) string)))

(defn local-date [string]
  ;; string = "20140401T145700-08:30" -> "2014-03-31T23:00:00.000Z"
  (cc/to-string (cc/to-local-date (parse-datetime string))))

(def categories ["paid-work" "unpaid-work" "relax"])
(def categories-color ["red", "blue", "green"])
(def project-category 
  {"productivity" 1 
   "facebook" 0 
   "videogames" 2 
   "steamengine" 1})


(defn duration-for-category-date [category date entries]
  (reduce + (map (fn [entry] (get entry "dur")) 
          (filter (fn [entry] (and 
                                (= (get project-category (get entry "project")) category)
                                (= (local-date (get entry "start")) date))
                    ) entries))))

(defn bar-chart-category [category dates entries]
  (clj->js {:label (get categories category)
   :backgroundColor (get categories-color category)
   :data (clj->js (vec  (map (fn [date] (duration-for-category-date category date entries)) dates)))
   }))

(defn bar-chart [report]
  (let [entries (report-data report) 
        labels (set (map (fn [entry] (local-date (get entry "start"))) entries)) 
        datasets (clj->js  (vec (map (fn [category] (bar-chart-category category labels entries)) (range (count categories )))))     
        ]
  {:labels labels
   :datasets (clj->js datasets)}))

