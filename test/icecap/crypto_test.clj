(ns icecap.crypto-test
  (:refer-clojure :exclude [derive])
  (:require [icecap.crypto :refer :all]
            [icecap.test-props :refer [n-bytes]]
            [caesium.byte-bufs :as bb]
            [clojure.test :refer :all]
            [clojure.test.check.properties :as prop]
            [clojure.test.check.generators :as gen]
            [clojure.test.check.clojure-test :refer [defspec]]))

(defn scheme-roundtrip-prop
  [scheme key-length]
  (prop/for-all [key (n-bytes key-length)
                 message gen/bytes]
                (->> message
                     (encrypt scheme key)
                     (decrypt scheme key)
                     (bb/bytes= message))))

(defspec secretbox-scheme-roundtrip
  (scheme-roundtrip-prop (secretbox-scheme) 32))
