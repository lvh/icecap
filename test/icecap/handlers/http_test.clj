(ns icecap.handlers.http-test
  (:require [clojure.core.async :as a]
            [manifold.deferred :as d]
            [icecap.handlers.core :refer [execute]]
            [icecap.schema :refer [check-plan]]
            [icecap.handlers.http]
            [aleph.http :as h]
            [clojure.test :refer :all]))

(def step
  {:type :http :url "http://www.example.com"})

(deftest schema-tests
  (testing "valid steps"
    (are [x] (nil? (check-plan x))
         step)))

(def fake-response
  {})

(defn fake-request
  [_]
  (d/success-deferred fake-response))

(deftest execute-tests
  (with-redefs
    [h/request fake-request]
    (is (let [ch (execute step)
              [result] (a/<!! (a/into [] ch))]
          (= result fake-response)))))
