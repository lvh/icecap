(defproject icecap "0.1.0-SNAPSHOT"
  :description "A secure capability URL system"
  :url "https://github.com/lvh/icecap"
  :license {:name "Eclipse Public License"
            :url "http://www.eclipse.org/legal/epl-v10.html"}
  :dependencies [[org.clojure/clojure "1.8.0"]

                 ;; Stores
                 [com.novemberain/welle "3.0.0"]

                 ;; Serialization
                 [com.taoensso/nippy "2.12.2"
                  :exclusions [org.clojure/clojure com.taoensso/encore]]

                 ;; Handlers
                 [org.clojure/core.async "0.2.395"]
                 [aleph "0.4.1"]
                 [manifold "0.1.5"]

                 ;; Schemata
                 [prismatic/schema "1.1.3"]
                 [schema-contrib "0.1.5"
                  :exclusions [instaparse]]
                 [org.clojure/core.typed "0.3.32"]

                 ;; Crypto
                 [caesium "0.9.0"]

                 ;; Logging
                 [com.taoensso/timbre "4.8.0"
                  :exclusions [org.clojure/clojure]]

                 ;; REST API
                 [compojure "1.5.2"]
                 [ring/ring-defaults "0.2.2"]
                 [ring-middleware-format "0.7.0"]

                 ;; REST API testing
                 [ring/ring-mock "0.3.0"]]
  :test-selectors {:default (complement (some-fn :riak))
                   :riak :riak}
  :main ^:skip-aot icecap.core
  :target-path "target/%s"
  :profiles {:uberjar {:aot :all}
             :dev {:dependencies [[org.clojure/test.check "0.9.0"]]

                   :aliases ^:replace {"lint"
                                       ["do"
                                        ["clean"]
                                        ["with-profile" "production"
                                         "deps" ":tree"]
                                        ["ancient"]
                                        ["kibit"]
                                        ["bikeshed"]
                                        ["eastwood"]]}
                   :plugins [[jonase/eastwood "0.2.3"]
                             [lein-ancient "0.6.10"
                              :exclusions [rewrite-clj]]
                             [lein-bikeshed "0.3.0"]
                             [lein-cljfmt "0.5.1"]
                             [lein-kibit "0.1.2"]]}})
