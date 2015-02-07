(ns icecap.core
  (:require [caesium.core :refer [sodium-init]]
            [icecap.crypto :as crypto]
            [icecap.rest :as rest]
            [icecap.store.mem :refer [mem-store]]
            [org.httpkit.server :refer [run-server]]
            [taoensso.timbre :refer [info]])
  (:gen-class))

(defn -main
  "Run the (development) server."
  [& args]
  (let [dev-mode (= args ["dev"])
        components {:store (mem-store)
                    :kdf (let [seed-key (crypto/nul-byte-array crypto/seed-key-bytes)
                               salt (crypto/nul-byte-array crypto/salt-bytes)]
                           (crypto/blake2b-kdf seed-key salt))
                    :scheme (crypto/secretbox-scheme)}
        handler (rest/build-site components :dev-mode dev-mode)]
    (info (str "Starting, dev-mode " (if dev-mode "on" "off")))
    (info "Initializing libsodium")
    (sodium-init)
    (info "Running API server")
    (run-server handler {:port 8080})))
