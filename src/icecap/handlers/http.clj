(ns icecap.handlers.http
  (:require [clojure.core.async :refer [to-chan]]
            [icecap.handlers.core :refer [defstep]]
            [schema-contrib.core :refer [URI]]))

(defstep :http
  {:url URI}
  [step]
  (to-chan step))
