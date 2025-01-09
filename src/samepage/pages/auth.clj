(ns samepage.pages.auth
  (:require [samepage.pages.layout :as layout]))

(defn register-user-page
  "A form to register a new user."
  [request error-message]
  (layout/page-layout
   request
   "Register New User"
   [:div {:class "max-w-md mx-auto bg-[#2a2136] p-6 rounded shadow-md"}
    [:h1 {:class "text-3xl mb-4 font-bold"} "Register"]
    (when error-message
      [:div {:class "bg-red-600 text-white p-2 rounded mb-4"}
       error-message])
    [:form {:action "/register" :method "post"
            :class "space-y-4"}
     [:div
      [:label {:class "block font-semibold"} "Name:"]
      [:input {:type "text"
               :name "name"
               :class "w-full p-2 border border-gray-300 rounded bg-[#2f2b3b] text-[#e0def2]"
               :required true}]]
     [:div
      [:label {:class "block font-semibold"} "Email:"]
      [:input {:type "email"
               :name "email"
               :class "w-full p-2 border border-gray-300 rounded bg-[#2f2b3b] text-[#e0def2]"
               :required true}]]
     [:div
      [:label {:class "block font-semibold"} "Password:"]
      [:input {:type "password"
               :name "password"
               :class "w-full p-2 border border-gray-300 rounded bg-[#2f2b3b] text-[#e0def2]"
               :required true}]]
     [:button {:type "submit"
               :class "bg-purple-600 text-white py-2 px-4 rounded hover:bg-purple-700"}
      "Register"]]]))

(defn login-page
  "A form to log in an existing user."
  [request error-message]
  (layout/page-layout
   request
   "Log In"
   [:div {:class "max-w-md mx-auto bg-[#2a2136] p-6 rounded shadow-md"}
    [:h1 {:class "text-3xl mb-4 font-bold"} "Log In"]
    (when error-message
      [:div {:class "bg-red-600 text-white p-2 rounded mb-4"}
       error-message])
    [:form {:action "/login" :method "post"
            :class "space-y-4"}
     [:div
      [:label {:class "block font-semibold"} "Email:"]
      [:input {:type "email"
               :name "email"
               :class "w-full p-2 border border-gray-300 rounded bg-[#2f2b3b] text-[#e0def2]"
               :required true}]]
     [:div
      [:label {:class "block font-semibold"} "Password:"]
      [:input {:type "password"
               :name "password"
               :class "w-full p-2 border border-gray-300 rounded bg-[#2f2b3b] text-[#e0def2]"
               :required true}]]
     [:button {:type "submit"
               :class "bg-purple-600 text-white py-2 px-4 rounded hover:bg-purple-700"}
      "Log In"]]]))
