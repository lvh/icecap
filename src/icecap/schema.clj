(ns icecap.schema
  (:require [schema.core :as s])
  (:import [java.net URI]))

(def supported-schemes
  "The currently supported schemes."
  #{"http" "https"})

(defn get-scheme
  "Gets the scheme of a (String) URL."
  [url]
  (.getScheme (URI. url)))

(def supported-scheme?
  "Does the given URL have a supported scheme?"
  (comp supported-schemes get-scheme))

(def SimpleRequest
  "A simple request."
  {:target (s/both s/Str
                   (s/pred supported-scheme? "supported-scheme?"))})

(def RequestSpec
  (s/either
   SimpleRequest
   [(s/recursive #'RequestSpec)]
   #{(s/recursive #'RequestSpec)}))
