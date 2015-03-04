(ns icecap.rest-test
  (:require [icecap.rest :refer :all]
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
  [plan]
  (-> (mock/request :post "/v0/caps")
      (mock/body (str plan))))

(deftest handler-tests
  (testing "creating cap with valid plan succeeds"
    (is (= (handler (create-cap-req {:type :succeed}))
           {:status 201})))
  (testing "creating cap with bogus plan results in useful errors"
    (is (let [response (handler (create-cap-req {:type :bogus}))
              response (update response :body edn/read-string)]
          (= response
             {:status 400
              :headers {"Content-Type" "application/edn"}
              :message "Test"})))))
