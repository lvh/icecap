(ns icecap.store.mem
  "An in-memory store."
  (:require [clojure.core.async :as a]
            [icecap.store.core :refer :all]))

(defn mem-store
  "Create an in-memory store."
  []
  (let [store (atom {})]
    (reify Store
      (create! [_ index blob]
        (swap! store assoc index blob)
        (a/to-chan []))
      (retrieve [_ index]
        (a/to-chan [(@store index)]))
      (delete! [_ index]
        (swap! store dissoc index)
        (a/to-chan [])))))
