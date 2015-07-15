(defproject icecap "0.1.0-SNAPSHOT"
  :description "A secure capability URL system"
  :url "https://github.com/lvh/icecap"
  :license {:name "Eclipse Public License"
            :url "http://www.eclipse.org/legal/epl-v10.html"}
  :dependencies [[org.clojure/clojure "1.7.0"]

                 ;; Stores
                 [com.novemberain/welle "3.0.0"]

                 ;; Serialization
                 [com.taoensso/nippy "2.10.0-alpha3"
                  :exclusions [org.clojure/clojure com.taoensso/encore]]

                 ;; Handlers
                 [org.clojure/core.async "0.1.346.0-17112a-alpha"]
                 [aleph "0.4.1-alpha1"]

                 ;; Schemata
                 [prismatic/schema "0.4.3"]
                 [schema-contrib "0.1.5"
                  :exclusions [instaparse]]
                 [cddr/integrity "0.3.0-SNAPSHOT"
                  :exclusions [org.clojure/clojure]]
                 [org.clojure/core.typed "0.3.7"]

                 ;; Crypto
                 [caesium "0.3.0"]

                 ;; Logging
                 [com.taoensso/timbre "4.1.0-alpha1"
                  :exclusions [org.clojure/clojure]]

                 ;; REST API
                 [compojure "1.4.0"]
                 [ring/ring-defaults "0.1.4"]
                 [ring-middleware-format "0.5.0"]

                 ;; REST API testing
                 [ring/ring-mock "0.2.0"]]
  :test-selectors {:default (complement (some-fn :riak))
                   :riak :riak}
  :main ^:skip-aot icecap.core
  :target-path "target/%s"
  :profiles {:uberjar {:aot :all}
             :dev {:dependencies [[org.clojure/test.check "0.8.0-alpha3"]]
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
                             [lein-cljfmt "0.2.0"]
                             [lein-kibit "0.1.0"]]}})
