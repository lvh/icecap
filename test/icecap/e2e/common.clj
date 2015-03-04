(ns icecap.e2e.common
  "Common tools for icecap e2e testing."
  (:require [icecap.core :as core]
            [clojure.test :refer :all]
            [aleph.http :as http]
            [taoensso.timbre :refer [info spy]]))

(def icecap-server)

(defn icecap-fixture
  "Runs an icecap server as a test fixture."
  [f]
  (with-redefs [icecap-server (core/run)]
    (f)
    (.close icecap-server)))

(def base-url
  (str "http://localhost:" core/port))

(defn create-cap
  "Creates a capability with the given plan."
  [plan]
  (let [url (str base-url "/v0/caps")
        req {:body (str plan)}]
    (info "Creating cap" url req)
    (http/post url req)))

(defn execute-cap
  "Executes the capability with the given URL."
  [cap-url]
  (info "Executing cap" cap-url)
  (http/get (spy cap-url)))

(defn revoke-cap
  "Revokes the capability with the given URL."
  [cap-url]
  (spy (http/delete cap-url)))