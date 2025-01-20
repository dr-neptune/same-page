(ns samepage.model.goal
  (:require [samepage.server.db.core :as db]
            [next.jdbc :as jdbc]
            [next.jdbc.result-set :as rs]
            [honey.sql :as sql]
            [honey.sql.helpers :as h])
  (:import (java.time Instant)))

(set! *warn-on-reflection* true)

(defn create-goal!
  "Insert a new goal row with user_id, title, icon, etc."
  [{:keys [user-id title description target_hours progress_hours icon]}]
  (let [now (Instant/now)
        prog (or progress_hours 0)
        insert-query
        (-> (h/insert-into :goals)
            (h/values [{:user_id        user-id
                        :title          title
                        :description    description
                        :target_hours   target_hours
                        :progress_hours prog
                        :icon           (or icon "")
                        :created_at     now
                        :updated_at     now}])
            (sql/format))]
    (jdbc/execute! (db/datasource) insert-query
                   {:builder-fn rs/as-unqualified-lower-maps})))

(defn get-goals-for-user
  "Fetch all goals for a specific user (descending by created_at).
   NOTE: Must include :icon so the UI can render the icon."
  [user-id]
  (let [select-query
        (-> (h/select :id
                       :user_id
                       :title
                       :description
                       :target_hours
                       :progress_hours
                       :icon                 ;; <-- Added
                       :created_at
                       :updated_at)
            (h/from :goals)
            (h/where [:= :user_id user-id])
            (h/order-by [:created_at :desc])
            (sql/format))]
    (jdbc/execute! (db/datasource) select-query
                   {:builder-fn rs/as-unqualified-lower-maps})))

(defn get-all-goals
  "Fetch all goals (for admin). Also include :icon."
  []
  (let [select-query
        (-> (h/select :id
                       :user_id
                       :title
                       :description
                       :target_hours
                       :progress_hours
                       :icon              ;; <-- Added
                       :created_at
                       :updated_at)
            (h/from :goals)
            (h/order-by [:id :asc])
            (sql/format))]
    (jdbc/execute! (db/datasource) select-query
                   {:builder-fn rs/as-unqualified-lower-maps})))

(defn get-goal-by-id
  "Load a single goal by ID, including :icon."
  [goal-id]
  (let [select-query
        (-> (h/select :id
                       :user_id
                       :title
                       :description
                       :target_hours
                       :progress_hours
                       :icon             ;; <-- Added
                       :created_at
                       :updated_at)
            (h/from :goals)
            (h/where [:= :id goal-id])
            (sql/format))]
    (first (jdbc/execute! (db/datasource) select-query
                          {:builder-fn rs/as-unqualified-lower-maps}))))

(defn update-goal!
  "Update an existing goal by ID. Supply a map of updated fields (title, description, icon, etc.)."
  [goal-id updates]
  (let [now (Instant/now)
        update-stmt
        (-> (h/update :goals)
            (h/set (assoc updates :updated_at now))
            (h/where [:= :id goal-id])
            (sql/format))]
    (jdbc/execute! (db/datasource) update-stmt
                   {:builder-fn rs/as-unqualified-lower-maps})))

(defn delete-goal!
  "Delete the goal with the given `goal-id`."
  [goal-id]
  (let [delete-query
        (-> (h/delete-from :goals)
            (h/where [:= :id goal-id])
            (sql/format))]
    (jdbc/execute! (db/datasource) delete-query)))
