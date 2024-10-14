(ns speculum.components.config
  (:require [integrant.core :as ig]
            [clj-http.conn-mgr :as cmgr]
            [clj-http.core :as hc]
            [clojure.tools.logging :as log]))

;;; %%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%
;;;  Config
;;; %%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%

(def config {:component/config
             {:logging (ig/ref :component/logging)
              :share-providers? false
              :use-pool? false
              :timeout 5
              :threads 20
              :default-per-route 10
              :insecure? true
              :caching? false}})

(defmethod ig/init-key :component/config
  [_ {:keys [tiles-providers wms-providers
             share-providers? timeout threads
             default-per-route insecure? caching?] :as sys}]
  (let [manager
        (cmgr/make-reusable-conn-manager
         {:timeout timeout
          :threads threads
          :default-per-route default-per-route
          :insecure? insecure?})
        factory (hc/build-http-client
                 {:socket-timeout 10000
                  :connection-timeout 10000
                  :insecure insecure?}
                 caching?
                 manager)]
    (log/info "initializing config component")
    (cond-> (assoc sys
                   :pool {:mgr manager
                          :factory factory}
                   :ping-fn (fn []
                              (cond-> {:ok true}
                                share-providers?
                                (assoc :providers {:tiles tiles-providers
                                                   :wms wms-providers})))))))

(defmethod ig/halt-key! :component/config
  [_ {:keys [pool]}]
  (log/info "shutting down config component")
  (let [{:keys [mgr]} pool]
    (when mgr
      (log/info "closing connection manager")
      (cmgr/shutdown-manager mgr))))
