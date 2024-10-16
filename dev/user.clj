(ns user
  (:require [clojure.tools.namespace.repl :refer [refresh]]
            [integrant.repl :as ir]
            [integrant.core :as ig]))

(defn repl-spec []
  (ig/read-string (slurp "specs/dev.edn")))
(defn- prep [] (ir/set-prep! (constantly (repl-spec))))
(defn- go! []  (prep) (ir/go))
(defn- refresh! [] (refresh))
(defn- halt! [] (ir/halt))
(defn- re! [] (halt!) (refresh) (go!))
(defn system [] integrant.repl.state/system)
(defn pprint-system [])

(defn pool [] (get-in (system) [:component/config :pool]))
