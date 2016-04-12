(ns icecap.handlers.http-test
  (:require [manifold.deferred :as md]
            [icecap.handlers.core :refer [execute]]
            [icecap.schema :refer [check-plan]]
            [icecap.handlers.http :refer [valid-scheme?]]
            [icecap.test-data :refer [simple-http-step simple-https-step]]
            [aleph.http :as h]
            [clojure.test :refer :all]
            [manifold.stream :as ms]))

(deftest schema-tests
  (testing "valid steps"
    (are [s] (nil? (check-plan s))
      simple-http-step
      (merge simple-http-step {:method :HEAD})
      (merge simple-http-step {:method :POST})
      (merge simple-http-step {:method :DELETE})
      (merge simple-http-step {:method :PUT})
      simple-https-step
      (merge simple-https-step {:method :HEAD})
      (merge simple-https-step {:method :POST})
      (merge simple-https-step {:method :DELETE})
      (merge simple-https-step {:method :PUT})))
  (testing "invalid steps"
    (are [s expected]  (= (check-plan (merge {:type :http} s))
                          expected)

      {}
      '{:url missing-required-key
        :method missing-required-key}

      {:uri (:url simple-http-step)}
      '{:uri disallowed-key
        :url missing-required-key
        :method missing-required-key}

      {:url 1}
      '{:url (not (valid-url? 1))
        :method missing-required-key}

      {:url (:url simple-http-step)
       :method :BOGUS}
      '{:method (not (#{:DELETE
                        :HEAD
                        :GET
                        :PATCH
                        :POST
                        :PUT}
                      :BOGUS))}

      {:url "bogus://example.test"
       :method :GET}
      '{:url (not (valid-scheme? a-java.lang.String))})))

(deftest valid-scheme?-tests
  (testing "valid schemes"
    (are [url] (valid-scheme? url)
      "http://example.test"
      "https://example.test"))
  (testing "invalid schemes"
    (are [url] (not (valid-scheme? url))
      "bogus://example.test"
      "ftp://example.test")))

(def fake-response
  ::my-fake-response)

(defn fake-request
  [_]
  (md/success-deferred fake-response))

(deftest execute-tests
  (with-redefs [h/request fake-request]
    (let [result (ms/stream->seq (execute simple-http-step))]
      (= result [fake-response]))))
