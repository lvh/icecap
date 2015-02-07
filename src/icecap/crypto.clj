(ns icecap.crypto
  "The cryptographic backing for icecap.

  Beyond than transport layer security (provided by TLS), icecap has
  two important cryptographic components:

  - A key derivation function. Used to generate the index (the
  location of the blob in the database) and the cap key (the key used
  to encrypt the blob).
  - An authenticated encryption scheme.

  This namespace defines those components."
  (:require [caesium.crypto.generichash :refer [blake2b]]
            [caesium.crypto.secretbox :as secretbox]
            [caesium.randombytes :refer [randombytes]])
  (:import (java.util Arrays))
  (:refer-clojure :exclude [derive]))

(def cap-bits
  "The size of a cap, in bits."
  256)

(def cap-bytes
  "See cap-bits."
  (/ cap-bits 8))

(def make-cap
  "Makes a new capability identifier.

  This uses the `randombytes` function from caesium (libsodium), which
  does the right thing depending on your platform. Consult the
  [libsodium documentation][randombytes] for more information.

  [randombytes]: http://doc.libsodium.org/generating_random_data/README.html
  "
  (partial randombytes cap-bytes))

(def seed-key-bits
  "The size of the seed key, in bits."
  256)

(def seed-key-bytes
  "See seed-key-bits."
  (/ seed-key-bits 8))

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

(defn nul-byte-array
  [n]
  (byte-array (repeat n 0)))

(defprotocol KDF
  "A key derivation function.

  This is a key derivation function specifically for icecap, and not a
  generic KDF interface.
  "
  (derive [kdf cap]))

(def ^:private personal
  "A personalization parameter."
  (.getBytes "icecap blob storage"))

(defn blake2b-kdf
  "Create a key derivation function based on BLAKE2b."
  [seed-key salt]
  (reify KDF
    (derive [_ cap]
      (let [output (blake2b cap :salt salt :key seed-key :personal personal)
            index-start-byte (inc cap-key-bytes)
            index-end-byte (+ index-start-byte index-bytes)
            index (Arrays/copyOfRange output index-start-byte index-end-byte)
            cap-key (Arrays/copyOf output cap-key-bytes)]
        {:index index :cap-key cap-key}))))

(defprotocol EncryptionScheme
  "An authenticated encryption scheme."
  (encrypt [scheme key plaintext])
  (decrypt [scheme key ciphertext]))

(defn secretbox-scheme
  "An encryption scheme based on NaCl's `secretbox`.

  Please note that this uses a fixed nonce, and that's *perfectly
  fine*. This is the only message ever encrypted with that key!"
  []
  (let [nonce (secretbox/int->nonce 1)]
    (reify EncryptionScheme
      (encrypt [_ key plaintext]
        (secretbox/encrypt key nonce plaintext))
      (decrypt [_ key ciphertext]
        (secretbox/decrypt key nonce ciphertext)))))
