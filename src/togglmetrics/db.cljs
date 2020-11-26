(ns togglmetrics.db "module to save and load configuration from users as JSON files"
  (:require [fs])
  )

(def PROD_DB_PATH "/var/www/togglmetrics/db/tokens.json")
(def DEV_DB_PATH "./db/tokens.json")
(def DB_PATH DEV_DB_PATH)

(defn load-db []
  (js->clj (.parse js/JSON (.readFileSync fs DB_PATH))))

(defn load-token [token]
  (get (load-db) token))

(defn load-token-value [token value]
  (get (load-token token) value))

(defn save-token-data [token payload]
  (let [updated-db (assoc (load-db) token 
                          ;; "If a key occurs in more than one map, the mapping from
                          ;; the latter (left-to-right) will be the mapping in the result."
                          (merge (load-token token) payload) 
                          )]
    (.writeFileSync fs DB_PATH (.stringify js/JSON (clj->js updated-db)))))
