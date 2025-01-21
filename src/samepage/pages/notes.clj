(ns samepage.pages.notes
  (:require [samepage.pages.layout :as layout]
            [hiccup2.core :as hc]))


(defn notes-table
  [notes]
  [:div {:class "overflow-x-auto mt-6"}
   [:table {:class "min-w-full border border-gray-600 text-left"}
    [:thead
     [:tr
      [:th {:class "py-2 px-4 border-b border-gray-600"} "User"]
      [:th {:class "py-2 px-4 border-b border-gray-600"} "Note"]
      [:th {:class "py-2 px-4 border-b border-gray-600"} "Timestamp"]
      [:th {:class "py-2 px-4 border-b border-gray-600"} "Edit"]
      [:th {:class "py-2 px-4 border-b border-gray-600"} "Delete"]]]
    [:tbody
     (for [{:keys [id user_name text timestamp]} notes]
       [:tr {:key id :class "hover:bg-[#3b2a40]"}
        [:td {:class "py-2 px-4 border-b border-gray-600"} user_name]
        [:td {:class "py-2 px-4 border-b border-gray-600"} text]
        [:td {:class "py-2 px-4 border-b border-gray-600"}
         (layout/format-timestamp timestamp)]

        ;; EDIT button:
        [:td {:class "py-2 px-4 border-b border-gray-600"}
         [:a {:href (str "/notes/" id "/edit")
              :class "text-blue-500 hover:underline"}
          "✏️"]]

        ;; DELETE form:
        [:td {:class "py-2 px-4 border-b border-gray-600"}
         [:form
          {:action   (str "/notes/" id "/delete")
           :method   "post"
           :onsubmit "return confirm('Are you sure you want to delete this note?');"}
          [:button
           {:type  "submit"
            :class "text-red-500 hover:underline"}
           "🗑️"]]]])]]])

(defn note-form
  "A form to create a new note. We POST to /notes."
  []
  [:form {:action "/notes" :method "post"
          :class "mb-6"}
   [:label {:class "block mb-2 font-semibold"} "Note content:"]
   [:textarea {:id "note-text"
               :name "note-text"
               :required true
               :class "w-full h-32 border border-gray-500 rounded p-2 bg-[#2f2b3b] text-[#e0def2]"
               :placeholder "Write your note here..."}]
   [:button {:type "submit"
             :class "mt-4 bg-purple-600 text-white px-4 py-2 rounded hover:bg-purple-700"}
    "Create Note"]])

;; The dedicated page for creating a note
(defn new-note-page
  [request]
  (layout/page-layout
   request
   "Create a Note"
   [:div {:class "max-w-md mx-auto bg-[#2a2136] p-6 rounded shadow-md"}
    [:h1 {:class "text-3xl mb-4 font-bold"} "Create a New Note"]
    (note-form)]))

(defn edit-note-form
  "A form to edit an existing note. Pre-populates the text field."
  [note]
  [:form {:action (str "/notes/" (:id note) "/edit")
          :method "post"
          :class "mb-6"}
   [:label {:class "block mb-2 font-semibold"} "Edit note content:"]
   [:textarea {:id "note-text"
               :name "note-text"
               :required true
               :class "w-full h-32 border border-gray-500 rounded p-2 bg-[#2f2b3b] text-[#e0def2]"}
    (or (:text note) "")]

   [:button {:type "submit"
             :class "mt-4 bg-purple-600 text-white px-4 py-2 rounded hover:bg-purple-700"}
    "Update Note"]])

(defn edit-note-page
  [request note]
  (layout/page-layout
   request
   (str "Edit Note #" (:id note))
   [:div {:class "max-w-md mx-auto bg-[#2a2136] p-6 rounded shadow-md"}
    [:h1 {:class "text-3xl mb-4 font-bold"}
     (str "Edit Note #" (:id note))]
    (edit-note-form note)]))


(defn user-notes-list
  "A user-facing view of notes. If `:read-only? true`, we skip edit/delete links."
  [notes & {:keys [read-only?] :or {read-only? false}}]
  (if (empty? notes)
    [:p "No notes yet!"]
    [:div {:class "space-y-4 mt-4"}
     (for [{:keys [id text]} notes]
       [:div {:key   (str "note-" id)
              :class "border border-gray-600 rounded p-4 bg-[#2f2b3b]
                      flex justify-between items-start"}
        ;; The note text on the left
        [:div {:class "text-[#e0def2]"} text]

        ;; Only show Edit/Delete if not read-only
        (when-not read-only?
          [:div {:class "ml-4"}
           ;; Edit link
           [:a {:href (str "/notes/" id "/edit")
                :class "text-blue-400 hover:underline mr-2"}
            "✏️"]
           ;; Delete form
           [:form
            {:action   (str "/notes/" id "/delete")
             :method   "post"
             :class    "inline-block"
             :onsubmit "return confirm('Are you sure you want to delete this note?');"}
            [:button {:type "submit"
                      :class "text-red-400 hover:underline"}
             "🗑️"]]])])]))
