(ns slurp.www.views.common
  (:use noir.core
        [hiccup.page :only [html5 include-css]]))


;;; Head includes here
(def includes
  {
   :base-css (include-css "/css/base.css")
   })

(defpartial head [incls title]
  [:head
   [:meta {:charset "UTF-8"}]
   [:title (if (seq title) (str title " | Lonja Mercantil La Niña")
               "Lonja Mercantil La Niña")]
   (map #(get includes %) incls)])

(defpartial main-layout [content]
  (html5 {:lang "es-MX"}
    (head [] "Slurp!")
    [:body
     [:h1 "Tweets!"]
     (:content content)]))