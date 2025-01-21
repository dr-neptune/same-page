(ns samepage.server.system
  (:require [samepage.server.routes.core :as routes]
            [samepage.server.db.core :as db]
            [ring.adapter.jetty :as jetty]
            [ring.middleware.params :refer [wrap-params]]
            [ring.middleware.session :refer [wrap-session]]
            [ring.middleware.resource :refer [wrap-resource]]
            [ring.middleware.session.memory :refer [memory-store]]))

(defn make-app [system]
  (-> (partial #'routes/root-handler system)
      ;; Move wrap-session to the *front*, so that the session is handled
      ;; before we serve static resources or parse params, etc.
      (wrap-session
        {:store (memory-store)
         :cookie-attrs {:secure false
                        :same-site :lax
                        :http-only true}})
      (wrap-resource "public")
      wrap-params))

(defn start-server [system]
  (db/create-schema!)  ;; If your table structure changed, ensure it matches the real DB
  (jetty/run-jetty
   (make-app system)
   {:port 9999 :join? false}))

(defn stop-server [system]
  (some-> (:server system) .stop))

(defn start-system []
  (let [server (start-server {})]
    {:server server}))

(defn stop-system
  [system]
  (when-let [^org.eclipse.jetty.server.Server s (:server system)]
    (.stop s)))
