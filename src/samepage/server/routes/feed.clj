(ns samepage.server.routes.feed
  (:require [samepage.model.feed :as feed-model]
            [samepage.pages.feed :as feed-page]))

(defn feed-handler
  [_system request]
  (let [items (feed-model/get-global-feed)]
    {:status  200
     :headers {"Content-Type" "text/html"}
     :body    (feed-page/feed-page request items)}))
