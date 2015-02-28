(ns icecap.e2e.common
  "Common tools for icecap e2e testing."
  (:require [icecap.core :as core]
            [clojure.test :refer :all]
            [aleph.http :refer [delete get post]]
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
  (post (str base-url "/v0/caps")
        {:body (str plan)}))

(defn execute-cap
  "Executes the capability with the given URL."
  [cap-url]
  (get cap-url))

(defn revoke-cap
  "Revokes the capability with the given URL."
  [cap-url]
  (delete cap-url))
