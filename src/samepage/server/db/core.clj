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

(defn create-users-table! []
  (let [create-users
        {:create-table [:users :if-not-exists]
         :with-columns
         [[:id :identity :primary-key]
          [:name          [:varchar 255] :not-null]
          [:display_name  [:varchar 255] :not-null [:raw "DEFAULT ''"]]
          [:email         [:varchar 255] :not-null]
          [:password      [:varchar 255] :not-null]
          [:role          [:varchar 50]  :not-null [:raw "DEFAULT 'admin'"]]
          ;; NEW column:
          [:profile_pic   [:varchar 255] :not-null [:raw "DEFAULT ''"]]

          [:created_at    :timestamp :not-null [:raw "DEFAULT CURRENT_TIMESTAMP"]]
          [:updated_at    :timestamp :not-null [:raw "DEFAULT CURRENT_TIMESTAMP"]]]}
        [sql-str & params] (sql/format create-users)]
    (jdbc/execute! ds (into [sql-str] params))))

(defn create-notes-table! []
  (let [create-notes
        {:create-table [:notes :if-not-exists]
         :with-columns
         [[:id :identity :primary-key]
          [:user_name  [:varchar 255]]
          [:text       [:varchar 4000] :not-null]
          [:created_at :timestamp      :not-null [:raw "DEFAULT CURRENT_TIMESTAMP"]]]}
        [sql-str & params] (sql/format create-notes)]
    (jdbc/execute! ds (into [sql-str] params))))

(defn create-goals-table! []
  (let [create-goals
        {:create-table [:goals :if-not-exists]
         :with-columns
         [[:id :identity :primary-key]
          [:user_id        :bigint]
          [:title          [:varchar 255] :not-null]
          [:description    :text]
          [:target_hours   :int]
          [:progress_hours :int :not-null [:raw "DEFAULT 0"]]
          [:created_at     :timestamp :not-null [:raw "DEFAULT CURRENT_TIMESTAMP"]]
          [:updated_at     :timestamp :not-null [:raw "DEFAULT CURRENT_TIMESTAMP"]]]}
        [sql-str & params] (sql/format create-goals)]
    (jdbc/execute! ds (into [sql-str] params))))

(defn create-practice-logs-table! []
  (let [create-plogs
        {:create-table [:practice_logs :if-not-exists]
         :with-columns
         [[:id :identity :primary-key]
          [:goal_id       :bigint :not-null]
          [:duration      :int    :not-null]
          [:notes         :text]
          [:practice_date :timestamp :not-null]
          [:created_at    :timestamp :not-null [:raw "DEFAULT CURRENT_TIMESTAMP"]]
          [:updated_at    :timestamp :not-null [:raw "DEFAULT CURRENT_TIMESTAMP"]]]}
        [sql-str & params] (sql/format create-plogs)]
    (jdbc/execute! ds (into [sql-str] params))))

(defn create-schema! []
  (create-users-table!)
  (create-notes-table!)
  (create-goals-table!)
  (create-practice-logs-table!))
