(ns icecap.store.mem-test
  (:require [caesium.crypto.util :refer [array-eq]]
            [icecap.store.mem :refer [mem-store]]
            [icecap.store.test-props :refer :all]
            [clojure.test :refer :all]
            [clojure.test.check.clojure-test :refer [defspec]]))

(defspec mem-store-roundtrip
  (roundtrip-prop (mem-store)))

(defspec mem-store-delete
  (delete-prop (mem-store)))
