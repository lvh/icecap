(ns icecap.store.riak-test
  (:require [icecap.store.riak :refer :all]
            [icecap.store.test-props :refer :all]
            [clojure.test :refer :all]
            [clojure.test.check.clojure-test :refer [defspec]]
            [clojurewerkz.welle.buckets :as wb]
            [clojurewerkz.welle.core :as wc]))

(deftest bucket-props-test
  (testing "default"
    (is (= (bucket-props)
            {:n-val 3
             :r 1
             :w 2
             :dw 2

             :notfound-ok false
             :basic-quorum true

             :allow-mult false
             :last-write-wins true})))
  (testing "custom n"
    (is (= (bucket-props :n 2)
            {:n-val 2
             :r 1
             :w 1
             :dw 1

             :notfound-ok false
             :basic-quorum true

             :allow-mult false
             :last-write-wins true}))))

(def riak-test-store)

(defn connect
  [f]
  (let [conn (wc/connect "http://localhost:8098/riak")
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
