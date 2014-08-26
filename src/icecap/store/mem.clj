(ns icecap.store.mem
  (:require [icecap.store.core :refer :all]))

(defn mem-store
  "Create an in-memory store."
  []
  (let [store (atom {})]
    (reify Store
      (create! [_ index blob]
        (swap! store assoc index blob))
      (retrieve [_ index]
        (@store index))
      (delete! [_ index]
        (swap! store dissoc index)))))
