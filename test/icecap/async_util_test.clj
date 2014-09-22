(ns icecap.async-util-test
  (:require [clojure.core.async :refer [<!! go] :as a]
            [icecap.async-util :refer :all]
            [clojure.test :refer :all]))

(defn ^:private assert-false?
  "Checks if this is an exception matching the one raised when
  evaluating `(assert false)`."
  [e]
  (and (instance? AssertionError e)
       (= (.getMessage e) "Assert failed: false")))

(defmacro ^:private has-exc
  "Checks if the given chan has a failed assertion on it, and then
  closes."
  [chan-expr]
  `(is (let [c# ~chan-expr
             exception# (<!! c#)
             next-val# (<!! c#)]
         (and (assert-false? exception#)
              (nil? next-val#)))))

(deftest go-catch-tests
  (testing "returns exceptions"
    (has-exc (go-catch
              (assert false))))
  (testing "supports multiple forms"
    (has-exc (go-catch
              (+ 1 2 3)
              (assert false)))))

(deftest <!!?-tests
  (testing "take a value"
    (is (= (<!!? (go 1 2 3))
           3)))
  (testing "raise an exc"
    (is (thrown-with-msg? AssertionError #"Assert failed: false"
                          (<!!? (go-catch (assert false)))))))

(deftest <!?-tests
  (testing "take a value"
    (let [inner-chan (a/to-chan [1 2 3])
          outer-chan (go (<!? inner-chan))]
      (is (= (<!! outer-chan)
             1))))
  (testing "raise an exc"
    (let [err-chan (go-catch (assert false))
          <!?-chan (go-catch (<!? err-chan))]
      (is (assert-false? (<!! <!?-chan))))))
