(ns togglmetrics.db "module to save and load configuration from users as JSON files"
  (:require [fs])
  )

(def DB_PATH "./db/tokens.json")

(defn load-db []
  (js->clj (.parse js/JSON (.readFileSync fs DB_PATH))))

(defn load-token [token]
  (get (load-db) token))

(defn load-token-value [token value]
  (get (load-token token) value))

(defn save-token-data [token payload]
  (let [updated-db (assoc (load-db) token payload)]
    (.writeFileSync fs DB_PATH (.stringify js/JSON (clj->js updated-db)))))
