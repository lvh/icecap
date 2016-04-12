(ns icecap.handlers.core-test
  (:require [icecap.handlers.core :as hc]
            [icecap.test-data :as td]
            [clojure.test :refer [deftest is are testing]]
            [manifold.stream :as ms]))

(defn match-seq-spec
  "Matches a seq to a simple seq spec.

  Simple seq specs are seqs of colls. For each of those colls, this
  takes as many elements as that coll has off of the thing to match
  into a coll of the same type, and compares it to that coll.
  "
  [[part & parts] to-match]
  (let [[candidate to-match] (split-at (count part) to-match)
        candidate (into (empty part) candidate)]
    (cond (not= candidate part) false
          (seq parts) (recur parts to-match)
          :else (not (seq to-match)))))

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
  (->> (hc/execute* plan) (ms/stream->seq) (map :name)))

(defn ^:private success-steps
  [names]
  (into (empty names) (map td/success-step names)))

(defn matches-spec
  "Asserts that the plan matches this execution spec.

  This fn asserts; it is not a predicate. It also first compares the
  execution order to the contents of the spec.

  "
  [plan spec]
  (let [order (execution-order plan)]
    (is (= (frequencies (apply concat spec)) (frequencies order)))
    (is (match-seq-spec spec order))))

(deftest execute-tests
  (testing "execute single step"
    (matches-spec (td/success-step 1) [[1]]))
  (testing "execute some plans consisting of ordered steps"
    (let [names (vec (range 10))]
      (matches-spec (success-steps names) [names])))
  (testing "execute some plans consisting of unordered steps"
    (let [names (set (range 10))]
      (matches-spec (success-steps names) [(set names)])))
  (testing "execute complex plans"
    (let [ordered-head (success-steps [1 2 3])
          unordered-middle (success-steps #{4 5 6})
          ordered-tail (success-steps [7 8 9])
          plan (vec (concat ordered-head [unordered-middle] ordered-tail))
          spec [[1 2 3] #{4 5 6} [7 8 9]]]
      (matches-spec plan spec))))

(deftest defstep-tests
  (testing "literal schema"
    (try
      (hc/defstep ::defstep-tests {} [step] nil)
      (is (::defstep-tests (methods hc/get-schema)))
      (is (::defstep-tests (methods hc/execute*)))
      (finally
        (remove-method hc/get-schema ::defstep-tests)
        (remove-method hc/execute* ::defstep-tests))))
  (testing "symbol schema"
    (try
      (let [schema {}]
        (hc/defstep ::defstep-tests schema [step] nil))
      (is (::defstep-tests (methods hc/get-schema)))
      (is (::defstep-tests (methods hc/execute*)))
      (finally
        (remove-method hc/get-schema ::defstep-tests)
        (remove-method hc/execute* ::defstep-tests)))))
