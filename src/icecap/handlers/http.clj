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
  "Get the scheme of a URL."
  [^String url]
  (.getScheme (URI. url)))

(defn valid-scheme?
  "Checks if the given URL has a valid scheme."
  [^String url]
  (#{"http" "https"} (scheme url)))

(defstep :http
  {:url (s/both sc/URI
                (s/pred valid-scheme? 'valid-scheme?))
   :method (s/enum :GET :POST :DELETE :PUT :PATCH :HEAD)}
  [step]
  (let [req (spy (request step))]
    (to-chan [(spy @req)])))
