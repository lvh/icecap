(ns icecap.schema-test
  (:require [icecap.schema :refer :all]
            [clojure.test :refer :all]
            [schema.core :as s]))

(def simple-http-request {:target "http://example.test"})
(def simple-https-request {:target "https://example.test"})
(def simple-ftp-request {:target "ftp://example.test"})

(deftest RequestSpecTests
  (testing "Correct request specs validate"
    (are [example] (s/validate RequestSpec example)
         simple-http-request
         simple-https-request
         #{simple-http-request simple-https-request}))
  (testing "Empty collections don't validate"
    (are [example] (some? (s/check RequestSpec example))
         []
         #{}))
  (testing "Request specs with unknown/unsupported schemes don't validate"
    (are [example reason] (= (s/check RequestSpec example) reason)
         simple-ftp-request {:target (not ("supported-scheme?" "ftp://example.test"))})))
