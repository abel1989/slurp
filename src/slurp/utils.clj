(ns slurp.utils
  (:require [clojure.string :as str]))

(defn raise
  [error-msg]
  (throw (Exception. error-msg)))

(defn url-encode
  [s]
  (let [reserved #"[!*'();:@&=+$,/?#\[\]]"]
    (str/replace s reserved
                 (fn [c]
                   (str/join
                    (map (partial format "%%%02X")
                         (.getBytes c "UTF-8")))))))

(defn fetch-url [url]
  (with-open [stream (.openStream (java.net.URL. url))]
    (let [buf (java.io.BufferedReader.
               (java.io.InputStreamReader. stream))]
      (apply str (line-seq buf)))))
