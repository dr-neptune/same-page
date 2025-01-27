(ns samepage.pages.feed
  (:require [samepage.pages.layout :as layout]
            [clojure.string :as str]
            [hiccup2.core :as h]))

;; Helper to parse aggregator messages into a top line & bottom line
(defn- parse-feed-lines
  "Given feed_type (📝/🎯/🏋️), aggregator message (e.g. 'Goal: MyTitle'),
   plus username & date, produce [top-line bottom-line]."
  [feed_type username formatted-time message]
  (cond
    ;; 1) NOTE => aggregator might be 'Note: <text>'
    (= feed_type "📝")
    (let [text (-> message
                   (str/replace-first #"(?i)^note:\s*" "")  ;; remove leading 'Note:'
                   (str/trim))]
      [(str username " | 📝 Note | " formatted-time)
       text])

    ;; 2) GOAL => aggregator might be 'Goal: <title>'
    (= feed_type "🎯")
    (let [title (-> message
                    (str/replace-first #"(?i)^goal:\s*" "")  ;; remove 'Goal:' prefix
                    (str/trim))]
      ;; We display it as "Created Goal: Title"
      [(str username " | 🎯 Created Goal: " title " | " formatted-time)
       ""]) ;; no separate bottom line for goals

    ;; 3) PRACTICE => aggregator might be 'Practice: <duration>: <goal name>: <notes>'
    (= feed_type "🏋️")
    (let [parts (map str/trim (str/split message #":" 4))
          ;; example => ["Practice" "30 min" "SomeGoal" "some practice note..."]
          dur   (nth parts 1 "")
          goal  (nth parts 2 "")
          note  (nth parts 3 "")]
      [(str username " | " goal " | 🏋️ Practice | " dur " | " formatted-time)
       note])

    ;; fallback => feed_type unrecognized => show entire message on top
    :else
    [(str username " | " feed_type " | " formatted-time)
     message]))

(defn feed-page
  "Renders each feed item with:
   [avatar] on the left, then two lines of text:
   - top line => parse-feed-lines => 'username | ... | date'
   - bottom line => extra text if any."
  [request feed-items]
  (layout/page-layout
   request
   "Global Feed"
   [:div {:class "max-w-2xl mx-auto bg-[#2a2136] p-6 rounded shadow-md"}
    [:h1 {:class "text-3xl mb-4 font-bold"} "Recent Activity"]

    (if (empty? feed-items)
      [:p "No items in the feed yet!"]

      [:ul {:class "space-y-4"}
       (for [{:keys [feed_type username profile_pic message created_at]} feed-items]
         (let [formatted-time (layout/format-timestamp created_at)
               [line1 line2] (parse-feed-lines feed_type username formatted-time message)]
           [:li {:class "border border-gray-600 rounded p-4 bg-[#2f2b3b]"}
            [:div {:class "flex items-start space-x-4"}
             ;; (1) Avatar on the far left
             (if (seq (str profile_pic))
               [:img {:src profile_pic
                      :alt (str username " avatar")
                      :class "w-12 h-12 object-cover rounded-full border border-gray-500"}]
               ;; fallback if no pic
               [:div {:class "w-12 h-12 rounded-full bg-gray-600 border border-gray-500
                              flex items-center justify-center text-sm text-white"}
                "?"])

             ;; (2) Two-line content
             [:div {:class "flex-1"}
              ;; top line => e.g. "alice | 🎯 Created Goal: Title | date"
              [:div {:class "text-sm text-gray-400 mb-1"}
               line1]
              ;; bottom line => if there's extra text (notes, etc.)
              (when (seq (str/trim line2))
                [:div {:class "text-[#e0def2] whitespace-pre-wrap"}
                 line2])]]]))])]))
