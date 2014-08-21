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
  (testing "Empty specs don't validate"
    (are [example reason] (= (pr-str (s/check RequestSpec example))
                             (pr-str reason))
         [] '(not ("collection of one or more request specs" []))
         #{} '(not ("collection of one or more request specs" #{}))))
  (testing "Specs with embedded empty specs don't validate"
    (are [example reason] (= (pr-str (s/check RequestSpec example))
                             (pr-str reason))
      [#{} simple-http-request] ['(not ("collection of one or more request specs" #{})) nil]))
  (testing "Request specs with unknown/unsupported schemes don't validate"
    ;; Comparing string representations isn't great, but it's the best
    ;; easily available tool until maybe one day cddr/integrity's
    ;; humanize function is on Clojars + can humanize these errors :-)
    (are [example reason] (= (pr-str (s/check RequestSpec example))
                             (pr-str reason))
         simple-ftp-request {:target '(not ("supported-scheme?" "ftp://example.test"))})))
