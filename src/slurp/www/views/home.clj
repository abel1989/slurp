(ns slurp.www.views.home
  (:use noir.core)
  (:require [noir.response :as resp]))

(defpage "/" []
  (resp/redirect "/welcome/"))

(defpage "/welcome/" []
  "Welcome to Slurp!")