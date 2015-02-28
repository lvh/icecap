(ns icecap.handlers.http-test
  (:require [clojure.core.async :as a]
            [manifold.deferred :as d]
            [icecap.handlers.core :refer [execute]]
            [icecap.handlers.http]
            [aleph.http :as h]
            [clojure.test :refer :all]))

(def step
  {:type :http :url "http://www.example.com"})

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
