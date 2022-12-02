(ns user
    (:require [clojure.tools.namespace.repl :refer [refresh]]
              [integrant.repl :as ir]
              [integrant.core :as ig]
              [buddy.core.codecs.base64 :as base64]
              [buddy.core.codecs :as codecs]
              [clj-http.client :as h]))

(defn repl-spec []
  (ig/read-string (slurp "specs/dev.edn")))
(defn- prep [] (ir/set-prep! (constantly (repl-spec))))
(defn- go! []  (prep) (ir/go))
(defn- refresh! [] (refresh))
(defn- halt! [] (ir/halt))
(defn- re! [] (halt!) (refresh) (go!))
(defn system [] integrant.repl.state/system)
(defn pprint-system [] )

(defn pool [] (get-in (system) [:component/config :pool]))

(def mk-base64
  (comp codecs/bytes->str
        base64/encode))

(comment
  (let [header (str "Basic "(mk-base64 "root:toor"))]
    (->> (h/get "http://localhost:8090/ping"
                {:headers {:authorization header}})
         :body)))
