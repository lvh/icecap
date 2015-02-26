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
  [plan & {store :store kdf :kdf scheme :scheme}]
  (let [validation (spy (check-plan plan))]
    (if (nil? validation)
      (let [cap (crypto/make-cap)
            {index :index cap-key :cap-key} (crypto/derive kdf cap)
            encoded-plan (nippy/freeze plan)
            blob (crypto/encrypt scheme cap-key encoded-plan)
            ch (create! store index blob)]
        (async/into {:cap cap} ch))
      (let [ch (async/chan)]
        (>!! ch validation)
        (async/close! ch)
        ch))))

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
  [cap & {store :store kdf :kdf}]
  (let [{index :index} (crypto/derive kdf cap)
        chan (delete! store index)]
    (async/into {} chan)))
