(ns icecap.codec
  (:require [clojure.string :as s]
            [ring.util.codec :refer [base64-encode base64-decode]]))

(defn safebase64-decode
  "Decode urlsafe base64."
  [^String encoded]
  (-> encoded
      (s/replace "-" "+")
      (s/replace "_" "/")
      base64-decode))

(defn safebase64-encode
  "Encode as urlsafe base64."
  [raw]
  (-> raw
      (base64-encode)
      (s/replace "+" "-")
      (s/replace "/" "_")
      (s/replace "=" "")))
