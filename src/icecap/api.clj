(ns icecap.api
  "Externally visible API functionality."
  (:require [taoensso.nippy :as nippy]
            [icecap.handlers.http]
            [icecap.handlers.delay]
            [clojure.core.async :refer [<!! <! go] :as async]
            [icecap.handlers.core :refer [execute]]
            [icecap.crypto :as crypto]
            [icecap.store.api :refer [create! retrieve delete!]]))

(defn create-cap
  "Creates a capability."
  [plan & {store :store kdf :kdf scheme :scheme}]
  (let [cap (crypto/make-cap)
        {index :index key :key} (crypto/derive kdf cap)
        encoded-plan (nippy/freeze plan)
        blob (crypto/encrypt scheme key encoded-plan)
        ch (create! store index blob)]
    (async/into {:cap cap} ch)))

(defn execute-cap
  "Executes a capability."
  [cap & {store :store kdf :kdf scheme :scheme}]
  (let [{index :index key :key} (crypto/derive kdf cap)
        blob (<!! (retrieve store index))
        encoded-plan (crypto/decrypt scheme key blob)
        plan (nippy/thaw encoded-plan)
        sub-results (execute plan)]
    (async/into {} sub-results)))

(defn revoke-cap
  "Revokes a capability."
  [cap & {store :store}]
  (async/into {} (delete! store cap)))
