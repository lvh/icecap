(ns icecap.handlers.delay
  (:require [clojure.core.async :refer [timeout]]
            [icecap.handlers.core :refer [defstep]]
            [schema.core :as sc]))

(defstep :delay
  {:amount (sc/both sc/Int (sc/pred #(< 0 % 60)'(< 0 delay 60)))}
  [step]
  (timeout (:amount step)))
