(ns icecap.e2e.http-test
  "End-to-end tests for making HTTP requests."
  (:require [icecap.e2e.common :refer :all]
            [clojure.test :refer :all]
            [aleph.http :as http]
            [manifold.deferred :refer [let-flow]]
            [taoensso.timbre :refer [info spy]]))

(def ^:dynamic http-server)
(def ^:dynamic recvd-reqs)

(defn handler
  [req]
  (swap! recvd-reqs conj req)
  {:status 200
   :headers {"content-type" "text/plain"}
   :body "xyzzy"})

(def http-server-port
  8378) ;; 8378 => TEST

(def http-server-base-url
  (str "http://localhost:" http-server-port))

(defn ^:private http-server-fixture
  [f]
  (binding [http-server (http/start-server handler
                                           {:port http-server-port})]
    (f)
    (.close http-server)))

(defn ^:private store-reqs-fixture
  [f]
  (binding [recvd-reqs (atom [])]
    (f)))

(use-fixtures :once icecap-fixture http-server-fixture)
(use-fixtures :each store-reqs-fixture)

(deftest http-tests
  (let [create-result @(create-cap {:type :http
                                    :url (str http-server-base-url
                                              "/test/example")})
        cap-url (:cap (get-body create-result))
        exercise-result @(execute-cap cap-url)]
    (is (= (select-keys create-result [:code])
           {:code 201}))
    (is (= cap-url nil))))

(deftest invalid-http-step-tests
  (are [step expected] (let [create-result @(create-cap step)
                             message (get-body create-result)]
                         (= message {:error expected}))
       {:type :http} '{:url missing-required-key}))
