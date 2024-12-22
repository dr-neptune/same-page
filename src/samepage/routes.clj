(ns samepage.routes
  (:require [reitit.ring :as reitit-ring]))

(defn hello-handler
  [system request]
  {:status 200
   :headers {"Content-Type" "text/html"}
   :body "Hello, world"})

(defn goodbye-handler
  [system request]
  {:status 200
   :headers {"Content-Type" "text/html"}
   :body "Goodbye, world"})

(defn routes
  [system]
  [["/"        {:get {:handler (partial #'hello-handler system)}}]
   ["/goodbye" {:get {:handler (partial #'goodbye-handler system)}}]])


(defn not-found-handler
  [_request]
  {:status 404
   :headers {"Content-Type" "text/html"}
   :body "Not Found"})

(defn root-handler
  [system request]
  (let [handler (reitit-ring/ring-handler
                  (reitit-ring/router
                    (routes system))
                  #'not-found-handler)]
    (handler request)))
