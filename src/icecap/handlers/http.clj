(ns icecap.handlers.http
  (:require [schema.core :as s]
            [aleph.http :refer [request]]
            [clojure.core.async :refer [to-chan]]
            [icecap.handlers.core :refer [defstep]]
            [manifold.stream :refer [connect]]
            [schema-contrib.core :as sc]
            [taoensso.timbre :refer [info spy]])
  (:import [java.net URI]))

(defn valid-scheme?
  "Checks if the given URL has a valid scheme."
  [^String url]
  (#{"http" "https"} (.getScheme (URI. url))))

(defstep :http
  {:url sc/URI
   :method (s/enum :GET :POST :DELETE :PUT :PATCH :HEAD)}
  [step]
  (let [req (spy (request step))]
    (to-chan [(spy @req)])))
