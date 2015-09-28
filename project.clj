(defproject icecap "0.1.0-SNAPSHOT"
  :description "A secure capability URL system"
  :url "https://github.com/lvh/icecap"
  :license {:name "Eclipse Public License"
            :url "http://www.eclipse.org/legal/epl-v10.html"}
  :dependencies [[org.clojure/clojure "1.8.0-alpha4"]

                 ;; Stores
                 [com.novemberain/welle "3.0.0"]

                 ;; Serialization
                 [com.taoensso/nippy "2.11.0-private1"
                  :exclusions [org.clojure/clojure com.taoensso/encore]]

                 ;; Handlers
                 [org.clojure/core.async "0.1.346.0-17112a-alpha"]
                 [aleph "0.4.1-alpha2"]

                 ;; Schemata
                 [prismatic/schema "1.0.1"]
                 [schema-contrib "0.1.5"
                  :exclusions [instaparse]]
                 [org.clojure/core.typed "0.3.11"]

                 ;; Crypto
                 [caesium "0.3.0"]

                 ;; Logging
                 [com.taoensso/timbre "4.1.2"
                  :exclusions [org.clojure/clojure]]

                 ;; REST API
                 [compojure "1.4.0"]
                 [ring/ring-defaults "0.1.5"]
                 [ring-middleware-format "0.6.0"]

                 ;; REST API testing
                 [ring/ring-mock "0.3.0"]]
  :test-selectors {:default (complement (some-fn :riak))
                   :riak :riak}
  :main ^:skip-aot icecap.core
  :target-path "target/%s"
  :profiles {:uberjar {:aot :all}
             :dev {:dependencies [[org.clojure/test.check "0.8.2"]]

                   :aliases ^:replace {"lint"
                                       ["do"
                                        ["clean"]
                                        ["with-profile" "production"
                                         "deps" ":tree"]
                                        ["ancient"]
                                        ["kibit"]
                                        ["bikeshed"]
                                        ["eastwood"]]}
                   :plugins [[jonase/eastwood "0.1.4"]
                             [lein-ancient "0.6.7"
                              :exclusions [rewrite-clj]]
                             [lein-bikeshed "0.1.8"]
                             [lein-cljfmt "0.3.0"]
                             [lein-kibit "0.1.0"]]}})
