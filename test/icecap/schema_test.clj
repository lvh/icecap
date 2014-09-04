(ns icecap.schema-test
  (:require [icecap.schema :refer :all]
            [icecap.test-data :refer :all]
            [clojure.test :refer :all]
            [schema.core :as s]))

(deftest PlanTests
  (testing "correct plans validate"
    (are [example] (s/validate Plan example)
         simple-http-step
         simple-https-step
         #{simple-http-step simple-https-step}))
  (testing "empty plans don't validate"
    (are [example reason] (= (pr-str (s/check Plan example))
                             (pr-str reason))
         [] '(not ("collection of one or more plans" []))
         #{} '(not ("collection of one or more plans" #{}))))
  (testing "plans with empty steps in them don't validate"
    (are [example reason] (= (pr-str (s/check Plan example))
                             (pr-str reason))
      [#{} simple-http-step] ['(not ("collection of one or more plans" #{})) nil]))
  (testing "plans with unsupported steps don't validate"
    (are [example reason] (= (pr-str (s/check Plan example))
                             (pr-str reason))
         simple-ftp-step '(not ("supported-step-type" a-clojure.lang.PersistentHashMap)))))
