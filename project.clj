(defproject icecap "0.1.0-SNAPSHOT"
  :description "A secure capability URL system"
  :url "https://github.com/lvh/icecap"
  :license {:name "Eclipse Public License"
            :url "http://www.eclipse.org/legal/epl-v10.html"}
  :dependencies [[org.clojure/clojure "1.7.0-alpha2"]

                 ;; Stores
                 [com.novemberain/welle "3.0.0"]

                 ;; Serialization
                 [com.taoensso/nippy "2.7.0-RC1" :exclusions [org.clojure/clojure]]

                 ;; Handlers
                 [org.clojure/core.async "0.1.338.0-5c5012-alpha"]
                 [http-kit "2.1.19"]

                 ;; Schemata
                 [prismatic/schema "0.3.0"]
                 [schema-contrib "0.1.5"]
                 [cddr/integrity "0.2.0-20140823.193326-1" :exclusions [org.clojure/clojure]]

                 ;; Crypto
                 [caesium "0.3.0"]

                 ;; Logging
                 [com.taoensso/timbre "3.3.1" :exclusions [org.clojure/clojure]]

                 ;; REST API
                 ;; http-kit already required as part of handlers
                 [compojure "1.1.9"]
                 [ring/ring-defaults "0.1.2"]
                 [ring-middleware-format "0.4.0"]
                 [prone "0.6.0"]

                 ;; Explicit transitive deps
                 [com.fasterxml.jackson.core/jackson-annotations "2.4.2"]
                 [com.fasterxml.jackson.core/jackson-core "2.4.2"]
                 [com.fasterxml.jackson.core/jackson-databind "2.4.2"]
                 [commons-codec "1.9"]
                 [clj-time "0.8.0"]
                 [joda-time "2.4"]
                 [org.clojure/tools.macro "0.1.5"]
                 [org.clojure/tools.reader "0.8.9"]
                 [potemkin "0.3.9"]
                 [com.taoensso/encore "1.9.3"]
                 [instaparse "1.3.4"]]
  :test-selectors {:default (complement (some-fn :riak))
                   :riak :riak}
  :main ^:skip-aot icecap.core
  :target-path "target/%s"
  :profiles {:uberjar {:aot :all}
             :dev {:dependencies [[peridot "0.3.0"]
                                  [org.clojure/test.check "0.5.9"]]
                   :aliases ^:replace {"lint" ["do"
                                               ["clean"]
                                               ["with-profile" "production" "deps" ":tree"]
                                               ["ancient"]
                                               ["kibit"]
                                               ["bikeshed"]
                                               ["eastwood"]]}
                   :plugins [[jonase/eastwood "0.1.4"]
                             [lein-ancient "0.5.5"]
                             [lein-bikeshed "0.1.8"]
                             [lein-kibit "0.0.8"]]}})
