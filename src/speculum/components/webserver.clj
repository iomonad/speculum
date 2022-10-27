(ns speculum.components.webserver
  (:require [integrant.core :as ig]
            [clojure.tools.logging :as log]
            [io.pedestal.http :as server]
            [reitit.pedestal :as pedestal]
            [reitit.http :as http]
            [speculum.routes :refer [routes]]
            [reitit.http.coercion :as coercion]
            [muuntaja.core :as muuntaja]
            [reitit.coercion.schema]
            [reitit.http.interceptors
             [exception :as exception]
             [parameters :as ri.parameters]
             [muuntaja   :as ri.muuntaja]]
            [speculum.interceptors :as itcp]))

;;; %%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%
;;;  Webserver
;;; %%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%

(defn interceptors-stack [components]
  [(itcp/speculum-context components)
   (ri.parameters/parameters-interceptor)
   (ri.muuntaja/format-negotiate-interceptor)
   (ri.muuntaja/format-response-interceptor)
   (exception/exception-interceptor)
   (ri.muuntaja/format-request-interceptor)
   (coercion/coerce-response-interceptor)
   (coercion/coerce-request-interceptor)])

(defmethod ig/init-key :component/webserver
  [_ {:keys [port] :as system}]
  (let [default-conf {::server/type :jetty
                      ::server/port port
                      ::server/join? false
                      ;; no pedestal routes
                      ::server/routes []
                      ::server/secure-headers {:content-security-policy-settings
                                               {:object-src "none"}}}
        deps (select-keys system [:config :storage])
        instance (-> default-conf
                     (server/default-interceptors)
                     (pedestal/replace-last-interceptor
                      (pedestal/routing-interceptor
                       (http/router [(routes deps)]
                                    (cond-> {:resources deps
                                             :data {:muuntaja muuntaja/instance
                                                    :coercion reitit.coercion.schema/coercion
                                                    ;; Keep stock itcp for the moment
                                                    #_#_:interceptors (interceptors-stack
                                                                       deps)}}))))
                     (server/dev-interceptors)
                     (server/create-server)
                     (server/start))]
    (log/info "starting webserver component")
    (assoc system :server instance)))


(defmethod ig/halt-key! :component/webserver
  [_ {:keys [server]}]
  (when server
    (log/info "shutting down server instance")
    (server/stop server)))
