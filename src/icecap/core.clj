(ns icecap.core
  (:require [aleph.http :refer [start-server]]
            [icecap.crypto :as crypto]
            [icecap.rest :as rest]
            [icecap.store.mem :refer [mem-store]]
            [taoensso.timbre :refer [info]])
  (:gen-class))

(def port
  42327) ;; ICECAP

(defn run
  []
  (let [[seed-key salt] (map crypto/nul-byte-array
                             [crypto/seed-key-bytes crypto/salt-bytes])
        components {:store (mem-store)
                    :kdf (crypto/blake2b-kdf seed-key salt)
                    :scheme (crypto/secretbox-scheme)}
        handler (rest/build-site components)]
    (info "Running icecap on port" port)
    (start-server handler {:port port
                           ;; :ssl-context (self-signed-ssl-context)
                           })))

(defn -main
  "Run the (development) server."
  [& args]
  (run)
  ;; block forever:
  @(future))
