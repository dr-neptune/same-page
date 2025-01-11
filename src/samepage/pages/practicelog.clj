(ns samepage.pages.practicelog
  (:require [samepage.pages.layout :as layout])
  (:import (java.time LocalDateTime ZoneId)
           (java.time.format DateTimeFormatter)))

;; Helper to produce a default value for <input type="datetime-local">
(defn now-datetime-string
  "Returns a string like '2025-01-11T08:10' for the local time now."
  []
  (let [ldt (LocalDateTime/now (ZoneId/systemDefault))
        fmt (DateTimeFormatter/ofPattern "yyyy-MM-dd'T'HH:mm")]
    (.format ldt fmt)))

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
      ;; default to "now" in local time
      :value (now-datetime-string)}]]
   [:div
    [:label {:class "block font-semibold"} "Duration (hours):"]
    [:input {:type "number"
             :step "0.5"
             :min "0"
             :name "duration"
             :class "w-32 p-2 border border-gray-300 rounded bg-[#2f2b3b] text-[#e0def2]"
             :value "1"}]]
   [:div
    [:label {:class "block font-semibold"} "Notes:"]
    [:textarea {:name "notes"
                :class "w-full h-20 p-2 border border-gray-300 rounded bg-[#2f2b3b] text-[#e0def2]"}]]
   [:button {:type "submit"
             :class "mt-2 bg-purple-600 text-white px-4 py-2 rounded hover:bg-purple-700"}
    "Create Log"]])

(ns samepage.pages.practicelog
  (:require [samepage.pages.layout :as layout]))

(defn practice-logs-table
  [practice-logs]
  (if (empty? practice-logs)
    [:p "No practice logs yet!"]
    [:table {:class "min-w-full border border-gray-600 text-left mb-6"}
     [:thead
      [:tr
       [:th {:class "py-2 px-4 border-b border-gray-600"} "Date"]
       [:th {:class "py-2 px-4 border-b border-gray-600"} "Duration (hrs)"]
       [:th {:class "py-2 px-4 border-b border-gray-600"} "Notes"]]]
     [:tbody
      (for [{:keys [id duration notes practice_date]} practice-logs]
        [:tr {:key id :class "hover:bg-[#3b2a40]"}
         [:td {:class "py-2 px-4 border-b border-gray-600"}
          (layout/format-timestamp practice_date)]
         [:td {:class "py-2 px-4 border-b border-gray-600"}
          (str duration)]
         [:td {:class "py-2 px-4 border-b border-gray-600"}
          (or notes "")]])]]))

(defn new-practice-log-form
  [goal-id]
  [:form {:action (str "/goals/" goal-id "/practice-logs")
          :method "post"
          :class "space-y-4"}
   [:div
    [:label {:class "block font-semibold"} "Date of log:"]
    [:input {:type "datetime-local"
             :name "practice_date"
             :class "w-full p-2 border border-gray-300 rounded bg-[#2f2b3b] text-[#e0def2]"
             :required true}]]
   [:div
    [:label {:class "block font-semibold"} "Duration (hours):"]
    [:input {:type "number"
             :step "0.5"
             :min "0"
             :name "duration"
             :class "w-32 p-2 border border-gray-300 rounded bg-[#2f2b3b] text-[#e0def2]"
             :value "1"}]]
   [:div
    [:label {:class "block font-semibold"} "Notes:"]
    [:textarea {:name "notes"
                :class "w-full h-20 p-2 border border-gray-300 rounded bg-[#2f2b3b] text-[#e0def2]"}]]
   [:button {:type "submit"
             :class "mt-2 bg-purple-600 text-white px-4 py-2 rounded hover:bg-purple-700"}
    "Create Log"]])

(defn practice-logs-page
  [request goal practice-logs]
  "Page listing existing logs for a goal, plus link to create a new log.
   Shows more info about the goal, plus a link back to root page."
  (layout/page-layout
   request
   (str "Practice Logs for " (:title goal))
   [:div {:class "max-w-2xl mx-auto bg-[#2a2136] p-6 rounded shadow-md"}
    ;; More info about the goal:
    [:h1 {:class "text-3xl mb-4 font-bold"} (str "Goal: " (:title goal))]
    (when-let [desc (:description goal)]
      [:p {:class "mb-2"} [:strong "Description: "] desc])
    (when-let [target (:target_hours goal)]
      [:p {:class "mb-2"} [:strong "Target Hours: "] target])
    ;; Show logs
    [:hr {:class "my-4 border-gray-500"}]
    (practice-logs-table practice-logs)

    ;; Link to new practice log
    [:div {:class "mt-4"}
     [:a {:href (str "/goals/" (:id goal) "/practice-logs/new")
          :class "bg-purple-600 text-white py-2 px-4 rounded hover:bg-purple-700"}
      "Log a Practice"]]
    ;; Link back home:
    [:div {:class "mt-4"}
     [:a
      {:href "/"
       :class "bg-purple-600 text-white py-2 px-4 rounded hover:bg-purple-700"}
      "← Back to Overview"]]]))

(defn new-practice-log-page
  [request goal-id]
  (layout/page-layout
   request
   "Log a Practice"
   [:div {:class "max-w-md mx-auto bg-[#2a2136] p-6 rounded shadow-md"}
    [:h1 {:class "text-3xl mb-4 font-bold"} "Log a New Practice"]
    (new-practice-log-form goal-id)]))
