(ns icecap.rest
  "The REST API for icecap."
  (:require [compojure.core :refer [defroutes context GET POST DELETE]]
            [ring.middleware.defaults :refer [wrap-defaults api-defaults]]
            [ring.middleware.format :refer [wrap-restful-format]]
            [ring.middleware.reload :refer [wrap-reload]]))

(defroutes routes
  (context "/v0/caps" []
           (POST "/" [] "POST")
           (context "/:cap-id" [cap-id]
                    (GET "/" [] "GET")
                    (DELETE "/" [] "DELETE"))))

(def site
  "The site."
  (-> routes
      ;; TODO: api-defaults -> secure-defaults
      (wrap-defaults api-defaults)
      (wrap-restful-format :formats [:edn])))

(def reloading-site
  "Like the regular site, except automatically reloading."
  (wrap-reload site))
