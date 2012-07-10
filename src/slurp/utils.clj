(ns slurp.utils
  (:require [clojure.string :as str]
            [clj-time.core  :as time]
            [clj-time.coerce :as timec])
  (:import (javax.crypto Mac)
           (javax.crypto.spec SecretKeySpec)))

(defn raise
  [error-msg]
  (throw (Exception. error-msg)))

(defn url-encode
  [s]
  (let [unreserved #"[^A-Za-z0-9_~.+-]+"
        s (if (keyword? s) (name s) (str s))]
    (str/replace s unreserved
                 (fn [c]
                   (str/join
                    (map (partial format "%%%02X")
                         (.getBytes c "UTF-8")))))))

(defn fetch-url [url]
  (with-open [stream (.openStream (java.net.URL. url))]
    (let [buf (java.io.BufferedReader.
               (java.io.InputStreamReader. stream))]
      (apply str (line-seq buf)))))

(defn unix-time []
  (long
   (/ (timec/to-long (time/now))
      1000)))

(defn hmac 
  "Calculate HMAC signature for given data."
  [^String key ^String data]
  (let [hmac-sha1 "HmacSHA1"
        signing-key (SecretKeySpec. (.getBytes key) hmac-sha1)
        mac (doto (Mac/getInstance hmac-sha1) (.init signing-key))]
    (String. (org.apache.commons.codec.binary.Base64/encodeBase64 
              (.doFinal mac (.getBytes data)))
             "UTF-8")))