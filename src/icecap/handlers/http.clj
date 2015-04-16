(ns icecap.handlers.http
  (:require [schema.core :as s]
            [aleph.http :refer [request]]
            [clojure.core.async :refer [to-chan]]
            [icecap.handlers.core :refer [defstep]]
            [manifold.stream :refer [connect]]
            [schema-contrib.core :refer [URI]]
            [taoensso.timbre :refer [info spy]]))

(defstep :http
  {:url URI
   :method (s/enum :GET :POST :DELETE :PUT :PATCH :HEAD)}
  [step]
  (let [req (spy (request step))]
    (to-chan [(spy @req)])))
