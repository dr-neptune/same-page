(ns samepage.pages
  (:require [hiccup.page :refer [html5 include-js]]))

(defn root-page
  "Return an HTML page using Hiccup, with the HTMX script included from a CDN."
  []
  (html5
   [:head
    [:meta {:charset "utf-8"}]
    [:title "SamePage Example"]
    ;; Minimal: only HTMX script from a CDN
    (include-js "https://unpkg.com/htmx.org@1.9.5")]
   [:body
    [:h1 "Hello from Hiccup!"]
    [:p "This is a minimal example of using HTMX to update a small part of the page."]

    ;; Button triggers a GET request to /change-text
    ;; The response will replace the content of #demo-target
    [:button
     {:hx-get    "/change-text"
      :hx-target "#demo-target"
      :hx-swap   "innerHTML"}
     "Click Me!"]

    ;; The div whose content will be replaced
    [:div {:id "demo-target"}
     "Original content."]]))
