(ns icecap.e2e.http-test
  "End-to-end tests for making HTTP requests."
  (:require [icecap.e2e.common :refer :all]
            [clojure.test :refer :all]
            [aleph.http :as http]
            [manifold.deferred :refer [let-flow]]
            [taoensso.timbre :refer [info spy]]))

(def http-server)
(def recvd-reqs)

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
  (let [server (http/start-server handler {:port http-server-port})]
    (with-redefs [http-server server]
      (try (f)
           (finally (.close http-server))))))

(defn ^:private store-reqs-fixture
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
    (let [[{}] (spy @recvd-reqs)]
      (is (= 1 1)))))

(deftest invalid-http-step-tests
  (are [step expected] (= (get-body @(create-cap step)) {:error expected})
       {:type :http} '{:url missing-required-key
                       :method missing-required-key}))
