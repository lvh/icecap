(ns icecap.schema-test
  (:require [icecap.schema :as is]
            [icecap.test-data :refer :all]
            [clojure.test :refer :all]
            [schema.core :as s]
            [icecap.handlers.core :refer [get-schema]]))

(deftest plan-tests
  (testing "correct plans validate"
    (are [example] (nil? (is/check-plan example))
      simple-http-step
      simple-https-step
      #{simple-http-step simple-https-step}))
  (testing "empty plans don't validate"
    (are [example reason] (= (check-plan example)
                             reason)
      [] '(not ("collection of two or more plans" []))
      #{} '(not ("collection of two or more plans" #{}))))
  (testing "plans with one step in them don't validate"
    (are [example reason] (= (check-plan example)
                             reason)
      [simple-http-step] '(not ("collection of two or more plans"
                                a-clojure.lang.PersistentVector))
      #{simple-http-step} '(not ("collection of two or more plans"
                                 a-clojure.lang.PersistentHashSet))))
  (testing "plans with empty steps in them don't validate"
    (are [example reason] (= (check-plan example)
                             reason)
      [#{} simple-http-step] ['(not ("collection of two or more plans" #{}))
                              nil]))
  (testing "plans with unsupported steps don't validate, with useful error"
    (let [supported-types (into #{} (keys (methods get-schema)))]
      (are [example] (let [[_ [suggested actual]] (:type (check-plan example))]
                       (and (= actual (:type example))
                            (= suggested supported-types)))
        simple-ftp-step))))
