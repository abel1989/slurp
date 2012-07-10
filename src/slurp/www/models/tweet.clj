(ns slurp.www.models.tweet
  (:use somnium.congomongo)
  (:require [slurp.www.models.utils :as db]
            [clojure.data.json :as json]
            [clojure.string :as str]))

;;; Playing around

(def tweet-dir "src/slurp/www/models/")
(def tweet-coll :tweets)

(defn get-tweets [fname]
  "gets the tweets as maps of user_name, text"
  (map (fn [t]
         {:text (:text t)
          :from (:from_user t)})
       (:results (json/read-json (slurp (str tweet-dir fname))))))

(defn insert-tweets! [tweet-map]
  (insert! tweet-coll tweet-map))

(defn main [fname]
  (db/maybe-init)
  (map insert-tweets! (get-tweets fname)))

(defn print-tweets []
  (doseq [doc (fetch tweet-coll)]
    (println (:from doc) ": " (:text doc))))

(defn all-tweets []
  (db/maybe-init)
  (fetch tweet-coll))