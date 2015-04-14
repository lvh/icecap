(ns icecap.handlers.http
  (:require [aleph.http :refer [request]]
            [clojure.core.async :refer [to-chan]]
            [icecap.handlers.core :refer [defstep]]
            [manifold.stream :refer [connect]]
            [schema-contrib.core :refer [URI]]
            [taoensso.timbre :refer [info spy]]))

(defstep :http
  {:url URI}
  [step]
  (let [args (spy (select-keys step [:url]))
        req (spy (request args))
        res (spy @req)]
    (to-chan [res])))
