(ns icecap.api
  "Externally visible API functionality."
  (:require [icecap.crypto :as crypto]
            [icecap.handlers.core :as h]
            [icecap.schema :refer [check-plan]]
            [icecap.store.api :as store]
            [manifold.deferred :as md]
            [manifold.stream :as ms]
            [taoensso.nippy :as nippy]))

;; require for multimethod-defining side effect
(require 'icecap.handlers.http
         'icecap.handlers.delay
         'icecap.handlers.rax)

(defn create-cap
  "Creates a capability."
  [plan {:keys [store kdf scheme]}]
  (if-let [error (check-plan plan)]
    (md/success-deferred {:error error})
    (let [cap (crypto/make-cap)
          {:keys [index cap-key]} (crypto/derive kdf cap)
          blob (crypto/encrypt scheme cap-key (nippy/freeze plan))]
      (md/chain (store/create! store index blob)
                (constantly {:cap cap})))))

(defn execute-cap
  "Executes a capability."
  [cap {:keys [store kdf scheme]}]
  (let [{:keys [index cap-key]} (crypto/derive kdf cap)]
    (md/chain (store/retrieve store index)
              (fn [blob]
                (->> (crypto/decrypt scheme cap-key blob)
                     (nippy/thaw)
                     (h/execute)
                     (ms/reduce merge {}))))))

(defn revoke-cap
  "Revokes a capability."
  [cap {:keys [store kdf]}]
  (let [{index :index} (crypto/derive kdf cap)]
    (md/chain (store/delete! store index) (constantly {}))))
