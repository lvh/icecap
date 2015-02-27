(ns icecap.codec-test
  (:require [icecap.codec :refer :all]
            [clojure.test :refer :all])
  (:import (java.util Arrays)))

(deftest safebase64-tests
  (testing "safebase64 passes KATs, round-trips"
    (are [raw enc] (and (= (safebase64-encode raw) enc)
                        (Arrays/equals (safebase64-decode enc) raw))
      (.getBytes "The quick brown fox") "VGhlIHF1aWNrIGJyb3duIGZveA")))
