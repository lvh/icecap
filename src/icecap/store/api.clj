(ns icecap.store.api
  "An abstract API for blob storage.")

(defprotocol Store
  "A blob store."
  (create! [store index blob]
    "Stores `blob` at `index`.

    Returns a core.async chan that will close when the blob has been
    stored.")
  (retrieve [store index]
    "Get the blob at `index`.

     Returns a core.async chan that returns the stored blob, then
     closes. If the blob does not exist, just closes the channel.")
  (delete! [store index]
    "Delete the blob at `index`.

    Returns a core.async chan that will close when the blob has been
    deleted, even if there wasn't a blob at `index`."))
