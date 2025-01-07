(ns samepage.server.db.core
    (:require [next.jdbc :as jdbc]))

;; 1) We create a single datasource for H2.
(defonce ^:private ds
         (jdbc/get-datasource
          {:dbtype   "h2"
           :dbname   "file:./db/samepage"
           :user     "sa"
           :password ""}))

(defn datasource []
      ds)

;; 2) Create the notes table if it doesn’t already exist.  We'll store
;;    the user name directly in 'user_name' for now. Going to avoid
;;    adding honeysql to this as DDL's don't seem to be as well
;;    supported and I will likely eventually move to a database
;;    migration tool. 
(defn create-schema!
      []
      (jdbc/execute! ds
                     ["
     CREATE TABLE IF NOT EXISTS notes (
       id IDENTITY PRIMARY KEY,
       user_name   VARCHAR(255),
       text        VARCHAR(4000) NOT NULL,
       created_at  TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP
     )
    "]))
