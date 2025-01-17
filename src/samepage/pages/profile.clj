(ns samepage.pages.profile
  (:require [samepage.pages.layout :as layout]))

(defn profile-page
  [request user-record]
  (layout/page-layout
   request
   "Your Profile"
   [:div {:class "max-w-md mx-auto bg-[#2a2136] p-6 rounded shadow-md"}
    [:h1 {:class "text-3xl mb-4 font-bold"} "Update Your Profile"]

    ;; Show profile pic if present, else "No Profile Picture"
    (if (seq (str (:profile_pic user-record)))
      [:div {:class "mb-4"}
       [:img {:src  (:profile_pic user-record)
              :alt  "Profile Picture"
              :class "h-40 w-auto object-cover border border-gray-500"}]]
      [:div {:class "mb-4 bg-[#1e1e28] border border-gray-500 h-40 w-40
                     flex items-center justify-center text-gray-300"}
       "No Profile Picture"])

    ;; The form
    [:form {:action "/profile" :method "post"
            :class "space-y-4"}
     [:div
      [:label {:class "block font-semibold"} "Display Name:"]
      [:input {:type "text"
               :name "display_name"
               :value (or (:display_name user-record) "")
               :class "w-full p-2 border border-gray-300 rounded
                       bg-[#2f2b3b] text-[#e0def2]"}]]
     [:div
      [:label {:class "block font-semibold"} "Profile Picture URL:"]
      [:input {:type "url"
               :name "profile_pic"
               :value (or (:profile_pic user-record) "")
               :class "w-full p-2 border border-gray-300 rounded
                       bg-[#2f2b3b] text-[#e0def2]"}]]

     [:button {:type "submit"
               :class "mt-2 bg-purple-600 text-white px-4 py-2
                       rounded hover:bg-purple-700"}
      "Update Profile"]]]))
