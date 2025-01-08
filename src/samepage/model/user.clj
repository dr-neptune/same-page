(ns samepage.model.user
  (:require [samepage.server.db.core :as db]
            [next.jdbc :as jdbc]
            [next.jdbc.result-set :as rs]
            [honey.sql :as sql]
            [honey.sql.helpers :as h]))

(defn create-user!
  "Insert a user row with :role = 'admin' by default."
  [{:keys [name email password] :as user-map}]
  (let [user-map-2 (assoc user-map :role "admin")
        insert-query (-> (h/insert-into :users)
                         (h/values [user-map-2])
                         (sql/format))
        result (jdbc/execute! (db/datasource)
                              insert-query
                              {:return-generated-keys true
                               :builder-fn rs/as-unqualified-lower-maps})
        generated-id (:id (first result))]
    (assoc user-map-2 :id generated-id)))

(defn get-all-users []
  (jdbc/execute! (db/datasource)
                 ["SELECT id, name, email, role, created_at, updated_at FROM users ORDER BY id"]
                 {:builder-fn rs/as-unqualified-lower-maps}))

;; NEW 1: Check if user with same name OR email already exists
(defn find-by-name-or-email
  "Returns the user row if name OR email matches, else nil."
  [name email]
  (first
   (jdbc/execute! (db/datasource)
                  ["SELECT * FROM users WHERE name = ? OR email = ?" name email]
                  {:builder-fn rs/as-unqualified-lower-maps})))

;; NEW 2: Check if user’s email/password match an existing user
;; In a real app, you'd hash passwords and compare hashed values.
(defn find-by-email-and-password
  [email password]
  (first
   (jdbc/execute! (db/datasource)
                  ["SELECT * FROM users WHERE email = ? AND password = ?" email password]
                  {:builder-fn rs/as-unqualified-lower-maps})))
