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

;; For the admin panel, let's add helper queries:
(defn get-all-users
  "Fetch all rows from users table."
  []
  (jdbc/execute! (db/datasource)
                 ["SELECT id, name, email, role, created_at, updated_at FROM users ORDER BY id"]
                 {:builder-fn rs/as-unqualified-lower-maps}))
