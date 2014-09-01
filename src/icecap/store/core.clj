(ns icecap.store.core
  "An abstract API for blob storage.")

(defprotocol Store
  "A blob store."
  (create! [store index blob]
    "Stores `blob` at `index`.")
  (retrieve [store index]
    "Get the blob at `index`,")
  (delete! [store index]
    "Delete the blob at `index`."))
