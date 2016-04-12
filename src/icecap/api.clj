(ns icecap.api
  "Externally visible API functionality."
  (:require [taoensso.nippy :as nippy]
            [icecap.handlers.http]
            [icecap.handlers.delay]
            [icecap.schema :refer [check-plan]]
            [icecap.handlers.core :as h]
            [icecap.crypto :as crypto]
            [icecap.store.api :refer [create! retrieve delete!]]
            [taoensso.timbre :refer [info spy]]
            [manifold.deferred :as md]
            [manifold.stream :as ms]))

(defn create-cap
  "Creates a capability."
  [plan {:keys [store kdf scheme]}]
  (let [error (spy (check-plan plan))]
    (if (nil? error)
      (let [cap (crypto/make-cap)
            {:keys [index cap-key]} (crypto/derive kdf cap)
            blob (crypto/encrypt scheme cap-key (nippy/freeze plan))]
        (md/chain (create! store index blob)
                  (constantly {:cap cap})))
      (md/success-deferred {:error error}))))

(defn execute-cap
  "Executes a capability."
  [cap {:keys [store kdf scheme]}]
  (let [{:keys [index cap-key]} (crypto/derive kdf cap)]
    (md/chain (retrieve store index)
              (fn [blob]
                (->> (crypto/decrypt scheme cap-key blob)
                     (nippy/thaw)
                     (h/execute)
                     (ms/reduce merge {}))))))

(defn revoke-cap
  "Revokes a capability."
  [cap {:keys [store kdf]}]
  (let [{index :index} (crypto/derive kdf cap)]
    (md/chain (delete! store index) (constantly {}))))
