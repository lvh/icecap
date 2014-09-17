(ns icecap.test-props
  (:require [clojure.test.check.generators :as gen]))

(defn n-bytes
  [n]
  (gen/fmap byte-array (gen/vector gen/byte n)))
