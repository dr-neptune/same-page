(ns samepage.model.model
    (:require [samepage.server.db.core :as db]
              [next.jdbc :as jdbc]
              [next.jdbc.result-set :as rs]
              [next.jdbc.sql :as sql])
    (:import (java.time Instant)))

(set! *warn-on-reflection* true)

(defn create-note!
      "Add a new note row to H2 with the given user name + text."
      [user-name note-text]
      (sql/insert! (db/datasource)
                   :notes
                   {:user_name  user-name
                    :text       note-text
                    :created_at (Instant/now)}))

(defn get-notes-for-user
      [user-name]
      (jdbc/execute! (db/datasource)
                     ["SELECT id, user_name, text, created_at AS timestamp
      FROM notes
      WHERE user_name = ?
      ORDER BY created_at DESC"
                      user-name]
                     {:builder-fn rs/as-unqualified-lower-maps
                      :return-lob-strings? true}))
