(ns samepage.model.user
  (:require [samepage.server.db.core :as db]
            [next.jdbc :as jdbc]
            [next.jdbc.result-set :as rs]
            [buddy.hashers :as hashers]
            [honey.sql :as sql]
            [honey.sql.helpers :as h])
  (:import (java.time Instant)))

(defn create-user!
  "Insert a user row, assigning role = 'admin' if email is old@rottenhat,
   otherwise role = 'user'.
   Accepts `:name`, `:display_name`, `:email`, `:password`."
  [{:keys [name display_name email password] :as user-map}]
  (let [hashed-pw      (hashers/derive password)
        final-display  (if (clojure.string/blank? display_name)
                         name
                         display_name)
        role           (if (= "old@rottenhat" email)
                         "admin"
                         "user")  ;; Everyone else is user
        row-to-insert  (-> user-map
                           (assoc :password hashed-pw)
                           (assoc :display_name final-display)
                           (assoc :role role))]

    ;; We'll do the two-step approach: Insert, then SELECT by email to get the :id
    (jdbc/execute!
     (db/datasource)
     ["INSERT INTO users (name, display_name, email, password, role)
       VALUES (?, ?, ?, ?, ?)"
      name final-display email hashed-pw role]
     {:builder-fn rs/as-unqualified-lower-maps})

    ;; Now SELECT the newly inserted row by email => returns the full user record
    (first
     (jdbc/execute!
      (db/datasource)
      ["SELECT * FROM users WHERE email = ?" email]
      {:builder-fn rs/as-unqualified-lower-maps}))))

(defn find-by-email
  [email]
  (first
   (jdbc/execute!
    (db/datasource)
    ["SELECT * FROM users WHERE email = ?" email]
    {:builder-fn rs/as-unqualified-lower-maps})))

(defn find-by-email-and-check
  [email plaintext-pw]
  (when-let [row (find-by-email email)]
    (if (hashers/check plaintext-pw (:password row))
      row
      nil)))

(defn find-by-name-or-email
  "Returns the user row if name OR email matches."
  [name email]
  (first
   (jdbc/execute!
    (db/datasource)
    ["SELECT * FROM users WHERE name = ? OR email = ?" name email]
    {:builder-fn rs/as-unqualified-lower-maps})))

(defn get-all-users
  []
  (jdbc/execute!
   (db/datasource)
   ["SELECT id, name, display_name, email, role, created_at, updated_at
     FROM users ORDER BY id"]
   {:builder-fn rs/as-unqualified-lower-maps}))

(defn update-user!
  "Update the user row in DB with `fields`, returning the new row."
  [user-id fields]
  (let [now (Instant/now)
        update-stmt
        (-> (h/update :users)
            (h/set (assoc fields :updated_at now))
            (h/where [:= :id user-id])
            (sql/format))]
    (jdbc/execute! (db/datasource) update-stmt
                   {:builder-fn rs/as-unqualified-lower-maps})
    ;; fetch updated
    (first
     (jdbc/execute!
      (db/datasource)
      ["SELECT * FROM users WHERE id = ?" user-id]
      {:builder-fn rs/as-unqualified-lower-maps}))))

(defn get-user-by-id
  [id]
  (first
   (jdbc/execute!
    (db/datasource)
    ["SELECT * FROM users WHERE id = ?" id]
    {:builder-fn rs/as-unqualified-lower-maps})))
