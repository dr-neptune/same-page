(ns samepage.model)

(def notes
  (atom {}))

(defn create-note!
  [text]
  (let [note-id (str (java.util.UUID/randomUUID))]
    (swap! notes assoc note-id text)
    note-id))

(defn get-note
  [note-id]
  (get @notes note-id))
