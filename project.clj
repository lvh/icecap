(defproject icecap "0.1.0-SNAPSHOT"
  :description "A secure capability URL system"
  :url "https://github.com/lvh/icecap"
  :license {:name "Eclipse Public License"
            :url "http://www.eclipse.org/legal/epl-v10.html"}
  :dependencies [[org.clojure/clojure "1.6.0"]
                 [org.clojure/core.async "0.1.338.0-5c5012-alpha"]
                 [prismatic/schema "0.2.6"]
                 [schema-contrib "0.1.3"]
                 [cddr/integrity "0.2.0-SNAPSHOT"]]
  :main ^:skip-aot icecap.core
  :target-path "target/%s"
  :profiles {:uberjar {:aot :all}})
