(ns icecap.handlers.http
  (:require [aleph.http :refer [request]]
            [icecap.handlers.core :refer [defstep]]
            [schema-contrib.core :as sc]
            [schema.core :as s])
  (:import (java.net URI)))


(defn valid-url?
  [^String s]
  (try
    (URI. s) true

    (catch Exception _
      false)))

(defn scheme
  "Get the scheme of a (string) URL."
  [^String s]
  (.getScheme (URI. s)))

(defn valid-scheme?
  "Checks if the (string) URL has a valid scheme."
  [^String s]
  (#{"http" "https"} (scheme s)))

(defstep :http
  {:url (s/conditional
         valid-url? (s/conditional
                     valid-scheme? sc/URI
                     'valid-scheme?)
         'valid-url?)
   :method (s/enum :GET :POST :DELETE :PUT :PATCH :HEAD)}
  [step]
  (request step))
