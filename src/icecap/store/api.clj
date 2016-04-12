(ns icecap.store.api
  "An abstract API for blob storage.")

(defprotocol Store
  "A blob store."
  (create! [store index blob]
    "Stores `blob` at `index`.

    Returns a deferred that will fire when the blob has been stored.")
  (retrieve [store index]
    "Get the blob at `index`.

     Returns a deferred that fires with the stored blob or `nil`.")
  (delete! [store index]
    "Delete the blob at `index`.

    Returns a deferred that fires when the blob has been deleted, even if
    there wasn't a blob at `index`."))
