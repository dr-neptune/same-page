(ns samepage.pages.feed
  (:require [samepage.pages.layout :as layout]
            [clojure.string :as str]
            [hiccup2.core :as h]))

;; We'll parse the aggregator's feed_type & message into a
;; 2-element vector: [top-line bottom-line].
;; But top-line itself will be a Hiccup vector so we can inject an <a> for the username.
(defn- parse-feed-lines
  "Given feed_type (📝/🎯/🏋️),
   aggregator message (e.g. 'Goal: MyTitle'),
   plus username & date, produce [top-line-hiccup bottom-line-string]."
  [feed_type username formatted-time message]
  (cond
    ;; 1) Note => aggregator might be 'Note: <text>'
    (= feed_type "📝")
    (let [text (-> message
                   (str/replace-first #"(?i)^note:\s*" "")  ;; remove 'Note:'
                   (str/trim))]
      ;; top line => [ :span [...hiccup...] ]
      [[:span
        [:a {:href (str "/u/" username)
             :class "text-pink-300 font-semibold hover:underline mr-1"}
         (or username "???")]
        " | 📝 Note | "
        formatted-time]
       text])

    ;; 2) Goal => aggregator might be 'Goal: <title>'
    (= feed_type "🎯")
    (let [title (-> message
                    (str/replace-first #"(?i)^goal:\s*" "")  ;; remove 'Goal:'
                    (str/trim))]
      [[:span
        [:a {:href (str "/u/" username)
             :class "text-pink-300 font-semibold hover:underline mr-1"}
         (or username "???")]
        " | 🎯 Created Goal: " title " | "
        formatted-time]
       ""])

    ;; 3) Practice => aggregator might be 'Practice: <duration>: <goal name>: <notes>'
    (= feed_type "🏋️")
    (let [parts (map str/trim (str/split message #":" 4))
          ;; example => ["Practice" "30 min" "SomeGoal" "some text..."]
          dur   (nth parts 1 "")
          goal  (nth parts 2 "")
          note  (nth parts 3 "")]
      [[:span
        [:a {:href (str "/u/" username)
             :class "text-pink-300 font-semibold hover:underline mr-1"}
         (or username "???")]
        " | " goal " | 🏋️ Practice | " dur " | "
        formatted-time]
       note])

    ;; fallback => feed_type not recognized => just show everything in top line
    :else
    [[:span
      [:a {:href (str "/u/" username)
           :class "text-pink-300 font-semibold hover:underline mr-1"}
       (or username "???")]
      " | " feed_type " | " formatted-time]
     message]))

(defn feed-page
  "Renders each feed item as:
   [avatar] => top line (hiccup) => bottom line (string).
   The top line is user name link, plus feed icon + text, plus date.
   The bottom line is extra text (e.g. the note or practice details)."
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
         (let [formatted-time (layout/format-timestamp created_at)
               [top-line bottom-line] (parse-feed-lines feed_type username formatted-time message)]
           [:li {:class "border border-gray-600 rounded p-4 bg-[#2f2b3b]"}
            [:div {:class "flex items-start space-x-4"}
             ;; (1) Avatar on the left
             (if (seq (str profile_pic))
               [:img {:src profile_pic
                      :alt (str username " avatar")
                      :class "w-12 h-12 object-cover rounded-full border border-gray-500"}]
               ;; fallback if no pic
               [:div {:class "w-12 h-12 rounded-full bg-gray-600 border border-gray-500
                              flex items-center justify-center text-sm text-white"}
                "?"])
             ;; (2) The textual content => top line (hiccup) + bottom line (string)
             [:div {:class "flex-1"}
              ;; top line => a small line with user link, feed icon or text, date
              [:div {:class "text-sm text-gray-400 mb-1"}
               top-line]
              ;; bottom line => only show if non-empty
              (when (seq (str/trim bottom-line))
                [:div {:class "text-[#e0def2] whitespace-pre-wrap"}
                 bottom-line])]]]))])]))
