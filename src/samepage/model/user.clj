(ns samepage.model.user
  (:require [samepage.server.db.core :as db]
            [next.jdbc :as jdbc]
            [next.jdbc.result-set :as rs]
            [buddy.hashers :as hashers]
            [honey.sql :as sql]
            [honey.sql.helpers :as h]))


(defn create-user!
  "Insert a user row with :role = 'admin' by default, hashing the password."
  [{:keys [name email password] :as user-map}]
  ;; 1) Hash the plaintext password
  (let [hashed-pw  (hashers/derive password)
        user-map-2 (assoc user-map
                          :role "admin"
                          :password hashed-pw)
        insert-query (-> (h/insert-into :users)
                         (h/values [user-map-2])
                         (sql/format))
        result (jdbc/execute! (db/datasource)
                              insert-query
                              {:return-generated-keys true
                               :builder-fn rs/as-unqualified-lower-maps})
        generated-id (:id (first result))]
    (assoc user-map-2 :id generated-id)))

;; For logging in, we do NOT pass the raw password check in the WHERE clause,
;; because it is hashed. Instead, we fetch by email, then verify with buddy.
(defn find-by-email
  "Fetch user by email or nil if not found."
  [email]
  (first
   (jdbc/execute! (db/datasource)
                  ["SELECT * FROM users WHERE email = ?" email]
                  {:builder-fn rs/as-unqualified-lower-maps})))

(defn find-by-email-and-check
  "Fetch user by email, then compare hashed password using buddy."
  [email plaintext-pw]
  (when-let [row (find-by-email email)]
    (if (hashers/check plaintext-pw (:password row))
      row
      nil)))

(defn find-by-name-or-email
  "Returns the user row if name OR email matches."
  [name email]
  (first
   (jdbc/execute! (db/datasource)
                  ["SELECT * FROM users WHERE name = ? OR email = ?" name email]
                  {:builder-fn rs/as-unqualified-lower-maps})))

(defn get-all-users
  []
  (jdbc/execute! (db/datasource)
                 ["SELECT id, name, email, role, created_at, updated_at FROM users ORDER BY id"]
                 {:builder-fn rs/as-unqualified-lower-maps}))
