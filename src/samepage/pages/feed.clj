(ns samepage.pages.feed
  (:require [samepage.pages.layout :as layout]
            [hiccup2.core :as h]))

(defn feed-page
  "Renders the global feed. Each item has :feed_type, :username, :message, :created_at."
  [request feed-items]
  (layout/page-layout
   request
   "Global Feed"
   [:div {:class "max-w-2xl mx-auto bg-[#2a2136] p-6 rounded shadow-md"}
    [:h1 {:class "text-3xl mb-4 font-bold"} "Global Activity Feed"]

    (if (empty? feed-items)
      [:p "No items in the feed yet!"]
      [:ul {:class "space-y-4"}
       (for [{:keys [feed_type username message created_at]} feed-items]
         [:li {:class "border border-gray-600 rounded p-4 bg-[#2f2b3b]"}
          ;; Show the feed_type (emoji) and user/time line:
          [:div {:class "text-sm text-gray-400 mb-1"}
           (str feed_type " " (or username "???") " @ " created_at)]
          ;; The main message
          [:div {:class "text-[#e0def2]"}
           message]])])]))
