(ns icecap.e2e.http-test
  "End-to-end tests for making HTTP requests."
  (:require [icecap.e2e.common :refer :all]
            [clojure.test :refer :all]
            [aleph.http :as http]))

(def ^:dynamic http-server)
(def ^:dynamic recvd-reqs)

(defn handler
  [req]
  (swap! recvd-reqs conj req)
  {:status 200
   :headers {"content-type" "text/plain"}
   :body "xyzzy"})

(defn ^:private http-server-fixture
  [f]
  (binding [http-server (http/start-server handler {:port 0})]
    (f)
    (.close http-server)))

(defn ^:private store-reqs-fixture
  [f]
  (binding [recvd-reqs (atom [])]
    (f)))

(use-fixtures :once icecap-fixture http-server-fixture)
(use-fixtures :each store-reqs-fixture)

(deftest http-tests
  (is (let [x (create-cap {:type :http})])))
