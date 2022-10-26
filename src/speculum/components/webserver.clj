(ns speculum.components.webserver
  (:require [integrant.core :as ig]
            [clojure.tools.logging :as log]
            [io.pedestal.http :as server]
            [reitit.pedestal :as pedestal]
            [reitit.http :as http]
            [speculum.routes :refer [routes]]
            [muuntaja.core :as muuntaja]
            [reitit.http.interceptors
             [parameters :as ri.parameters]
             [muuntaja   :as ri.muuntaja]]))

;;; %%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%
;;;  Webserver
;;; %%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%

(def interceptors-stack
  [(ri.parameters/parameters-interceptor)
   (ri.muuntaja/format-interceptor)
   (ri.muuntaja/format-response-interceptor)])

(defmethod ig/init-key :component/webserver
  [_ {:keys [port] :as system}]
  (let [default-conf {::server/type :jetty
                      ::server/port port
                      ::server/join? false
                      ;; no pedestal routes
                      ::server/routes []}
        deps (select-keys system [:config])
        muuntaja-formatter (muuntaja/create
                              (assoc-in
                               muuntaja/default-options
                               [:formats "application/json" :encoder-opts]
                               {:date-format "yyyy-MM-dd'T'HH:mm:ss.SSSX"}))
        instance (-> default-conf
                     (server/default-interceptors)
                     (pedestal/replace-last-interceptor
                      (pedestal/routing-interceptor
                       (http/router [(routes deps)]
                                    (cond-> {:resources deps
                                             :data {:muuntaja muuntaja-formatter
                                                    :interceptor interceptors-stack}}))))
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
