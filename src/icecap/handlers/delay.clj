(ns icecap.handlers.delay
  (:require [icecap.handlers.core :refer [defstep]]
            [integrity.number :refer [between]]
            [clojure.core.async :refer [timeout]]))

(defstep :delay
  {:amount (between 0 60)}
  (let [amount (:amount step)
        delay (timeout (:amount step))]
    delay))
