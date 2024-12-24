(ns samepage.model
  (:import (java.time Instant)))

(defn get-session-notes
  "Retrieve all notes from the session. Returns an empty vector if none present."
  [session]
  (or (:notes session) []))

(defn add-session-note
  "Add a new note to the session, returning the updated session map."
  [session note-text]
  (let [note {:id        (str (java.util.UUID/randomUUID))
              :text      note-text
              :timestamp (Instant/now)}]
    (update session :notes (fnil conj []) note)))
