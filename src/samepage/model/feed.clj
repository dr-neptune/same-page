(ns samepage.model.feed
  (:require [next.jdbc :as jdbc]
            [next.jdbc.result-set :as rs]
            [honey.sql :as sql]
            [honey.sql.helpers :as h]
            [samepage.server.db.core :as db]))

(defn get-global-feed
  "Returns a seq of maps like:
   {:feed_type  \"📝\" or \"🎯\" or \"🏋️\"
    :username   \"alice\"
    :message    \"Wrote a note: Hello world!\"
    :created_at #inst ... }
   sorted newest first."
  []
  (let [union-query
        (-> (h/select
              ;; NOTES feed:
              [[:raw "'📝'"] :feed_type]
              [[:raw "COALESCE(users.name, '???')"] :username]
              [[:raw "CONCAT('Wrote a note: ', notes.text)"] :message]
              [[:raw "notes.created_at"] :created_at])
            (h/from :notes)
            ;; In your DB, notes table has :notes.user_name. We'll attempt an INNER JOIN
            ;; on users.name. If you want old notes to appear even if the user is gone, use LEFT JOIN.
            (h/join :users [:= :notes.user_name :users.name])

            ;; UNION with GOALS feed:
            (h/union-all
              (-> (h/select
                    [[:raw "'🎯'"] :feed_type]
                    [[:raw "COALESCE(u.name, '???')"] :username]
                    [[:raw "CONCAT('Created a new goal: ', goals.title)"] :message]
                    [[:raw "goals.created_at"] :created_at])
                  (h/from :goals)
                  (h/join [:users :u] [:= :goals.user_id :u.id])))

            ;; UNION with PRACTICE LOGS feed:
            (h/union-all
              (-> (h/select
                    [[:raw "'🏋️'"] :feed_type]
                    [[:raw "COALESCE(u2.name, '???')"] :username]
                    ;; We'll show duration + goal title + optional notes in parentheses
                    [[:raw "
                      CONCAT(
                        'Practiced ', pl.duration, ' min on ',
                        g.title,
                        CASE
                          WHEN pl.notes IS NOT NULL AND pl.notes <> ''
                          THEN CONCAT(' (', pl.notes, ')')
                          ELSE ''
                        END
                      )"] :message]
                    [[:raw "pl.created_at"] :created_at])
                  (h/from [:practice_logs :pl])
                  (h/join [:goals :g] [:= :pl.goal_id :g.id])
                  (h/join [:users :u2] [:= :g.user_id :u2.id])))

            (h/order-by [:created_at :desc])
            (sql/format))]
    (jdbc/execute! (db/datasource)
                   union-query
                   {:builder-fn rs/as-unqualified-lower-maps})))
