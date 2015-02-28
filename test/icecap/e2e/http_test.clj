(ns icecap.e2e.http-test
  "End-to-end tests for making HTTP requests."
  (:require [icecap.e2e.common :refer :all]
            [clojure.test :refer :all]
            [aleph.http :as http]
            [manifold.deferred :refer [let-flow]]
            [byte-streams :as bs]))

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
  (is (let [d (let-flow
               [create-result (create-cap {:type :http})
                cap-url (bs/to-string (:body create-result))
                exercise-result (execute-cap cap-url)]
               [create-result exercise-result cap-url])
            [{create-code :code} {} cap-url] @d]
        (and (= create-code 201)
             (= cap-url nil)))))
