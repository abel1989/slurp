(ns slurp.core
  (:use slurp.utils)
  (:require [clojure.xml :as xml]
            [clojure.data.json :as json]))

(def twitter-api "https://api.twitter.com/1/")
(def twitter-stream "https://userstream.twitter.com/2/")
(def tweet-search "http://search.twitter.com/search")
(def user-search "http://api.twitter.com/1/users/search")

(defn get-data [url format]
  (cond
    (= format "xml") (xml/parse url)
    (= format "json") (json/read-json (fetch-url url))
    :else (raise (str "Invalid format '" format "'."))))

(defn api-request
  "Do a restful request on the twitter api.
Example: Get the twitter followers of a user: (api-request \"users\" \"show\" \"screen_name\" \"cbp_\" \"xml\")
default format is json"
  ([resource subresource parameter value] (api-request resource subresource parameter value "json"))
  ([resource subresource parameter value format]
     (let [request-url (str twitter-api resource "/" subresource "." format "?" parameter "=" value)]
       (get-data request-url format))))

(defn search-data
  "Do a search, params can either be a map of parameters -> values or just the search query.
search-url must be one of tweet-search, user-search or other valid search url.
Searches for tweets by default.
Returns the search as json or xml depending on format
Examples: (tweet-search \"clojure\"), (tweet-search {\"q\" \"clojure\"})"
  ([params] (search-data tweet-search "json" params))
  ([search-url params] (search-data search-url "json" params))
  ([search-url format params]
     (let [url (str search-url "." format)
           query-url
           (if (map? params)
             (let [q (str/replace-first
                      (str/join (map (fn [[k v]]
                                       (str "&" (url-encode k) "=" (url-encode v)))
                                     params))
                      "&" "?")]
               (str url q))
             (str url "?q=" (url-encode params)))]
       (get-data query-url format))))

(defn search [q]
  (let [tweet-data (:results (search-data q))
        user-data (search-data user-search q)
        tweet-top-results (map :text tweet-data)
        user-top-results (map :name user-data)]
    (println (str "Top tweets: " (str/join "\n" tweet-top-results)))
    (println (str "Top tweets: " (str/join "\n" user-top-results)))))