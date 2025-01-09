(ns samepage.server.db.core
  (:require [next.jdbc :as jdbc]
            [honey.sql :as sql]
            [honey.sql.helpers :as h]))

(defonce ^:private ds
  (jdbc/get-datasource
   {:dbtype "h2"
    :dbname "./db/samepage;DB_CLOSE_DELAY=-1"
    :user   "sa"
    :password ""}))

(defn datasource []
  ds)

(defn create-schema!
  []
  ;; 1) Create users if not exists
  (let [create-users
        {:create-table [:users :if-not-exists]
         :with-columns
         [[:id :identity :primary-key]
          [:name     [:varchar 255] :not-null]
          [:email    [:varchar 255] :not-null]
          [:password [:varchar 255] :not-null]
          [:role     [:varchar 50] :not-null [:raw "DEFAULT 'admin'"]] ;; new role column
          [:created_at  :timestamp :not-null [:raw "DEFAULT CURRENT_TIMESTAMP"]]
          [:updated_at  :timestamp :not-null [:raw "DEFAULT CURRENT_TIMESTAMP"]]]}
        [u-sql & u-params] (sql/format create-users)]
    (jdbc/execute! ds (into [u-sql] u-params)))

  ;; 2) Create notes if not exists
  (let [create-notes
        {:create-table [:notes :if-not-exists]
         :with-columns
         [[:id :identity :primary-key]
          [:user_name [:varchar 255]]
          [:text [:varchar 4000] :not-null]
          [:created_at :timestamp :not-null
           [:raw "DEFAULT CURRENT_TIMESTAMP"]]]}
        [n-sql & n-params] (sql/format create-notes)]
    (jdbc/execute! ds (into [n-sql] n-params)))

  
  ;; 3) Create goals table if not exists
  (let [create-goals
        {:create-table [:goals :if-not-exists]
         :with-columns
         [[:id :identity :primary-key]
          [:user_id :bigint]   ;; references users.id
          [:title [:varchar 255] :not-null]
          [:description :text]
          [:target_hours :int]
          ;; NEW: progress_hours, default 0
          [:progress_hours :int :not-null [:raw "DEFAULT 0"]]
          [:created_at :timestamp :not-null [:raw "DEFAULT CURRENT_TIMESTAMP"]]
          [:updated_at :timestamp :not-null [:raw "DEFAULT CURRENT_TIMESTAMP"]]]}
        [g-sql & g-params] (sql/format create-goals)]
    (jdbc/execute! ds (into [g-sql] g-params))))
