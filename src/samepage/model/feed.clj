(ns samepage.model.feed
  (:require [next.jdbc :as jdbc]
            [next.jdbc.result-set :as rs]
            [honey.sql :as sql]
            [honey.sql.helpers :as h]
            [samepage.server.db.core :as db]))

(defn get-global-feed
  "Returns a seq of maps like:
   {:feed_type \"📝\"|\"🎯\"|\"🏋️\"
    :username  \"alice\"
    :profile_pic \"(unused for feed now)\"
    :message   \"Note: Hi all\"
    :created_at #inst}
   sorted newest first."
  []
  (let [union-query
        (-> (h/select
              ;; 1) NOTES => feed_type 📝, message => "Note: text"
              [[:raw "'📝'"] :feed_type]
              [[:raw "COALESCE(users.name, '???')"] :username]
              [[:raw "COALESCE(users.profile_pic, '')"] :profile_pic]
              [[:raw "CONCAT('Note: ', notes.text)"] :message]
              [[:raw "notes.created_at"] :created_at])
            (h/from :notes)
            (h/join :users [:= :notes.user_name :users.name])

            ;; 2) GOALS => feed_type 🎯, message => "Goal: <title>"
            (h/union-all
              (-> (h/select
                    [[:raw "'🎯'"] :feed_type]
                    [[:raw "COALESCE(u.name, '???')"] :username]
                    [[:raw "COALESCE(u.profile_pic, '')"] :profile_pic]
                    [[:raw "CONCAT('Goal: ', goals.title)"] :message]
                    [[:raw "goals.created_at"] :created_at])
                  (h/from :goals)
                  (h/join [:users :u] [:= :goals.user_id :u.id])))

            ;; 3) PRACTICE => feed_type 🏋️, message => "Practice: <duration> min: <goal title>: <notes>"
            (h/union-all
              (-> (h/select
                    [[:raw "'🏋️'"] :feed_type]
                    [[:raw "COALESCE(u2.name, '???')"] :username]
                    [[:raw "COALESCE(u2.profile_pic, '')"] :profile_pic]
                    [[:raw "
                      CONCAT(
                        'Practice: ',
                        pl.duration, ' min: ',
                        g.title, ': ',
                        COALESCE(pl.notes, '')
                      )"] :message]
                    [[:raw "pl.created_at"] :created_at])
                  (h/from [:practice_logs :pl])
                  (h/join [:goals :g] [:= :pl.goal_id :g.id])
                  (h/join [:users :u2] [:= :g.user_id :u2.id])))

            (h/order-by [:created_at :desc])
            (sql/format))]
    (jdbc/execute! (db/datasource) union-query
                   {:builder-fn rs/as-unqualified-lower-maps})))
