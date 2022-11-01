(defproject speculum "0.1.6-SNAPSHOT"
  :description "Mirror XYZ Raster Tiles locally, rebuild the server tree and estimate total mirror coverage"
  :url "https://github.com/iomonad/speculum"
  :license {:name "EPL-2.0 OR GPL-2.0-or-later WITH Classpath-exception-2.0"
            :url "https://www.eclipse.org/legal/epl-2.0/"}
  :dependencies [[clj-http                     "3.12.3"]
                 [integrant                    "0.8.0"]
                 [org.clojure/core.match       "1.0.0"]
                 [org.clojure/clojure          "1.10.1"]
                 [prismatic/schema             "1.4.1"]
                 [spootnik/unilog              "0.7.24"]
                 [io.pedestal/pedestal.jetty   "0.5.5"]
                 [io.pedestal/pedestal.service "0.5.5"]
                 [metosin/muuntaja             "0.6.8"]
                 [metosin/reitit               "0.5.18"]
                 [metosin/reitit-pedestal      "0.5.18"]
                 [org.clojure/tools.logging    "1.2.4"]
                 [buddy/buddy-core             "1.10.413"]
                 [cc.qbits/knit                "1.0.0"]
                 [clj-commons/fs               "1.6.310"]
                 [clj-http                     "3.12.3"]
                 [com.grammarly/omniconf       "0.4.3"]
                 [medley                       "1.4.0"]]
  :profiles {:dev {:dependencies [[integrant/repl               "0.3.2"]
                                  [org.clojure/tools.namespace  "1.3.0"]]
                   :source-paths ["dev"]
                   :resource-paths ["resources"]}
             :uberjar {:aot :all}}
  :target-path "target/%s"
  :min-lein-version "2.5.3"
  :main ^{:skip-aot true} speculum.core
  :repl-options {:init-ns user
                 :prompt #(str "\u001B[35m[\u001B[34m" % "\u001B[35m]\u001B[33m▶\u001B[m ")})
