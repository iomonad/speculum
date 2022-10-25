(defproject speculum "0.1.0-SNAPSHOT"
  :description "Mirror XYZ Raster Tiles locally, rebuild the server tree and estimate total mirror coverage"
  :url "https://github.com/iomonad/speculum"
  :license {:name "EPL-2.0 OR GPL-2.0-or-later WITH Classpath-exception-2.0"
            :url "https://www.eclipse.org/legal/epl-2.0/"}
  :dependencies [[org.clojure/clojure "1.10.1"]]
  :profiles {:dev {:dependencies [[integrant/repl              "0.3.2"]
                                  [org.clojure/tools.namespace "1.3.0"]]
                   :source-paths ["dev"]
                   :resource-paths ["resources" "dev-resources"]}
             :uberjar {:aot :all}}
  :repl-options {:init-ns user
                 :prompt #(str "\u001B[35m[\u001B[34m" % "\u001B[35m]\u001B[33m▶\u001B[m ")})
