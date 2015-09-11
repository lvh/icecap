(ns icecap.handlers.delay
  (:require [clojure.core.async :refer [timeout]]
            [icecap.handlers.core :refer [defstep]]
            [schema.core :as sc]))

(defstep :delay
  {:amount (sc/conditional #(< 0 % 60) sc/Int)}
  [step]
  (timeout (:amount step)))
