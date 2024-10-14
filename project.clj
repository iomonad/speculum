(defproject speculum "0.1.9-SNAPSHOT"
  :description "Mirror XYZ Raster Tiles locally, rebuild the server tree and estimate total mirror coverage"
  :url "https://github.com/iomonad/speculum"
  :license {:name "EPL-2.0 OR GPL-2.0-or-later WITH Classpath-exception-2.0"
            :url "https://www.eclipse.org/legal/epl-2.0/"}
  :dependencies [[cc.qbits/knit                    "2.0.3"]
                 [clj-commons/fs                   "1.6.311"]
                 [clj-http                         "3.13.0"]
                 [com.grammarly/omniconf           "0.5.2"]
                 [com.oscaro/macrometer.core       "1.12.4.0"]
                 [com.oscaro/macrometer.jmx        "1.12.4.0"]
                 [com.oscaro/macrometer.prometheus "1.12.4.0"]
                 [integrant                        "0.13.0"]
                 [io.pedestal/pedestal.jetty       "0.7.1"]
                 [io.pedestal/pedestal.service     "0.7.1"]
                 [medley                           "1.4.0"]
                 [metosin/muuntaja                 "0.6.10"]
                 [metosin/reitit                   "0.7.2"]
                 [metosin/reitit-pedestal          "0.7.2"]
                 [net.mikera/core.matrix           "0.63.0"]
                 [org.clojure/clojure              "1.10.1"]
                 [org.clojure/core.match           "1.1.0"]
                 [org.clojure/tools.logging        "1.3.0"]
                 [prismatic/schema                 "1.4.1"]
                 [spootnik/unilog                  "0.7.32"]]
  :profiles {:dev {:dependencies [[integrant/repl               "0.3.3"]
                                  [org.clojure/tools.namespace  "1.5.0"]]
                   :source-paths ["dev"]
                   :resource-paths ["resources"]}
             :uberjar {:aot :all}}
  :target-path "target/%s"
  :min-lein-version "2.5.3"
  :main ^{:skip-aot true} speculum.core
  :repl-options {:init-ns user
                 :prompt #(str "\u001B[35m[\u001B[34m" % "\u001B[35m]\u001B[33m▶\u001B[m ")})
