(ns slurp.core
  (:use slurp.utils)
  (:require [clojure.xml :as xml]
            [clojure.data.json :as json]))

(def twitter-api "https://api.twitter.com/1/")

(defn get-request
  "Example: Get the twitter followers of a user: (get-request \"users\" \"show\" \"screen_name\" \"cbp_\" \"xml\")
default format is json"
  ([resource subresource parameter value] (get-request resource subresource parameter value "json"))
  ([resource subresource parameter value format]
     (let [request-url (str twitter-api resource "/" subresource "." format "?" parameter "=" value)]
       (cond
         (= format "xml") (xml/parse request-url)
         (= format "json") (json/read-json (fetch-url request-url))
         :else (raise (str "Invalid format '" format "'."))))))


