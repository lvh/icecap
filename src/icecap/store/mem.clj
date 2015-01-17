(ns icecap.store.mem
  "An in-memory store."
  (:require [clojure.core.async :as a]
            [icecap.store.api :refer :all]
            [icecap.codec :refer [safebase64-encode]]))

(defn mem-store
  "Create an in-memory store."
  []
  (let [store (atom {})]
    (reify Store
      (create! [_ index blob]
        (let [index (safebase64-encode index)]
          (swap! store assoc index blob)
          (a/to-chan [])))
      (retrieve [_ index]
        (let [index (safebase64-encode index)
              v (@store index)
              vs (if (nil? v) [] [v])]
          (a/to-chan vs)))
      (delete! [_ index]
        (let [index (safebase64-encode index)]
          (swap! store dissoc index)
          (a/to-chan []))))))
