(ns icecap.schema
  (:require [schema.core :as s])
  (:import [java.net URI]))

(def supported-schemes #{"http" "https"})

(defn get-scheme
  [url]
  (.getScheme (URI. url)))

(def supported-scheme? (comp supported-schemes get-scheme))

(def SimpleRequest
  {:target (s/both s/Str
                   (s/pred supported-scheme? "supported-scheme?"))})

(def RequestSpec
  (s/either
   SimpleRequest
   [(s/recursive #'RequestSpec)]
   #{(s/recursive #'RequestSpec)}))
