(ns icecap.handlers.http
  (:require [aleph.http :refer [request]]
            [clojure.core.async :refer [chan]]
            [icecap.handlers.core :refer [defstep]]
            [manifold.stream :refer [connect]]
            [schema-contrib.core :refer [URI]]))

(defstep :http
  {:url URI}
  [step]
  (let [ch (chan)]
    (connect (request {:url "http://www.rackspace.com"}) ch)))
