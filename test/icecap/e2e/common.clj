(ns icecap.e2e.common
  "Common tools for icecap e2e testing."
  (:require [icecap.core :refer [run]]
            [clojure.test :refer :all]
            [taoensso.timbre :refer [info spy]]))

(def icecap-server)

(defn icecap-fixture
  "Runs an icecap server as a test fixture."
  [f]
  (with-redefs [icecap-server (run)]
    (f)
    (.close icecap-server)))

(defn create-cap
  "Creates a capability with the given plan."
  [plan])
