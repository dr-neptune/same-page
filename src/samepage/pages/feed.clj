(ns samepage.pages.feed
  (:require [samepage.pages.layout :as layout]
            [hiccup2.core :as h]))

(defn feed-page
  "Root feed page. Displays each item with:
   [ user avatar ][ action icon ] => username + short timestamp => message."
  [request feed-items]
  (layout/page-layout
   request
   "Global Feed"
   [:div {:class "max-w-2xl mx-auto bg-[#2a2136] p-6 rounded shadow-md"}
    [:h1 {:class "text-3xl mb-4 font-bold"} "Global Activity Feed"]

    (if (empty? feed-items)
      [:p "No items in the feed yet!"]
      [:ul {:class "space-y-4"}
       (for [{:keys [feed_type username profile_pic message created_at]} feed-items]
         (let [formatted-time (layout/format-timestamp created_at)]
           [:li {:class "border border-gray-600 rounded p-4 bg-[#2f2b3b]"}
            ;; We'll have a row with 3 'columns':
            ;; (1) user avatar, (2) feed icon, (3) text content
            [:div {:class "flex items-center space-x-4"}
             ;; LEFT column(s) => user avatar + feed icon side by side
             [:div {:class "flex items-center space-x-2"}
              ;; user avatar => if profile_pic is blank, fallback
              (if (seq (str profile_pic))
                [:img {:src profile_pic
                       :alt (str username " avatar")
                       :class "w-12 h-12 object-cover rounded-lg border border-gray-500"}]
                ;; fallback if no pic
                [:div {:class "w-12 h-12 rounded-full bg-gray-600 border border-gray-500
                               flex items-center justify-center text-sm text-white"}
                 "?"])
              ;; feed icon => bigger square
              [:div {:class "w-12 h-12 flex items-center justify-center
                             rounded-md bg-gray-700 text-2xl"}
               feed_type]]

             ;; RIGHT column => user/time on top, then message
             [:div {:class "flex-1"}
              ;; First line => user name link + timestamp
              [:div {:class "mb-1 text-sm text-gray-400"}
               [:a {:href (str "/u/" username)
                    :class "text-pink-300 font-semibold hover:underline mr-2"}
                (or username "???")]
               formatted-time]

              ;; The main message => e.g. "Note: hi there", "Goal: become a rockstar", etc.
              [:div {:class "text-[#e0def2]"}
               message]]]]))])]))
