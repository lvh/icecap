(ns icecap.store.riak-test
  (:require [icecap.store.riak :refer :all]
            [icecap.store.test-props :refer :all]
            [clojure.test :refer :all]
            [clojure.test.check.clojure-test :refer [defspec]]
            [clojurewerkz.welle.buckets :as wb]
            [clojurewerkz.welle.core :as wc]))

(def riak-test-store)

(defn connect
  [f]
  (let [conn (wc/connect "http://localhost:8098/riak" "icecap-test")
        store (riak-store conn "test-bucket")]
    (try
      (wb/update conn "test-bucket" (bucket-props))
      (with-redefs-fn {#'riak-test-store store} f)
      (finally
        (wc/shutdown conn)))))

(use-fixtures :once connect)

(defspec ^:riak riak-store-roundtrip
  (roundtrip-prop riak-test-store))

(defspec ^:riak riak-store-delete
  (delete-prop riak-test-store))
