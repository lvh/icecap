(ns icecap.handlers.http-test
  (:require [clojure.core.async :as a]
            [manifold.deferred :as d]
            [icecap.handlers.core :refer [execute]]
            [icecap.schema :refer [check-plan]]
            [icecap.handlers.http]
            [aleph.http :as h]
            [clojure.test :refer :all]))

(def url
  "http://www.example.com")

(def step
  {:type :http :url url})

(deftest schema-tests
  (testing "valid steps"
    (are [s] (nil? (check-plan s))
         step))
  (testing "invalid steps"
    (are [s expected] (let [step (merge {:type :http} s)]
                        (= (check-plan step) expected))
         {} '{:url missing-required-key}
         {:uri url} '{:uri disallowed-key
                      :url missing-required-key}
         {:url 1} '{:url (throws? (URI 1))})))

(def fake-response
  ::my-fake-response)

(defn fake-request
  [_]
  (d/success-deferred fake-response))

(deftest execute-tests
  (with-redefs
    [h/request fake-request]
    (is (let [ch (execute step)
              [result] (a/<!! (a/into [] ch))]
          (= result fake-response)))))
