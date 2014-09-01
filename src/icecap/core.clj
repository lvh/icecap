(ns icecap.core
  (:require [icecap.rest :as rest]
            [org.httpkit.server :refer [run-server]])
  (:gen-class))

(defn -main
  "Run the (development) server."
  [& args]
  (let [handler (if (= args ["dev"])
               rest/reloading-site
               rest/site)]
    (run-server handler {:port 8080})))
