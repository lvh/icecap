(ns icecap.rest
  "The REST API for icecap."
  (:require [compojure.core :refer [defroutes context GET POST DELETE]]
            [prone.middleware :refer [wrap-exceptions]]
            [ring.middleware.defaults :refer [wrap-defaults api-defaults secure-api-defaults]]
            [ring.middleware.format :refer [wrap-restful-format]]
            [ring.middleware.reload :refer [wrap-reload]]))

(defroutes routes
  (context "/v0/caps" []
           (POST "/" [] "POST")
           (context "/:cap-id" [cap-id]
                    (GET "/" [] "GET")
                    (DELETE "/" [] "DELETE"))))

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
  [components & {dev-mode :dev-mode :or {dev-mode false}}]
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
