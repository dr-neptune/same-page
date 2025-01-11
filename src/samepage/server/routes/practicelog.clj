(ns samepage.server.routes.practicelog
  (:require [clojure.string :as str]
            [hiccup2.core :refer [html]]
            [samepage.model.goal :as goal-model]
            [samepage.model.practicelog :as pl-model]
            [samepage.pages.practicelog :as pl-pages])
  (:import
    (java.time LocalDateTime ZoneId)
    (java.time.format DateTimeFormatter)))

(defn get-practice-logs-for-goal-handler
  [_system request]
  (let [session    (:session request)
        user       (:user session)
        goal-id    (some-> (get-in request [:path-params :goal-id]) (Integer/parseInt))]
    (if (nil? user)
      {:status 302 :headers {"Location" "/login"} :body ""}
      (let [goal          (goal-model/get-goal-by-id goal-id)
            practice-logs (pl-model/get-practice-logs-for-goal goal-id)]
        {:status 200
         :headers {"Content-Type" "text/html"}
         :body (pl-pages/practice-logs-page request goal practice-logs)}))))

(defn get-new-practice-log-handler
  [_system request]
  (let [session  (:session request)
        user     (:user session)
        goal-id  (some-> (get-in request [:path-params :goal-id]) (Integer/parseInt))]
    (if (nil? user)
      {:status 302 :headers {"Location" "/login"} :body ""}
      {:status 200
       :headers {"Content-Type" "text/html"}
       :body (pl-pages/new-practice-log-page request goal-id)})))

(defn post-practice-log-handler
  [_system request]
  (let [session  (:session request)
        user     (:user session)
        goal-id  (some-> (get-in request [:path-params :goal-id]) (Integer/parseInt))
        params   (:params request)]
    (if (nil? user)
      {:status 302 :headers {"Location" "/login"} :body ""}
      (let [practice-date-str (or (get params "practice_date") "")
            duration          (some-> (get params "duration") (Double/parseDouble))
            notes             (get params "notes")]
        (pl-model/create-practice-log!
         {:goal-id goal-id
          :duration duration
          :notes notes
          :practice-date
          (if (str/blank? practice-date-str)
            (java.time.Instant/now)
            (let [fmt (DateTimeFormatter/ofPattern "yyyy-MM-dd'T'HH:mm")
                  ldt (LocalDateTime/parse practice-date-str fmt)
                  zdt (.atZone ldt (ZoneId/systemDefault))]
              (.toInstant zdt)))})
        {:status 302
         :headers {"Location" (str "/goals/" goal-id "/practice-logs")}
         :body ""}))))
