(ns samepage.model.model
  (:import (java.time Instant)))

(defn get-session-notes
  "Retrieve all notes from the session. Returns an empty vector if none present."
  [session]
  (or (:notes session) []))

(defn add-session-note
  "Add a new note to the session, returning the updated session map.
   We'll attach the user name from session if available."
  [session note-text]
  (let [user-name (get-in session [:user :name] "Anonymous")
        note {:id        (str (java.util.UUID/randomUUID))
              :text      note-text
              :timestamp (Instant/now)
              :user      user-name}]
    (update session :notes (fnil conj []) note)))
