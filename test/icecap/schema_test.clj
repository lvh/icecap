(ns icecap.schema-test
  (:require [icecap.schema :refer :all]
            [clojure.test :refer :all]
            [schema.core :as s]))

(def simple-http-step {:target "http://example.test"})
(def simple-https-step {:target "https://example.test"})
(def simple-ftp-step {:target "ftp://example.test"})

(deftest PlanTests
  (testing "Correct plans validate"
    (are [example] (s/validate Plan example)
         simple-http-step
         simple-https-step
         #{simple-http-step simple-https-step}))
  (testing "Empty plans don't validate"
    (are [example reason] (= (pr-str (s/check Plan example))
                             (pr-str reason))
         [] '(not ("collection of one or more plans" []))
         #{} '(not ("collection of one or more plans" #{}))))
  (testing "Plans with empty in them don't validate"
    (are [example reason] (= (pr-str (s/check Plan example))
                             (pr-str reason))
      [#{} simple-http-step] ['(not ("collection of one or more plans" #{})) nil]))
  (testing "plans with unknown/unsupported schemes don't validate"))
(are [example reason] (= (pr-str (s/check Plan example))
                         (pr-str reason))
     simple-ftp-step {:target '(not ("supported-scheme?" "ftp://example.test"))})
