(ns icecap.handlers.http
  (:require [schema.core :as s]
            [aleph.http :refer [request]]
            [clojure.core.async :refer [to-chan]]
            [icecap.handlers.core :refer [defstep]]
            [manifold.stream :refer [connect]]
            [schema-contrib.core :as sc]
            [taoensso.timbre :refer [info spy]])
  (:import [java.net URI]))


(defn scheme
  "Get the scheme of a (string) URL."
  [^String s]
  (.getScheme (URI. s)))

(defn valid-scheme?
  "Checks if the (string) URL has a valid scheme."
  [^String s]
  (#{"http" "https"} (scheme s)))

(defstep :http
  {:url (s/conditional valid-scheme? sc/URI)
   :method (s/enum :GET :POST :DELETE :PUT :PATCH :HEAD)}
  [step]
  (let [req (spy (request step))]
    (to-chan [(spy @req)])))
