(ns speculum.core
  (:require [integrant.core :as ig]
            [omniconf.core :as cfg]
            [speculum.components
             [workqueue :as workqueue]
             [config :as config]
             [webserver :as webserver]
             [logging :as logging]
             [storage :as storage]]
            [medley.core :refer [deep-merge]]
            [clojure.tools.logging :as log]
            [macrometer.prometheus :as prom])
  (:gen-class))

;;; %%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%
;;;  Core
;;; %%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%

(cfg/define
  {:spec {:description "application spec"
          :type :file
          :required true}})

(def default-system-config
  (merge config/config
         logging/config
         webserver/config
         storage/config
         workqueue/config
         ;;metrics/config
         prom/config))

(defn load-config [path]
  (deep-merge default-system-config
              (ig/read-string (slurp path))))

(defn -main [& args]
  (cfg/populate-from-cmd args)
  (cfg/populate-from-properties)
  (cfg/populate-from-env)
  (let [config (load-config (cfg/get :spec))]
    (log/info "starting speculum server")
    (try
      (let [system (ig/init config)]
        (.addShutdownHook (Runtime/getRuntime)
                          (Thread. (fn []
                                     (log/warn "shutting down system")
                                     (ig/halt! system)))))
      (catch clojure.lang.ExceptionInfo ex
        (ig/halt! (:system (ex-data ex)))
        (throw ex)))))
