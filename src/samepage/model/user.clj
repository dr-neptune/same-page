(ns samepage.model.user
  (:require [samepage.server.db.core :as db]
            [next.jdbc :as jdbc]
            [next.jdbc.result-set :as rs]
            [honey.sql :as sql]
            [honey.sql.helpers :as h]))

(defn create-user!
  "Insert a user row into DB, returning the created user (including auto-generated :id)."
  [{:keys [name email password] :as user-map}]
  ;; Build INSERT statement
  (let [insert-query (-> (h/insert-into :users)
                         (h/values [user-map])  ; user-map has name, email, password
                         (sql/format))
        ;; Execute with :return-generated-keys => returns a vector with a map that includes :id
        result (jdbc/execute! (db/datasource)
                              insert-query
                              {:return-generated-keys true
                               :builder-fn rs/as-unqualified-lower-maps})
        generated-id (:id (first result))]
    ;; Merge the newly generated ID with the original user data so we have
    ;; {:id ..., :name "X", :email "Y", :password "Z"}
    (assoc user-map :id generated-id)))
