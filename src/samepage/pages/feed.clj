(ns samepage.pages.feed
  (:require [samepage.pages.layout :as layout]
            [hiccup2.core :as h]))

(defn feed-page
  "Renders the global feed. If the user is logged in, show 'Create Note' & 'Create Goal' buttons."
  [request feed-items]
  (let [user (-> request :session :user)]
    (layout/page-layout
     request
     "Global Feed"
     [:div {:class "max-w-2xl mx-auto bg-[#2a2136] p-6 rounded shadow-md"}
      [:h1 {:class "text-3xl mb-4 font-bold"} "Global Activity Feed"]

      (when user
        [:div {:class "flex space-x-4 mb-6"}
         [:a {:href "/notes/new"
              :class "bg-purple-600 text-white py-2 px-4 rounded hover:bg-purple-700"}
          "Create a Note"]
         [:a {:href "/goals/new"
              :class "bg-purple-600 text-white py-2 px-4 rounded hover:bg-purple-700"}
          "Create a Goal"]])

      (if (empty? feed-items)
        [:p "No items in the feed yet!"]
        [:ul {:class "space-y-4"}
         (for [{:keys [feed_type username profile_pic message created_at]} feed-items]
           [:li {:class "border border-gray-600 rounded p-4 bg-[#2f2b3b]"}
            ;; Row top => avatar, username link, feed emoji, date
            [:div {:class "flex items-center mb-2 space-x-2"}
             (if (seq profile_pic)
               [:img {:src profile_pic
                      :alt (str username " avatar")
                      :class "w-8 h-8 object-cover rounded-full border border-gray-500"}]
               [:div {:class "w-8 h-8 rounded-full bg-gray-600 border border-gray-500
                              flex items-center justify-center text-sm text-white"}
                "?"])
             [:a {:href (str "/u/" username)
                  :class "text-pink-300 font-semibold hover:underline"}
              (or username "???")]
             [:span {:class "text-sm"} feed_type]
             [:span {:class "text-xs text-gray-400 ml-auto"}
              (str created_at)]]

            ;; Message
            [:div {:class "ml-10 text-[#e0def2]"}
             message]])])])))
