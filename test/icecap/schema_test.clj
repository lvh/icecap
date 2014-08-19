(ns icecap.schema-test
  (:require [icecap.schema :refer :all]
            [clojure.test :refer :all]
            [schema.core :as s]))

(def simple-http-request {:target "http://example.test"})
(def simple-https-request {:target "https://example.test"})
(def simple-ftp-request {:target "ftp://example.test"})

(deftest RequestSpecTests
  (testing "Request specs matching the schema pass"
    (are [example] (s/validate RequestSpec example)
         simple-http-request
         simple-https-request
         #{simple-http-request simple-https-request}))
  (testing "Request specs with unknown/unsupported schemes don't pass"
    (are [example] (thrown? Throwable (s/validate RequestSpec example))
         simple-ftp-request)))
