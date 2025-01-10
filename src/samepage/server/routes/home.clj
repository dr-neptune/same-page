(ns samepage.server.routes.home
  (:require [samepage.pages.home :as home]))

(defn root-page-handler
  [_system request]
  {:status  200
   :headers {"Content-Type" "text/html"}
   :body    (home/root-page request)})
