(ns icecap.store.mem-test
  (:require [icecap.store.core :refer :all]
            [icecap.store.mem :refer :all]
            [clojure.test :refer :all]))

(deftest MemStoreTests
  (testing "Data can be round-tripped through the store."
    (let [s (mem-store)]
      (create! s "index" "blob")
      (is (= (retrieve s "index")
             "blob")))))
