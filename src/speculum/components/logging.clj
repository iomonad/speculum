(ns speculum.components.logging
  (:require [integrant.core :as ig]
            [unilog.config :as c]
            [clojure.tools.logging :as log])
  (:import (ch.qos.logback.classic Logger)
           (org.slf4j LoggerFactory)))

;;; %%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%
;;;  Logging
;;; %%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%

(defonce ex-handler
  (reify Thread$UncaughtExceptionHandler
    (uncaughtException [_ t e]
      (log/error e "Uncaught exception in" (.getName t)))))

(defn get-logger
  ^Logger
  [app]
  (LoggerFactory/getLogger (name app)))

(defmethod ig/init-key :component/logging
  [_ {:keys [app-name app-level] :as sys}]
  (log/debug "starting logging component")
  (when-not app-name
    (throw (ex-info "`app-name` not set in component spec" sys)))
  (let [config (cond-> sys
                 app-level (update :overrides assoc (name app-name) app-level))]
    (c/start-logging! config)
    (Thread/setDefaultUncaughtExceptionHandler ex-handler)
    (assoc config :logger (get-logger app-name))))
