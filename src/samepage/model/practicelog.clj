(ns samepage.model.practicelog
  (:require [samepage.server.db.core :as db]
            [next.jdbc :as jdbc]
            [next.jdbc.result-set :as rs]
            [honey.sql :as sql]
            [honey.sql.helpers :as h])
  (:import (java.time Instant)))

(set! *warn-on-reflection* true)

(defn create-practice-log!
  "Insert a practice log row for a given goal, with duration, optional notes, etc."
  [{:keys [goal-id duration notes practice-date]}]
  (let [now (Instant/now)
        insert-stmt
        (-> (h/insert-into :practice_logs)
            (h/values [{:goal_id       goal-id
                        :duration      duration
                        :notes         notes
                        :practice_date practice-date
                        :created_at    now
                        :updated_at    now}])
            (sql/format))]
    (jdbc/execute! (db/datasource) insert-stmt
                   {:builder-fn rs/as-unqualified-lower-maps})))

(defn get-practice-logs-for-goal
  "Fetch all practice logs for a given goal, newest first by practice_date."
  [goal-id]
  (let [select-stmt
        (-> (h/select :id
                       :goal_id
                       :duration
                       :notes
                       :practice_date
                       :created_at
                       :updated_at)
            (h/from :practice_logs)
            (h/where [:= :goal_id goal-id])
            (h/order-by [:practice_date :desc])
            (sql/format))]
    (jdbc/execute! (db/datasource)
                   select-stmt
                   {:builder-fn rs/as-unqualified-lower-maps})))

(defn get-total-duration-for-goal [goal-id]
  (let [query (-> (h/select [[:raw "COALESCE(SUM(duration), 0) AS total"]])
                  (h/from :practice_logs)
                  (h/where [:= :goal_id goal-id])
                  (sql/format))
        rows  (jdbc/execute! (db/datasource) query
                             {:builder-fn rs/as-unqualified-lower-maps})]
    (or (:total (first rows)) 0)))

(defn get-latest-5-logs-for-goal
  "Return up to 5 most recent practice logs (descending by practice_date)."
  [goal-id]
  (let [query (-> (h/select :id :duration :notes :practice_date :created_at :updated_at)
                  (h/from :practice_logs)
                  (h/where [:= :goal_id goal-id])
                  (h/order-by [:practice_date :desc])
                  (h/limit 5)
                  (sql/format))]
    (jdbc/execute! (db/datasource) query
                   {:builder-fn rs/as-unqualified-lower-maps})))

(defn get-all-practice-logs
  "Fetch all practice logs across all goals, with the goal_id, created_at, etc."
  []
  (let [query "SELECT id, goal_id, duration, notes, practice_date, created_at
               FROM practice_logs
               ORDER BY created_at DESC"]
    (jdbc/execute! (db/datasource) [query]
                   {:builder-fn rs/as-unqualified-lower-maps})))
