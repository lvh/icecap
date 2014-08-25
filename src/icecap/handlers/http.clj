(ns icecap.handlers.http
  (:require [schema-contrib.core :refer [URI]]
            [icecap.handlers.core :refer [defstep]]
            [clojure.core.async :refer [to-chan]]))

(defstep :http
  {:url URI}
  (to-chan step))
