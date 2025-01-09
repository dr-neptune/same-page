(ns samepage.model.goal
  (:require [samepage.server.db.core :as db]
            [next.jdbc :as jdbc]
            [next.jdbc.result-set :as rs]
            [honey.sql :as sql]
            [honey.sql.helpers :as h])
  (:import (java.time Instant)))

(set! *warn-on-reflection* true)

(defn create-goal!
  "Insert a new goal row for a given user_id + title, desc, etc."
  [{:keys [user-id title description target_hours progress_hours]}]
  (let [now           (Instant/now)
        ;; If user didn't specify progress_hours, default to 0
        prog          (or progress_hours 0)
        insert-query
        (-> (h/insert-into :goals)
            (h/values [{:user_id       user-id
                        :title         title
                        :description   description
                        :target_hours  target_hours
                        :progress_hours prog
                        :created_at    now
                        :updated_at    now}])
            (sql/format))]
    (jdbc/execute! (db/datasource) insert-query
                   {:builder-fn rs/as-unqualified-lower-maps})))

(defn get-goals-for-user
  [user-id]
  (let [select-query
        (-> (h/select :id
                       :user_id
                       :title
                       :description
                       :target_hours
                       :progress_hours
                       :created_at
                       :updated_at)
            (h/from :goals)
            (h/where [:= :user_id user-id])
            (h/order-by [:created_at :desc])
            (sql/format))]
    (jdbc/execute! (db/datasource) select-query
                   {:builder-fn rs/as-unqualified-lower-maps})))

(defn get-all-goals
  []
  (let [select-query
        (-> (h/select :id
                       :user_id
                       :title
                       :description
                       :target_hours
                       :progress_hours
                       :created_at
                       :updated_at)
            (h/from :goals)
            (h/order-by [:id :asc])
            (sql/format))]
    (jdbc/execute! (db/datasource) select-query
                   {:builder-fn rs/as-unqualified-lower-maps})))

(defn get-goal-by-id
  [goal-id]
  (let [select-query
        (-> (h/select :id
                       :user_id
                       :title
                       :description
                       :target_hours
                       :progress_hours
                       :created_at
                       :updated_at)
            (h/from :goals)
            (h/where [:= :id goal-id])
            (sql/format))]
    (first (jdbc/execute! (db/datasource) select-query
                          {:builder-fn rs/as-unqualified-lower-maps}))))
