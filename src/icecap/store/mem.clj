(ns icecap.store.mem
  "An in-memory store."
  (:require [icecap.codec :refer [safebase64-encode]]
            [icecap.store.api :as api]
            [manifold.deferred :as md]))

(defn mem-store
  "Create an in-memory store.

  Internally, this base64-encodes indices. This is necessary because
  Java byte arrays don't have equality semantics."
  []
  (let [store (atom {})]
    (reify api/Store
      (create! [_ index blob]
        (swap! store assoc (safebase64-encode index) blob)
        (md/success-deferred nil))
      (retrieve [_ index]
        (md/success-deferred (@store (safebase64-encode index))))
      (delete! [_ index]
        (swap! store dissoc (safebase64-encode index))
        (md/success-deferred nil)))))
