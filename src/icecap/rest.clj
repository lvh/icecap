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
  (context "/v0/caps" {components :components}
    (POST "/" {plan :body-params :as request}
      (info request)
      (let [{cap :cap error :error}
            (spy (<!! (create-cap (spy plan) components)))]
        (if cap
          {:body (cap-url request cap)}
          {:body (str error)})))
    (context "/:encoded-cap" [encoded-cap]
      (GET "/" request
        (let [cap (spy (safebase64-decode encoded-cap))]
          (spy (<!! (execute-cap cap components)))))
      (DELETE "/" request
        (let [cap (spy (safebase64-decode encoded-cap))]
          (spy (<!! (revoke-cap cap components))))))))

(defn ^:private wrap-components
  "Adds some components to each request map."
  [handler components]
  (fn [request]
    (handler (assoc request :components components))))

(defn build-site
  "Builds a site.

  All requests will have the given components available. Incoming data
  EDN data will be parsed. Outgoing data structures will be serialized
  as EDN.

  The site's behavior will have some sane defaults.
  "
  [components]
  (-> routes
      (wrap-defaults api-defaults)
      (wrap-restful-format :formats [:edn])
      (wrap-components components)))
