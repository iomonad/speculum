(ns user
  (:require [clojure.tools.namespace.repl :refer [refresh]]
            [unilog.config :as u]
            [integrant.repl :as ir]
            [integrant.core :as ig]))

(u/start-logging! u/default-configuration)

(defn repl-spec []
  (ig/read-string (slurp "specs/dev.edn")))
(defn- prep [] (ir/set-prep! (constantly (repl-spec))))
(defn- go! []  (prep) (ir/go))
(defn- refresh! [] (refresh))
(defn- halt! [] (ir/halt))
(defn- re! [] (halt!) (refresh) (go!))
(defn system [] integrant.repl.state/system)
(defn pprint-system []
  (clojure.pprint/pprint (system)))
