(ns icecap.rest-test
  (:require [manifold.deferred :refer [let-flow]]
            [icecap.rest :refer :all]
            [clojure.test :refer :all]
            [icecap.crypto :as crypto]
            [icecap.rest :as rest]
            [icecap.store.mem :refer [mem-store]]
            [ring.mock.request :as mock]
            [clojure.tools.reader.edn :as edn]))

(def handler
  (let [[seed-key salt] (map crypto/nul-byte-array
                             [crypto/seed-key-bytes crypto/salt-bytes])
        components {:store (mem-store)
                    :kdf (crypto/blake2b-kdf seed-key salt)
                    :scheme (crypto/secretbox-scheme)}]
    (rest/build-site components)))

(defn create-cap-req
  "A request for creating a cap, given a plan."
  [plan]
  (-> (mock/request :post "/v0/caps")
      (mock/content-type "application/edn")
      (mock/header "Accept" "application/edn")
      (mock/body (str plan))))

(deftest handler-tests
  (testing "creating cap with valid plan succeeds"
    (let-flow [req (create-cap-req {:type :succeed})
               res (handler req)]
              (is (= (:status res) 201))))
  (testing "creating cap with bogus plan results in useful errors"
    (let-flow [req (create-cap-req {:type :bogus})
               {status :status headers :headers} (handler req)]
              (is (= status 400))
              (is (= headers {})))))
