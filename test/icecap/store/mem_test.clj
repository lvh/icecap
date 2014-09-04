(ns icecap.store.mem-test
  (:require [icecap.store.api :refer :all]
            [icecap.store.mem :refer :all]
            [clojure.test :refer :all]
            [clojure.core.async :as a :refer [<!!]]))

(deftest MemStoreTests
  (testing "Data can be round-tripped through the store."
    (let [s (mem-store)]
      (create! s "index" "blob")
      (is (= (<!! (retrieve s "index"))
             "blob")))))
