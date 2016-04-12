(ns icecap.store.test-props
  (:require [caesium.util :refer [array-eq]]
            [icecap.store.api :refer [create! delete! retrieve]]
            [icecap.crypto :refer [index-bytes]]
            [icecap.test-props :refer [n-bytes]]
            [clojure.test.check.generators :as gen]
            [clojure.test.check.properties :as prop]))

(defn roundtrip-prop
  [store]
  (prop/for-all [index (n-bytes index-bytes)
                 blob gen/bytes]
                (do @(create! store index blob)
                    (array-eq @(retrieve store index) blob))))

(defn delete-prop
  [store]
  (prop/for-all [index (n-bytes index-bytes)
                 blob gen/bytes]
                (do @(create! store index blob)
                    @(delete! store index)
                    (nil? @(retrieve store index)))))
