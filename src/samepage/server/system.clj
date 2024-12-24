(ns samepage.server.system
    (:require [ring.adapter.jetty :as jetty]
              [ring.middleware.params :refer [wrap-params]]
              [ring.middleware.session :refer [wrap-session]]
              [samepage.server.routes :as routes]
              [samepage.server.db.core :as db])  ;; <-- add
    (:import (org.eclipse.jetty.server Server)))

(set! *warn-on-reflection* true)

(defn make-app
      [system]
      (-> (partial #'routes/root-handler system)
          (wrap-params)
          (wrap-session)))

(defn start-server
      [system]
      (jetty/run-jetty
       (make-app system)
       {:port 9999
        :join? false}))

(defn stop-server
      [server]
      (Server/.stop server))

(defn start-system
      []
      (let [system-state {}]
    ;; Ensure the notes table is created in H2
        (db/create-schema!)
        {::server (start-server system-state)}))

(defn stop-system
      [system]
      (stop-server (::server system)))
