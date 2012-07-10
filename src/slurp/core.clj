(ns slurp.core
  (:use slurp.utils)
  (:require [clojure.xml :as xml]
            [clojure.data.json :as json]
            [clj-http.client :as client]
            [clojure.string :as str]))

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
Examples: (tweet-search \"clojure\"), (tweet-search {:q \"clojure\"})"
  ([params] (search-data tweet-search "json" params))
  ([search-url params] (search-data search-url "json" params))
  ([search-url format params]
     (let [url (str search-url "." format)
           query-url
           (if (map? params)
             (let [q (str "?"
                      (str/join "&"
                                (map (fn [[k v]]
                                       (str (url-encode k) "=" (url-encode v)))
                                     params)))]
               (str url q))
             (str url "?q=" (url-encode params)))]
       (get-data query-url format))))

(defn search [q]
  (letfn [(search- [next-page acc times]
            (let [url tweet-search
                  data (get-data (str tweet-search ".json" next-page) "json")]
              (if (zero? times)
                acc
                (search- (:next_page data) (into acc (:results data)) (dec times)))))]
    (search- (str "?q=" (url-encode q)) [] 10)))

(def oauth-path "extra/oauth.clj")
(def search-users-request "GET")
(def search-users-url "https://api.twitter.com/1/users/search.json")

(defn gen-nonce []
  "Generates a random string"
  (let [length (+ 16 (rand-int 48))
        chars (vec
               (concat (range 10)
                       (map char (range 97 123))
                       (map char (range 65 91))))
        cnt (count chars)]
    (apply str (repeatedly length #(chars (rand-int cnt))))))

(defn oauth-signature [f]
  (let [oauth (read-string (slurp f))
        oauth-map (:oauth-params oauth)
        signin-key (str (url-encode (:consumer-secret oauth)) \&
                        (url-encode (:oauth-token-secret oauth)))
        param-string
        (str/join "&"
         (map (fn [[k v]]
                (str (url-encode
                      (str/replace (name k) "-" "_"))
                     "=" (url-encode v)))
              (into (sorted-map) oauth-map)))
        sig-basestring
        (str search-users-request \&
             (url-encode search-users-url) \&
             (url-encode param-string))]
    (println sig-basestring)
    (hmac signin-key sig-basestring)))

(defn oauth-authorization [f]
  (let [oauth-initial (merge (:oauth-params
                              (read-string (slurp f)))
                             {:oauth-nonce (gen-nonce)
                              :oauth-timestamp (unix-time)
                              :oauth-signature "dexEFvz4dbScJFgMjPRhe%2FwJbWY%3D"})
        oauth-sorted (into (sorted-map) oauth-initial)]
    (str "Oauth "
     (str/join ", "
      (map (fn [[k v]]
             (str (str/replace (name k) "-" "_")
                  "=\""  (url-encode v) "\""))
           oauth-sorted)))))

(defn search-users
  "Authenticates a GET request (used to search users)"
  [q]
  (let [oauth (read-string (slurp oauth-path))]
    (client/get
     (str search-users-url "?q="
          (url-encode q))
     {:headers
      {"Authorization"
       "OAuth oauth_consumer_key=\"N33HM0iaLX1POBV7edBrQ\", oauth_nonce=\"279172cb4d34c0617738212a15d46512\", oauth_signature=\"sDvkXqtcoyaJOekiceakDDIl%2Fco%3D\", oauth_signature_method=\"HMAC-SHA1\", oauth_timestamp=\"1341929342\", oauth_token=\"492327220-lsy7mwNA7w70RnMoagStePS9ehCOwaHTZL8OMSix\", oauth_version=\"1.0\""}})))