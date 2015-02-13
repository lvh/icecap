(ns icecap.core
  (:require [caesium.core :refer [sodium-init]]
            [icecap.crypto :as crypto]
            [icecap.rest :as rest]
            [icecap.store.mem :refer [mem-store]]
            [aleph.http :refer [start-server]]
            [taoensso.timbre :refer [info]]
            [environ.core :refer [env]]
            [icecap.codec :refer [safebase64-decode]])
  (:gen-class))

(defn run
  []
  (let [[seed-key salt] (map safebase64-decode [(env :seed-key) (env :salt)])
        components {:store (mem-store)
                    :kdf (crypto/blake2b-kdf seed-key salt)
                    :scheme (crypto/secretbox-scheme)}
        handler (rest/build-site components)]
    (info "Initializing libsodium")
    (sodium-init)
    (info "Running icecap")
    (start-server handler {:port 42327})))

(defn -main
  "Run the (development) server."
  [& args]
  (run))
