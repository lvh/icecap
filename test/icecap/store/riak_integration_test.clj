(ns ^:riak icecap.store.riak-integration-test
  (:require [icecap.store.riak :as riak]
            [icecap.store.test-props :as props]
            [clojure.test.check.clojure-test :refer [defspec]]
            [clojure.test :refer [use-fixtures]]
            [clojurewerkz.welle.buckets :as wb]
            [clojurewerkz.welle.core :as wc]))

(def ^:private riak-test-url "http://localhost:8098/riak")
(def ^:private riak-test-store)

(defn connect
  [f]
  (let [conn (wc/connect riak-test-url)
        store (riak/riak-store conn "test-bucket")]
    (try
      (wb/update conn "test-bucket" (riak/bucket-props))
      (with-redefs-fn {#'riak-test-store store} f)
      (finally
        (wc/shutdown conn)))))

(use-fixtures :once connect)

(defspec riak-store-roundtrip
  (props/roundtrip-prop riak-test-store))

(defspec riak-store-delete
  (props/delete-prop riak-test-store))
