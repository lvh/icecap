(ns icecap.rest
  "The REST API for icecap."
  (:require [clojure.core.async :refer [<!!]]
            [compojure.core :refer [defroutes context GET POST DELETE]]
            [icecap.codec :refer [safebase64-decode]]
            [icecap.crypto :as crypto]
            [icecap.store.api :refer [create! retrieve delete!]]
            [taoensso.nippy :as nippy]
            [prone.debug :refer [debug]]
            [prone.middleware :refer [wrap-exceptions]]
            [ring.middleware.defaults :refer [wrap-defaults api-defaults secure-api-defaults]]
            [ring.middleware.format :refer [wrap-restful-format]]
            [ring.middleware.reload :refer [wrap-reload]]))

(defn create-cap
  [cap & {store :store kdf :kdf scheme :scheme}]
  (let [cap (crypto/make-cap)
        {index :index key :key} (crypto/hardcoded-derive kdf cap)
        encoded-plan (nippy/freeze)
        encrypted-plan (crypto/encrypt scheme key encoded-plan)
        ]))

(defn get-cap
  [cap & {store :store kdf :kdf scheme :scheme}]
  (let [{index :index key :key} (crypto/hardcoded-derive kdf cap)
        blob (<!! (retrieve store index))
        encoded-plan (crypto/decrypt scheme key blob)
        plan (nippy/thaw encoded-plan)
        sub-results nil] ;; (execute plan)
    ;; FIXME: maybe it would be better to write this with ->>?
    (into [] sub-results)))

(defn delete-cap
  [cap & {store :store}]
  (delete! store cap))

(defroutes routes
  (context "/v0/caps" []
           (POST "/" request (debug))
           (context "/:encoded-cap" [encoded-cap :as {store :store kdf :kdf scheme :scheme}]
                    (GET "/" [] (<!! (get-cap (safebase64-decode encoded-cap)
                                              :store store :kdf kdf :scheme scheme)))
                    (DELETE "/" [] (<!! (delete-cap (safebase64-decode encoded-cap)
                                                    :store store))))))

(defn ^:private wrap-components
  "Adds some components to each request map."
  [handler components]
  (fn [request]
    (handler (merge request components))))

(defn build-site
  "Builds a site.

  All requests will have the given components available. Incoming data
  EDN data will be parsed. Outgoing data structures will be serialized
  as EDN.

  The site's behavior will have some sane defaults. If it is running
  in dev mode, secure defaults (which include automatically
  redirecting to HTTPS) are disabled. Additionally, the server will
  automatically reload code.
  "
  [components & {:keys [dev-mode] :or {dev-mode false}}]
  (let [defaults (if dev-mode api-defaults secure-api-defaults)
        site (-> routes
                 (wrap-defaults defaults)
                 (wrap-restful-format :formats [:edn])
                 (wrap-components components))]
    (if dev-mode
      (-> site
          (wrap-reload)
          (wrap-exceptions))
      site)))
