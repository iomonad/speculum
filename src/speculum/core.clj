(ns speculum.core
  (:require [integrant.core :as ig]
            [omniconf.core :as cfg]
            [speculum.components
             [config]
             [webserver]
             [logging]]
            [clojure.tools.logging :as log])
  (:gen-class))

;;; %%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%
;;;  Core
;;; %%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%

(cfg/define
  {:spec {:description "application spec"
          :type :file
          :required true}})

(defn -main [& args]
  (cfg/populate-from-cmd args)
  (cfg/populate-from-properties)
  (cfg/populate-from-env)
  ;;(cfg/verify :silent true)
  (let [config (ig/read-string (slurp (cfg/get :spec)))]
    (log/info "starting arkana server")
    (try
      (let [system (ig/init config)]
        (.addShutdownHook (Runtime/getRuntime)
                          (Thread. (fn []
                                     (log/warn "shutting down system")
                                     (ig/halt! system)))))
      (catch clojure.lang.ExceptionInfo ex
        (ig/halt! (:system (ex-data ex)))
        (throw ex)))))
