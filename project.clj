(defproject icecap "0.1.0-SNAPSHOT"
  :description "A secure capability URL system"
  :url "https://github.com/lvh/icecap"
  :license {:name "Eclipse Public License"
            :url "http://www.eclipse.org/legal/epl-v10.html"}
  :dependencies [[org.clojure/clojure "1.7.0-alpha5"]

                 ;; Stores
                 [com.novemberain/welle "3.0.0"]

                 ;; Serialization
                 [com.taoensso/nippy "2.7.1"
                  :exclusions [org.clojure/clojure]]

                 ;; Handlers
                 [org.clojure/core.async "0.1.346.0-17112a-alpha"]
                 [http-kit "2.1.19"]
                 [aleph "0.4.0-beta2"]

                 ;; Schemata
                 [prismatic/schema "0.3.7"]
                 [schema-contrib "0.1.5"
                  :exclusions [instaparse]]
                 [cddr/integrity "0.2.0-20140823.193326-1"
                  :exclusions [org.clojure/clojure]]
                 [org.clojure/core.typed "0.2.78"]

                 ;; Crypto
                 [caesium "0.3.0"]

                 ;; Logging
                 [com.taoensso/timbre "3.4.0" :exclusions [org.clojure/clojure]]

                 ;; REST API
                 ;; http-kit already required as part of handlers
                 [compojure "1.3.1"]
                 [ring/ring-defaults "0.1.4"]
                 [ring-middleware-format "0.4.0"]
                 [prone "0.8.0"]]
  :test-selectors {:default (complement (some-fn :riak))
                   :riak :riak}
  :main ^:skip-aot icecap.core
  :target-path "target/%s"
  :profiles {:uberjar {:aot :all}
             :dev {:dependencies [[org.clojure/test.check "0.7.0"]]
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
                             [lein-ancient "0.6.2"]
                             [lein-bikeshed "0.1.8"]
                             [lein-kibit "0.0.8"]]}})
