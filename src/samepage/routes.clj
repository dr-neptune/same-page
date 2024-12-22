(ns samepage.routes
  (:require [reitit.ring :as reitit-ring]
            [samepage.pages :as pages]))

;; Return the entire HTML page
(defn root-page-handler
  [system request]
  {:status  200
   :headers {"Content-Type" "text/html"}
   :body    (pages/root-page)})

;; Return just a snippet for partial page update
(defn change-text-handler
  [system request]
  {:status  200
   :headers {"Content-Type" "text/html"}
   :body    "<p style='color: red;'>You just changed this text via HTMX!</p>"})

(defn routes
  [system]
  [["/"           {:get {:handler (partial #'root-page-handler system)}}]
   ["/change-text" {:get {:handler (partial #'change-text-handler system)}}]])

(defn not-found-handler
  [_request]
  {:status  404
   :headers {"Content-Type" "text/html"}
   :body    "Not Found"})

(defn root-handler
  [system request]
  (let [handler (reitit-ring/ring-handler
                  (reitit-ring/router (routes system))
                  #'not-found-handler)]
    (handler request)))
