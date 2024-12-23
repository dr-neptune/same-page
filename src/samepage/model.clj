(ns samepage.model)

(def notes
  "Atom to store notes keyed by a generated ID."
  (atom {}))

(defn create-note!
  "Store the given text in the notes atom under a fresh UUID."
  [text]
  (let [note-id (str (java.util.UUID/randomUUID))]
    (swap! notes assoc note-id text)
    note-id))

(defn get-note
  "Retrieve the note text from the atom by note-id."
  [note-id]
  (get @notes note-id))
