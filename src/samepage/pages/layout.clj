(ns samepage.pages.layout
  (:require [hiccup.page :refer [html5]]
            [hiccup2.core :as hc])
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
  "Common site layout with a top bar for navigation (including emojis),
   plus a toggleGoalRow() script so that expanding goals works."
  [request title & body-content]
  (let [session   (:session request)
        user      (:user session)
        is-admin? (= "admin" (:role user))]
    (html5
     [:head
      [:meta {:charset "utf-8"}]
      [:meta {:name "viewport" :content "width=device-width, initial-scale=1.0"}]
      [:title title]
      ;; Tailwind + htmx
      [:script {:src "https://cdn.tailwindcss.com"}]
      [:script {:src "https://unpkg.com/htmx.org@1.9.2"}]
      [:link {:rel "stylesheet"
              :href "/css/webfont/tabler-icons.min.css"}]]
     [:body {:class "min-h-screen bg-[#1e1e28] text-[#e0def2]"}

      ;; -- TOP NAV --
      [:div {:class "flex justify-between items-center p-2 bg-[#2a2136]"}
       ;; Left side => link to Feed
       [:div
        [:a {:href "/"
             :class "text-xl text-purple-400 hover:underline font-bold"}
         "📰 Feed"]]

       ;; Right side => depends if user is logged in
       (if (nil? user)
         ;; Logged OUT => show Log In & Register
         [:div {:class "space-x-4"}
          [:a {:href "/login"
               :class "underline"} "🔑 Login"]
          [:a {:href "/register"
               :class "underline"} "📝 Register"]]

         ;; Logged IN => show Dashboard, maybe Admin, and Log Out
         [:div {:class "space-x-4"}
          [:a {:href "/dashboard"
               :class "underline"} "🎯 Dashboard"]
          (when is-admin?
            [:a {:href "/admin"
                 :class "underline"} "👑 Admin"])
          [:a {:href "/logout"
               :class "underline"} "🔓 Logout"]])]

      ;; MAIN CONTENT
      [:div {:class "p-8"}
       body-content]

      ;; -- Script for toggling goal rows (expansion) --
      [:script
       "function toggleGoalRow(goalId) {
          let detailId = 'goal-detail-' + goalId;
          let detailEl = document.getElementById(detailId);
          if (!detailEl) return;

          if (detailEl.dataset.state === 'open') {
            // It's open => collapse
            detailEl.dataset.state = 'closed';
            detailEl.classList.add('hidden');
            detailEl.innerHTML = '';
          } else {
            // It's closed => expand via HTMX GET
            detailEl.dataset.state = 'open';
            detailEl.classList.remove('hidden');
            htmx.ajax('GET', '/goals/' + goalId + '/desc', detailEl, {credentials: 'include'});
          }
        }"]])))
