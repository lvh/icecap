(defproject icecap "0.1.0-SNAPSHOT"
  :description "A secure capability URL system"
  :url "https://github.com/lvh/icecap"
  :license {:name "Eclipse Public License"
            :url "http://www.eclipse.org/legal/epl-v10.html"}
  :dependencies [[org.clojure/clojure "1.7.0-alpha2"]

                 ;; Logging
                 [com.taoensso/timbre "3.3.0" :exclusions [org.clojure/clojure]]

                 ;; Handlers
                 [org.clojure/core.async "0.1.338.0-5c5012-alpha"]
                 [http-kit "2.1.16"]

                 ;; Schemata
                 [prismatic/schema "0.2.6"]
                 [schema-contrib "0.1.3"]
                 [cddr/integrity "0.2.0-20140823.193326-1" :exclusions [org.clojure/clojure]]

                 ;; Crypto
                 [crypto-random "1.2.0"]
                 [caesium "0.1.2"]

                 ;; Serialization
                 [com.taoensso/nippy "2.7.0-RC1" :exclusions [org.clojure/clojure]]

                 ;; REST API
                 ;; http-kit already required as part of handlers
                 [compojure "1.1.8"]
                 [ring/ring-defaults "0.1.1"]
                 [ring-middleware-format "0.4.0"]
                 [prone "0.4.0"]
                 [peridot "0.3.0"]]
  :main ^:skip-aot icecap.core
  :target-path "target/%s"
  :profiles {:uberjar {:aot :all}})
