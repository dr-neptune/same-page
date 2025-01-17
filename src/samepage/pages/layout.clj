(ns samepage.pages.layout
  (:require [hiccup.page :refer [html5]]
            [hiccup2.core :as hc])  ;; might use if building partials
  (:import (java.sql Timestamp)
           (java.time.format DateTimeFormatter)
           (java.time ZoneId)))

(defn format-timestamp
  [^Timestamp ts]
  (let [formatter (DateTimeFormatter/ofPattern "yyyy-MM-dd HH:mm")
        instant   (.toInstant ts)
        zdt       (.atZone instant (ZoneId/systemDefault))]
    (.format zdt formatter)))

(defn page-layout
  "Common layout. If user is logged in, display top bar w/ admin link if role=admin.
   Also includes a <script> for toggling goal rows."
  [request title & body-content]
  (let [session    (:session request)
        user       (:user session)
        is-admin?  (= "admin" (:role user))
        user-info  (when user
                     [:div {:class "text-right p-2 bg-[#2a2136] mb-6"}
                      [:span
                       "Logged in as: "
                       [:span {:class "text-pink-400 font-semibold"} (or (:name user) "???")]
                       " ("
                       [:span {:class "font-semibold"} (or (:email user) "???")]
                       ") "]
                      (when is-admin?
                        [:a {:href "/admin"
                             :class "underline ml-4"} "[Admin Panel]"])
                      (when user
                        [:a {:href "/logout" :class "underline ml-4"} "[Logout]"])])]
    (html5
     [:head
      [:meta {:charset "utf-8"}]
      [:meta {:name "viewport" :content "width=device-width, initial-scale=1.0"}]
      [:title title]
      ;; tailwind + htmx
      [:script {:src "https://cdn.tailwindcss.com"}]
      [:script {:src "https://unpkg.com/htmx.org@1.9.2"}]
      ;; Our custom JS:
      [:script
       "function toggleGoalRow(goalId) {
          let detailId = 'goal-detail-' + goalId;
          let row = document.getElementById(detailId);
          if (row) {
            if (row.dataset.state === 'open') {
              // It's open => collapse:
              row.innerHTML = '';
              row.dataset.state = 'closed';
            } else {
              // It's closed => expand via HTMX:
              row.dataset.state = 'open';
              htmx.ajax('GET', '/goals/' + goalId + '/desc', row);
            }
          }
        }"]]
     [:body
      {:class "min-h-screen p-8 bg-[#1e1e28] text-[#e0def2]"}
      user-info
      body-content])))
