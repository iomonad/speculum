(ns speculum.components.webserver
  (:require [integrant.core :as ig]
            [clojure.tools.logging :as log]
            [io.pedestal.http :as server]
            [reitit.pedestal :as pedestal]
            [reitit.http :as http]
            [speculum.routes :refer [routes
                                     preview-routes]]
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

(def config
  {:component/webserver
   {:port 3000
    :logging (ig/ref :component/logging)
    :storage (ig/ref :component/storage)
    :config (ig/ref :component/config)}})

(defn interceptors-stack
  [{:keys [config] :as components}]
  (let [{:keys [realm? realm]} config]
    (cond-> [(itcp/speculum-context components)
             (ri.parameters/parameters-interceptor)
             (ri.muuntaja/format-negotiate-interceptor)
             (ri.muuntaja/format-response-interceptor)
             (exception/exception-interceptor)
             (ri.muuntaja/format-request-interceptor)]
      ;; Add auth interceptors if enabled in spec
      realm? (concat [(itcp/authentication-interceptor realm)
                      (itcp/authorization-interceptor  realm)
                      itcp/check-permissions]))))

(defmethod ig/init-key :component/webserver
  [_ {:keys [port preview?] :as system}]
  (with-redefs [io.pedestal.http.impl.servlet-interceptor/stylobate itcp/stylobate]
    (let [default-conf {::server/type :jetty
                        ::server/port port
                        ::server/host "0.0.0.0"
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
                         (http/router [(if preview?
                                         (vec (concat (routes deps) preview-routes))
                                         (routes deps))]
                                      (cond-> {:resources deps
                                               :data {:muuntaja muuntaja/instance
                                                      :coercion reitit.coercion.schema/coercion
                                                      ;; Keep stock itcp for the moment
                                                      :interceptors (interceptors-stack
                                                                     deps)}}))))
                       (server/dev-interceptors)
                       (server/create-server)
                       (server/start))]
      (log/infof "starting webserver component on port %s" port)
      (assoc system :server instance))))


(defmethod ig/halt-key! :component/webserver
  [_ {:keys [server]}]
  (when server
    (log/info "shutting down server instance")
    (server/stop server)))
