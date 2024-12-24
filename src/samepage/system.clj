(ns samepage.system
  (:require [ring.adapter.jetty :as jetty]
            [ring.middleware.params :refer [wrap-params]]
            [ring.middleware.session :refer [wrap-session]]  ;; <--- added
            [samepage.routes :as routes])
  (:import (org.eclipse.jetty.server Server)))

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
  (let [system-so-far {}]
    {::server (start-server system-so-far)}))

(defn stop-system
  [system]
  (stop-server (::server system)))
