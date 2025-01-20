(ns samepage.pages.practicelog
  (:require [samepage.pages.layout :as layout])
  (:import (java.time LocalDateTime ZoneId)
           (java.time.format DateTimeFormatter)))

;; -------------- NEW helper for showing minutes as "Xh Ym" --------------
(defn minutes->display
  "Convert an integer `m` (minutes) into 'Xh Ym' string."
  [m]
  (let [hrs (quot m 60)
        min (rem m 60)]
    (str hrs "h " min "m")))

(defn now-datetime-string
  "Returns a string like '2025-01-11T08:10' for the local time now."
  []
  (let [ldt (LocalDateTime/now (ZoneId/systemDefault))
        fmt (DateTimeFormatter/ofPattern "yyyy-MM-dd'T'HH:mm")]
    (.format ldt fmt)))

(defn practice-logs-table
  "Shows a table of existing practice logs."
  [practice-logs]
  (if (empty? practice-logs)
    [:p "No practice logs yet!"]
    [:table {:class "min-w-full border border-gray-600 text-left mb-6"}
     [:thead
      [:tr
       [:th {:class "py-2 px-4 border-b border-gray-600"} "Date"]
       [:th {:class "py-2 px-4 border-b border-gray-600"} "Duration"]
       [:th {:class "py-2 px-4 border-b border-gray-600"} "Notes"]]]
     [:tbody
      (for [{:keys [id duration notes practice_date]} practice-logs]
        [:tr {:key id :class "hover:bg-[#3b2a40]"}
         [:td {:class "py-2 px-4 border-b border-gray-600"}
          (layout/format-timestamp practice_date)]
         ;; Instead of just (str duration), convert to "Xh Ym":
         [:td {:class "py-2 px-4 border-b border-gray-600"}
          (minutes->display duration)]
         [:td {:class "py-2 px-4 border-b border-gray-600"}
          (or notes "")]])]]))

;; -------------- UPDATED form to collect hours & minutes separately --------------
(defn new-practice-log-form
  [goal-id]
  [:form {:action (str "/goals/" goal-id "/practice-logs")
          :method "post"
          :class "space-y-4"}
   [:div
    [:label {:class "block font-semibold"} "Date of log:"]
    [:input
     {:type "datetime-local"
      :name "practice_date"
      :class "w-full p-2 border border-gray-300 rounded bg-[#2f2b3b] text-[#e0def2]"
      :required true
      :value (now-datetime-string)}]]
   ;; Hours input
   [:div
    [:label {:class "block font-semibold"} "Hours:"]
    [:input {:type "number"
             :name "hours"
             :class "w-32 p-2 border border-gray-300 rounded bg-[#2f2b3b] text-[#e0def2]"
             :min "0"
             :value "0"}]]
   ;; Minutes input, default to 30:
   [:div
    [:label {:class "block font-semibold"} "Minutes:"]
    [:input {:type "number"
             :name "minutes"
             :class "w-32 p-2 border border-gray-300 rounded bg-[#2f2b3b] text-[#e0def2]"
             :min "0"
             :max "59"
             :value "30"}]]
   [:div
    [:label {:class "block font-semibold"} "Notes:"]
    [:textarea {:name "notes"
                :class "w-full h-20 p-2 border border-gray-300 rounded bg-[#2f2b3b] text-[#e0def2]"}]]
   [:button {:type "submit"
             :class "mt-2 bg-purple-600 text-white px-4 py-2 rounded hover:bg-purple-700"}
    "Create Log"]])

(defn practice-logs-page
  [request goal practice-logs]
  (layout/page-layout
   request
   (str "Practice Logs for " (:title goal))
   [:div {:class "max-w-2xl mx-auto bg-[#2a2136] p-6 rounded shadow-md"}
    ;; Basic goal info:
    [:h1 {:class "text-3xl mb-4 font-bold"} (str "Goal: " (:title goal))]
    (when-let [desc (:description goal)]
      [:p {:class "mb-2"} [:strong "Description: "] desc])
    (when-let [target (:target_hours goal)]
      [:p {:class "mb-2"} [:strong "Target Hours: "] target])

    ;; Show logs:
    [:hr {:class "my-4 border-gray-500"}]
    (practice-logs-table practice-logs)

    ;; Links
    [:div {:class "mt-4 space-x-2"}
     [:a {:href "/"
          :class "bg-zinc-600 text-white py-2 px-4 rounded hover:bg-purple-700"}
      "← Back Home"]
     [:a {:href (str "/goals/" (:id goal) "/practice-logs/new")
          :class "bg-purple-600 text-white py-2 px-4 rounded hover:bg-purple-700"}
      "Log a Practice"]]]))

(defn new-practice-log-page
  [request goal-id]
  (layout/page-layout
   request
   "Log a Practice"
   [:div {:class "max-w-md mx-auto bg-[#2a2136] p-6 rounded shadow-md"}
    [:h1 {:class "text-3xl mb-4 font-bold"} "Log a New Practice"]
    (new-practice-log-form goal-id)]))
