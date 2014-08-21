(ns icecap.schema-test
  (:require [icecap.schema :refer :all]
            [clojure.test :refer :all]
            [schema.core :as s]))

(def simple-http-action {:target "http://example.test"})
(def simple-https-action {:target "https://example.test"})
(def simple-ftp-action {:target "ftp://example.test"})

(deftest PlanTests
  (testing "Correct plans validate"
    (are [example] (s/validate Plan example)
         simple-http-action
         simple-https-action
         #{simple-http-action simple-https-action}))
  (testing "Empty plans don't validate"
    (are [example reason] (= (pr-str (s/check Plan example))
                             (pr-str reason))
         [] '(not ("collection of one or more plans" []))
         #{} '(not ("collection of one or more plans" #{}))))
  (testing "Plans with empty in them don't validate"
    (are [example reason] (= (pr-str (s/check Plan example))
                             (pr-str reason))
      [#{} simple-http-action] ['(not ("collection of one or more plans" #{})) nil]))
  (testing "plans with unknown/unsupported schemes don't validate"
    ;; Comparing string representations isn't great, but it's the best
    ;; easily available tool until maybe one day cddr/integrity's
    ;; humanize function is on Clojars + can humanize these errors :-)
    (are [example reason] (= (pr-str (s/check Plan example))
                             (pr-str reason))
         simple-ftp-action {:target '(not ("supported-scheme?" "ftp://example.test"))})))
