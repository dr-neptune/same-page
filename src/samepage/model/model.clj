(ns samepage.model.model
  (:require [samepage.server.db.core :as db]
            [next.jdbc :as jdbc]
            [next.jdbc.result-set :as rs]
            [honey.sql :as sql]
            [honey.sql.helpers :as h])
  (:import (java.time Instant)))

(set! *warn-on-reflection* true)

(defn create-note!
  "Add a new note row to H2 with the given user name + text."
  [user-name note-text]
  (let [honey-query
        (-> (h/insert-into :notes)
            (h/values [{:user_name  user-name
                        :text       note-text
                        ;; We also store Instant.now() here,
                        ;; but your table has a default too—either approach is fine.
                        :created_at (Instant/now)}])
            ;; Convert data DSL -> SQL string + params
            (sql/format))]
    (jdbc/execute! (db/datasource)
                   honey-query
                   ;; builder-fn => convert column keys to unqualified, lower keys
                   {:builder-fn rs/as-unqualified-lower-maps})))

(defn get-notes-for-user
  [user-name]
  (let [honey-query
        (-> (h/select :id
                       :user_name
                       :text
                       ;; We'll alias 'created_at' as "timestamp"
                       ;; by writing: [[:raw "created_at"] :timestamp]
                       [[:raw "created_at"] :timestamp])
            (h/from :notes)
            (h/where [:= :user_name user-name])
            (h/order-by [:created_at :desc])
            (sql/format))]
    (jdbc/execute! (db/datasource)
                   honey-query
                   {:builder-fn rs/as-unqualified-lower-maps
                    :return-lob-strings? true})))
