(ns speculum.components.webserver
  (:require [integrant.core :as ig]
            [clojure.tools.logging :as log]
            [io.pedestal.http :as server]
            [reitit.pedestal :as pedestal]
            [reitit.http :as http]
            [speculum.routes :refer [routes]]))

;;; %%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%
;;;  Webserver
;;; %%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%

(defmethod ig/init-key :component/webserver
  [_ {:keys [port] :as system}]
  (let [default-conf {::server/type :jetty
                      ::server/port port
                      ::server/join? false
                      ;; no pedestal routes
                      ::server/routes []}
        instance (-> default-conf
                     (server/default-interceptors)
                     (pedestal/replace-last-interceptor
                      (pedestal/routing-interceptor
                       (http/router routes)))
                     (server/dev-interceptors)
                     (server/create-server)
                     (server/start))]
    (log/info "starting webserver component")
    (assoc system :server instance)))


(defmethod ig/halt-key! :component/webserver
  [_ {:keys [instance]}]
  (when instance
    (log/info "shutting down server instance")
    (server/stop instance)))
