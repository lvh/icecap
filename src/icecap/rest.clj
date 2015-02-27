(ns icecap.rest
  "The REST API for icecap."
  (:require [clojure.core.async :refer [<!! <! go] :as async]
            [compojure.core :refer [defroutes context GET POST DELETE]]
            [icecap.codec :refer [safebase64-encode safebase64-decode]]
            [taoensso.timbre :refer [info spy]]
            [icecap.api :refer :all]
            [ring.middleware.defaults :refer [wrap-defaults
                                              api-defaults
                                              secure-api-defaults]]
            [ring.middleware.format :refer [wrap-restful-format]]))

(defn ^:private cap-url
  [request cap]
  (let [netloc (str (:server-name request) ":" (:server-port request))
        path (:uri request)
        encoded-cap (safebase64-encode cap)]
    (str "http://" netloc path "/" encoded-cap)))

(defroutes routes
  (context "/v0/caps" {store :store kdf :kdf scheme :scheme}
    (POST "/" {plan :body-params :as request}
      (info request)
      (let [{cap :cap error :error}
            (spy (<!! (create-cap (spy plan)
                                  :store store
                                  :kdf kdf
                                  :scheme scheme)))]
        {:body (if cap
                 (cap-url request cap)
                 (str error))}))
    (context "/:encoded-cap" [encoded-cap]
      (GET "/" request
        (let [cap (spy (safebase64-decode encoded-cap))]
          (spy (<!! (execute-cap cap
                                 :store store
                                 :kdf kdf
                                 :scheme scheme)))))
      (DELETE "/" request
        (let [cap (spy (safebase64-decode encoded-cap))]
          (spy (<!! (revoke-cap cap
                                :store store
                                :kdf kdf))))))))

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
  [components]
  (-> routes
      (wrap-defaults api-defaults)
      (wrap-restful-format :formats [:edn])
      (wrap-components components)))
