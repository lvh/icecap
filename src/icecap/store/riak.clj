(ns icecap.store.riak
  (:require [clojure.core.async :as a :refer [thread]]
            [clojurewerkz.welle.kv :as kv]
            [icecap.store.api :refer [Store]]))

(defn riak-store
  "Creates a Riak-backed store."
  [conn bucket]
  (reify Store
    (create! [_ index blob]
      (thread (kv/store conn bucket index blob)))
    (retrieve [_ index]
      (thread (kv/fetch-one conn bucket index)))
    (delete! [_ index]
      (thread (kv/delete conn bucket)))))

(defn bucket-props
  "Creates some bucket props suitable for an icecap bucket.

  These properties are centered around two basic ideas:

  - blobs are immutable; they either exist or they don't
  - reads are more common than writes (create/delete ops)

  Because they're immutable, we don't need high consistency. Read
  consistency is only used to determine if a blob has been deleted.
  Additionally, we know that we're never going to see key collisions,
  because the keys are long random strings.
  "
  [& {n :n :or {:n 3}}]
  (let [r 1
        w (- n r)]
    {:n-val n
     :r r
     :w w
     :dw w

     :notfound-ok false
     :basic-quorum true

     :allow-mult false
     :last-write-wins true}))
