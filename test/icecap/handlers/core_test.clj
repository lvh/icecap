(ns icecap.handlers.core-test
  (:require [icecap.handlers.core :refer :all]
            [icecap.test-data :refer :all]
            [clojure.test :refer :all]
            [clojure.core.async :as a :refer [<!!]]))

(defn match-seq-spec
  "Matches a seq to a simple seq spec.

  Simple seq specs are seqs of colls. For each of those colls, this
  takes as many elements as that coll has off of the thing to match
  into a coll of the same type, and compares it to that coll.
  "
  [spec to-match]
  (loop [spec spec
         to-match to-match]
    (let [spec-part (first spec)
          [candidate to-match] (split-at (count spec-part) to-match)
          candidate (into (empty spec-part) candidate)]
      (cond (not= candidate spec-part) false
            (seq spec) (recur (rest spec) to-match)
            :else (not (seq to-match))))))

(deftest match-seq-spec-tests
  (testing "match single matching items"
    (are [spec to-match] (match-seq-spec spec to-match)
      [[1]] [1]
      [#{1}] [1]))
  (testing "don't match single nonmatching items"
    (are [spec to-match] (not (match-seq-spec spec to-match))
      [[1]] [2]
      [[2]] [1]
      [#{1}] [2]
      [#{2}] [1]))
  (testing "match empty items"
    (are [spec to-match] (match-seq-spec spec to-match)
      [[]] []
      [#{}] []))
  (testing "match complex specs"
    (are [spec to-match] (match-seq-spec spec to-match)
      [[1 2 3] #{4 5 6} [7 8 9]] [1 2 3 4 5 6 7 8 9]
      [[1 2 3] #{4 5 6} [7 8 9]] [1 2 3 6 5 4 7 8 9])))

(defn ^:private execution-order
  "Given a plan, returns the names of the steps in the order of which
  they would be executed."
  [plan]
  (map :name (<!! (a/into [] (execute plan)))))

(defn ^:private success-steps
  [names]
  (into (empty names) (map success-step names)))

(deftest execute-tests
  (testing "execute single step"
    (is (= (execution-order (success-step 1))
           [1])))
  (testing "execute some plans consisting of ordered steps"
    (let [names (vec (range 10))
          plan (success-steps names)
          order (execution-order plan)]
      (is (= order names))))
  (testing "execute some plans consisting of unordered steps"
    (let [names (set (range 10))
          plan (success-steps names)
          order (execution-order plan)
          spec [(into #{} names)]]
      (is (match-seq-spec spec order))))
  (testing "execute complex plans"
    (let [ordered-head (success-steps [1 2 3])
          unordered-middle (success-steps #{4 5 6})
          ordered-tail (success-steps [7 8 9])
          plan (into (conj ordered-head unordered-middle)
                     ordered-tail)
          spec [[1 2 3] #{4 5 6} [7 8 9]]]
      (is (match-seq-spec spec (execution-order plan))))))

(deftest defstep-tests
  (testing "literal schema"
    (try
      (defstep ::defstep-tests {} [step] nil)
      (is (::defstep-tests (methods get-schema)))
      (is (::defstep-tests (methods execute)))
      (finally
        (remove-method get-schema ::defstep-tests)
        (remove-method execute ::defstep-tests))))
  (testing "symbol schema"
    (try
      (let [schema {}]
        (defstep ::defstep-tests schema [step] nil))
      (is (::defstep-tests (methods get-schema)))
      (is (::defstep-tests (methods execute)))
      (finally
        (remove-method get-schema ::defstep-tests)
        (remove-method execute ::defstep-tests)))))
