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
  "Common layout. If user is logged in, display a top bar with a home link on the left,
   user info on the right, plus an admin link if role=admin. Also includes a <script>
   for toggling goal rows via htmx partial expansions."
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
      [:link
       {:rel "stylesheet"
        :href "/css/webfont/tabler-icons.min.css"}]
      ;; Our custom JS for goal row toggling:
      [:script
       "function toggleGoalRow(goalId) {
    let detailId = 'goal-detail-' + goalId;
    let detailEl = document.getElementById(detailId);
    if (!detailEl) return;

    if (detailEl.dataset.state === 'open') {
      // It's open => collapse:
      detailEl.dataset.state = 'closed';
      // Remove the content & hide the div
      detailEl.innerHTML = '';
      detailEl.classList.add('hidden');
    } else {
      // It's closed => expand via HTMX:
      detailEl.dataset.state = 'open';
      detailEl.classList.remove('hidden');
      htmx.ajax('GET', '/goals/' + goalId + '/desc', detailEl, {credentials: 'include'});
    }
  }"]]
     [:body
      {:class "min-h-screen bg-[#1e1e28] text-[#e0def2]"}

      ;; Top bar container: flex with space-between
      [:div {:class "flex justify-between items-center p-2 bg-[#2a2136] mb-6"}
       ;; Left side => home link
       [:div
        [:a {:href "/"
             :class "text-xl text-purple-400 hover:underline font-bold"}
         "🏠"]]

       ;; Right side => user info (if logged in)
       (when user
         [:div
          ;; "Logged in as: someusername (someemail)"
          [:span
           "Logged in as: "
           ;; changed to a link
           [:a {:href "/profile"
                :class "text-pink-400 font-semibold hover:underline"}
            (or (:name user) "???")]
           " ("
           [:span {:class "font-semibold"} (or (:email user) "???")]
           ") "
           (when is-admin?
             [:a {:href "/admin" :class "underline ml-4"} "[Admin Panel]"])
           [:a {:href "/logout" :class "underline ml-4"} "[Logout]"]]])]

      ;; The main page content
      [:div {:class "p-8"}
       body-content]])))
