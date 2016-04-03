(ns icecap.e2e.http-test
  "End-to-end tests for making HTTP requests."
  (:require [icecap.e2e.common :refer :all]
            [clojure.test :refer :all]
            [aleph.http :as http]
            [taoensso.timbre :refer [info spy]]))

(def http-server
  "A test HTTP server, serving requests.

  This var gets assigned in a test fixture, so that we can clean up
  when the test is over.")

(def recvd-reqs
  "The requests received by the test HTTP server.

  This var gets reset in an :each test fixture, so that we can clean
  up between tests.")

(defn handler
  "The implementation of the test HTTP server.

  Keeps track of all requests, and always returns a simple 200 OK."
  [req]
  (swap! recvd-reqs conj req)
  {:status 200
   :headers {"content-type" "text/plain"}
   :body "xyzzy"})

(def http-server-port
  "The port the test HTTP server will listen on."
  8378) ;; 8378 => TEST

(def http-server-base-url
  "The base URL of the test HTTP server."
  (str "http://localhost:" http-server-port))

(defn ^:private http-server-fixture
  "A fixture that listens on `http-server-port` and cleans up afterwards."
  [f]
  (let [server (http/start-server handler {:port http-server-port})]
    (with-redefs [http-server server]
      (try (f)
           (finally (.close http-server))))))

(defn ^:private store-reqs-fixture
  "A fixture that resets the request store."
  [f]
  (with-redefs [recvd-reqs (atom [])]
    (f)))

(use-fixtures :once icecap-fixture http-server-fixture)
(use-fixtures :each store-reqs-fixture)

(deftest http-tests
  (let [plan {:type :http
              :method :GET
              :url (str http-server-base-url "/test/example")}
        create-result (spy @(create-cap plan))
        cap-url (:cap (get-body create-result))
        exercise-result @(execute-cap cap-url)]
    ;; Was the cap successfully created?
    (is (= (:status create-result) 201))
    (is (some? cap-url))

    ;; Was the cap successfully executed?
    (is (= (:status exercise-result) 200))
    (let [[request] @recvd-reqs]
      (is (= request
             {:remote-addr "127.0.0.1"
              :request-method :get
              :scheme :http
              :server-name "localhost"
              :server-port 8378
              :uri "/test/example"
              :headers {"host" "localhost"
                        "content-length" "0"}
              :keep-alive? true
              :query-string nil
              :body nil})))))

(deftest invalid-http-step-tests
  (are [step expected] (= (get-body @(create-cap step)) {:error expected})
    {:type :http} '{:url missing-required-key
                    :method missing-required-key}))
