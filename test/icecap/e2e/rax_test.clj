(ns icecap.e2e.rax-test
  "End-to-end tests for Rackspace APIs, mocked by Mimic."
  (:require [icecap.e2e.common :refer :all]
            [clojure.test :refer :all]))

(def mimic-url
  "http://localhost:8900")

(deftest nova-tests
  (testing "server creation and deletion"))
