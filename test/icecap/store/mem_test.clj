(ns icecap.store.mem-test
  (:require [caesium.crypto.util :refer [array-eq]]
            [icecap.store.api :refer :all]
            [icecap.store.mem :refer :all]
            [icecap.store.test-props :refer :all]
            [clojure.test :refer :all]
            [clojure.core.async :as a :refer [<!!]]
            [clojure.test.check.clojure-test :refer [defspec]]))

(defspec mem-store-roundtrip
  (store-roundtrip-prop (mem-store)))

(defspec mem-store-delete
  (store-delete-prop (mem-store)))
