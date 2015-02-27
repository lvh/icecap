(ns icecap.store.riak-test
  (:require [icecap.store.riak :refer :all]
            [icecap.store.test-props :refer :all]
            [clojure.test :refer :all]
            [clojure.test.check.clojure-test :refer [defspec]]
            [clojurewerkz.welle.kv :as kv]))

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

(def ^:private store-result
  {:vclock nil,
   :has-siblings? false,
   :has-value? false,
   :deleted? false,
   :modified? true,
   :result ()})

(defn ^:private fetch-one-result
  [v]
  {:vclock nil
   :has-siblings? false,
   :has-value? true,
   :deleted? false,
   :modified? true,
   :result (when v
             {:vtag "\"AAAAAAAAAAAAAAAAAAAAAA\""
              :value v,
              :deleted? false,
              :last-modified #inst "1989-02-07T02:37:29.000-00:00",
              :vclock nil
              :content-type "application/octet-stream",
              :indexes {},
              :metadata {},
              :links ()})})

(def ^:private fake-riak-conn
  (Object.))

(defn ^:private fake-riak-redefs
  "Builds redefs for a fake Riak, for testing purposes.

  Returns a map of vars of the original functions to their test double
  implementations."
  []
  (let [data (atom {})]
    {#'kv/store (fn [conn bucket index blob]
                  (assert (= conn fake-riak-conn))
                  (swap! data assoc-in [bucket index] blob)
                  store-result)
     #'kv/fetch-one (fn [conn bucket index]
                      (assert (= conn fake-riak-conn))
                      (let [res (get-in @data [bucket index])]
                        (fetch-one-result res)))
     #'kv/delete (fn [conn bucket index]
                   (assert (= conn fake-riak-conn))
                   (swap! data dissoc bucket index)
                   nil)}))

(use-fixtures :once
  (partial with-redefs-fn (fake-riak-redefs)))

(def ^:private riak-test-store
  (riak-store fake-riak-conn "test-bucket"))

(defspec ^:riak riak-store-roundtrip
  (roundtrip-prop riak-test-store))

(defspec ^:riak riak-store-delete
  (delete-prop riak-test-store))
