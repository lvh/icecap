(ns icecap.store.mem-test
  (:require [icecap.store.mem :as mem]
            [icecap.store.test-props :as props]
            [clojure.test.check.clojure-test :refer [defspec]]))

(defspec mem-store-roundtrip
  (props/roundtrip-prop (mem/mem-store)))

(defspec mem-store-delete
  (props/delete-prop (mem/mem-store)))
