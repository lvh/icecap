(ns icecap.store.riak
  (:require [icecap.store.api :refer [Store]]
            [clojurewerkz.welle.kv :as kv]))

(defn riak-store
  "Creates a Riak-backed store."
  [conn bucket]
  (reify Store
    (create! [_ index blob]
      (kv/store conn bucket index blob))
    (retrieve [_ index]
      (kv/fetch-one conn bucket index))
    (delete! [_ index]
      (kv/delete conn bucket))))

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
  [& {n :n r :r :or {:n 3 :r 1}}]
  (let [w (- n r)]
    {:n-val n
     :r r
     :w w
     :dw w

     :notfound-ok false
     :basic-quorum true

     :allow-mult false
     :last-write-wins true}))
