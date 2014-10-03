(ns icecap.handlers.delay
  (:require [clojure.core.async :refer [timeout]]
            [icecap.handlers.core :refer [defstep]]
            [integrity.number :refer [between]]))

(defstep :delay
  {:amount (between 0 60)}
  [step]
  (timeout (:amount step)))
