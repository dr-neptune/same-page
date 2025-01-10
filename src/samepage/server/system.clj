(ns samepage.server.system
  (:require [samepage.server.routes.core :as routes]
            [samepage.server.db.core :as db]
            [ring.adapter.jetty :as jetty]
            [ring.middleware.params :refer [wrap-params]]
            [ring.middleware.session :refer [wrap-session]]))

(defn make-app
  [system]
  (-> (partial #'routes/root-handler system)
      wrap-params
      wrap-session))

(defn start-server
  [system]
  (jetty/run-jetty
   (make-app system)
   {:port 9999 :join? false}))

(defn stop-server
  [server]
  (.stop server))

(defn start-system
  []
  (db/create-schema!)
  (let [server (start-server {})]
    {:server server}))

(defn stop-system
  [system]
  (when-let [^org.eclipse.jetty.server.Server s (:server system)]
    (.stop s)))
