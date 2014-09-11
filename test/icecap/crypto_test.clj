(ns icecap.crypto-test
  (:refer-clojure :exclude [derive])
  (:require [icecap.crypto :refer :all]
            [caesium.crypto.util :refer [array-eq]]
            [clojure.test :refer :all]
            [clojure.test.check.generators :as gen]
            [clojure.test.check.properties :as prop]
            [clojure.test.check.clojure-test :refer [defspec]]))

(defn n-bytes
  [n]
  (gen/fmap byte-array (gen/vector gen/byte n)))

(defn scheme-roundtrip-prop
  [scheme key-length]
  (prop/for-all [key (n-bytes key-length)
                 message gen/bytes]
                (->> message
                     (encrypt scheme key)
                     (decrypt scheme key)
                     (array-eq message))))

(defspec secretbox-scheme-roundtrip
  (scheme-roundtrip-prop (secretbox-scheme) 32))
(defspec bogus-scheme-roundtrip
  (scheme-roundtrip-prop (bogus-scheme) 16))
