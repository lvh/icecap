(ns icecap.crypto
  "The cryptographic backing for icecap.

  Beyond than transport layer security (provided by TLS), icecap has
  two important cryptographic components:

  - A key derivation function. Used to generate the index (the
  location of the blob in the database) and the cap key (the key used
  to encrypt the blob).
  - An authenticated encryption scheme."
  ;; (:require [caesium.crypto.generichash :refer [blake2b]]
  ;;           [caesium.crypto.secretbox :as secretbox])
  ;; (:import java.util Arrays)
  )

(def master-key-bits
  "The size of the master key, in bits."
  256)

(def master-key-bytes
  "See master-key-bits."
  (/ master-key-bits 8))

(def salt-bits
  "The size of the salt, in bits."
  256)

(def salt-bytes
  "See salt-bits."
  (/ salt-bits 8))

(def index-bits
  "The size of the index, in bits.

  This is picked sufficiently large, such that even with a large
  number of stored capabilities, the odds of a birthday collision are
  negligible."
  256)

(def index-bytes
  "See index-bits."
  (/ index-bits 8))

(def cap-key-bits
  "The size of the cap key, in bits.

  This is picked sufficiently large, such that even with a large
  number of stored capabilities, the odds of a birthday collision are
  negligible."
  256)

(def cap-key-bytes
  "See cap-key-bits."
  (/ cap-key-bits 8))

(defn ^:private nul-byte-array
  [n]
  (byte-array (repeat n 0)))

(def hardcoded-master-key-fixme
  "See #15."
  (nul-byte-array master-key-bytes))

(def hardcoded-salt-fixme
  "See #15."
  (nul-byte-array salt-bytes))

(defprotocol KDF
  "A key derivation function.

  This is a key derivation function specifically for icecap, and not a
  generic KDF interface.
  "
  (derive [kdf cap master-key salt]))

(defn bogus-kdf
  "A totally bogus KDF that consistently returns all-NUL keys.

  Clearly only suitable for development."
  []
  (reify KDF
    (derive [_ _ _ _]
      {:index (nul-byte-array index-bytes)
       :cap-key (nul-byte-array cap-key-bytes)})))

;; (def ^:private personal
;;   "A personalization parameter."
;;   (.getBytes "icecap blob storage"))
;; (defn blake2b-kdf
;;   "Create a key derivation function based on BLAKE2b."
;;   (reify KDF
;;     (derive [_ cap mccaster-key salt]
;;       (let [output (blake2b cap :salt salt :key master-key :personal personal)
;;             index-start-byte (inc cap-key-bytes)
;;             index-end-byte (+ index-start-byte index-bytes)
;;             index (Arrays/copyOfRange output index-start-byte index-end-byte)
;;             cap-key (Arrays/copyOf output cap-key-bytes)]
;;         {:index index :cap-key cap-key}))))

(defprotocol EncryptionScheme
  "An authenticated encryption scheme."
  (encrypt [scheme key plaintext])
  (decrypt [scheme key ciphertext]))

(defn bogus-scheme
  "An encryption scheme that doesn't actually do anything.

  Clearly only suitable for development."
  []
  (reify EncryptionScheme
    (encrypt [_ _ plaintext] plaintext)
    (decrypt [_ _ ciphertext] ciphertext)))

;; (defn secretbox-scheme
;;   "An encryption scheme based on NaCl's `secretbox`.

;;   Please note that this uses a fixed nonce, and that's *perfectly
;;   fine*. This is the only message ever encrypted with that key!"
;;   (reify EncryptionScheme
;;     (encrypt [_ key plaintext]
;;       (secretbox/encrypt key (secretbox/int->nonce 1) plaintext))
;;     (decrypt [_ key ciphertext]
;;       (secretbox/decrypt key (secretbox/int->nonce 1) ciphertext))))
