(ns slurp.www.views.tweet
  (:use noir.core
        hiccup.form
        [slurp.core :only [search]]
        [slurp.www.models.tweet :only [all-tweets]]
        [slurp.www.views.common :only [main-layout]])
  (:require [noir.response :as resp]))

(defpartial tweet-row [tweet-map]
  [:tr.tweet-row
   [:td (:from_user_name tweet-map)]
   [:td (:text tweet-map)]])

(defpage "/" []
  (resp/redirect "/tweets/"))

(defpartial search-tweet []
  (form-to {:id "search-tweet"} [:post "/tweets/"]
    (text-field "query")
    (submit-button "Buscar")))

(defpage "/tweets/" []
  (search-tweet))

(defpage [:post "/tweets/"] {:as query}
  (let [content {:content [:table.tweets {:border 1} [:tr [:th "Nombre"] [:th "Texto"]]
                           (map tweet-row (search (:query query)))]}]
    (main-layout content)))