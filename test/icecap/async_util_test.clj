(ns icecap.async-util
  (:require [clojure.core.async :refer [<!! go] :as a]
            [icecap.async-util :refer :all]
            [clojure.test :refer :all]))

(defmacro ^:private has-exc
  "Checks if the given chan has a failed assertion on it, and then closes."
  [c]
  `(is
    (let [e# (<!! ~c)
          n# (<!! ~c)]
      (prn e#)
      (prn n#)
      (and (instance? AssertionError e#)
           (= (.getMessage e#)
              "Assert failed: false")           ))))

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
    (is (= (<!!? (go 1))
           1)))
  (testing "raise an exc"
    (is (thrown-with-msg? AssertionError #"Assert failed: false"
                          (<!!? (go-catch (assert false)))))))
