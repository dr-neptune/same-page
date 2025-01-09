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
  [{:keys [user-id title description target_hours]}]
  (let [now (Instant/now)
        insert-query
        (-> (h/insert-into :goals)
            (h/values [{:user_id      user-id
                        :title        title
                        :description  description
                        :target_hours target_hours
                        :created_at   now
                        :updated_at   now}])
            (sql/format))]
    (jdbc/execute! (db/datasource) insert-query
                   {:builder-fn rs/as-unqualified-lower-maps})))

(defn get-goals-for-user
  "Fetch all goals owned by user_id, newest first by created_at DESC."
  [user-id]
  (let [select-query
        (-> (h/select :id
                       :user_id
                       :title
                       :description
                       :target_hours
                       :created_at
                       :updated_at)
            (h/from :goals)
            (h/where [:= :user_id user-id])
            (h/order-by [:created_at :desc])
            (sql/format))]
    (jdbc/execute! (db/datasource) select-query
                   {:builder-fn rs/as-unqualified-lower-maps})))

;; NEW: Using HoneySQL for the admin panel to fetch every goal row
(defn get-all-goals
  "Fetch every goal row in 'goals', sorted by ID ascending, using HoneySQL."
  []
  (let [select-query
        (-> (h/select :id
                       :user_id
                       :title
                       :description
                       :target_hours
                       :created_at
                       :updated_at)
            (h/from :goals)
            (h/order-by [:id :asc])
            (sql/format))]
    (jdbc/execute! (db/datasource)
                   select-query
                   {:builder-fn rs/as-unqualified-lower-maps})))
