(ns icecap.api
  "Externally visible API functionality."
  (:require [taoensso.nippy :as nippy]
            [icecap.handlers.http]
            [icecap.handlers.delay]
            [icecap.schema :refer [check-plan]]
            [clojure.core.async :refer [<!! <! >!! go] :as async]
            [icecap.handlers.core :refer [execute]]
            [icecap.crypto :as crypto]
            [icecap.store.api :refer [create! retrieve delete!]]
            [taoensso.timbre :refer [info spy]]))

(defn create-cap
  "Creates a capability."
  [plan {:keys [store kdf scheme]}]
  (let [error (spy (check-plan plan))]
    (if (nil? error)
      (let [cap (crypto/make-cap)
            {:keys [index cap-key]} (crypto/derive kdf cap)
            encoded-plan (nippy/freeze plan)
            blob (crypto/encrypt scheme cap-key encoded-plan)
            ch (create! store index blob)]
        (async/into {:cap cap} ch))
      (async/to-chan [(spy {:error error})]))))

(defn execute-cap
  "Executes a capability."
  [cap {:keys [store kdf scheme]}]
  (let [{:keys [index cap-key]} (crypto/derive kdf cap)
        blob (<!! (retrieve store index))
        encoded-plan (crypto/decrypt scheme cap-key blob)
        plan (spy (nippy/thaw encoded-plan))
        sub-results (execute plan)]
    (async/into {} sub-results)))

(defn revoke-cap
  "Revokes a capability."
  [cap {:keys [store kdf]}]
  (let [{index :index} (crypto/derive kdf cap)
        chan (delete! store index)]
    (async/into {} chan)))
