(ns speculum.components.config
  (:require [integrant.core :as ig]
            [clj-http.conn-mgr :as cmgr]
            [clj-http.core :as hc]
            [clojure.tools.logging :as log]
            [buddy.auth.backends :as ab]))

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
              :caching? false
              :realm? false
              :realm-name "Speculum"}})

(defmethod ig/init-key :component/config
  [_ {:keys [tiles-providers wms-providers
             share-providers? timeout threads
             default-per-route insecure? caching?
             realm? realm-username realm-password
             realm-name] :as sys}]
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
                 manager)
        realm-enabled? (or realm? (and realm-username
                                       realm-password))
        realm (if realm-enabled?
                (ab/basic {:realm realm-name
                           :authfn (fn [_request {:keys [username password]}]
                                     (when (and (= username realm-username)
                                                (= password  realm-password))
                                       :ok))})
                (log/warn "authentification realm is disabled!"))]
    (log/info "initializing config component")
    (cond-> (assoc sys
                   :pool {:mgr manager
                          :factory factory}
                   :ping-fn (fn []
                              (cond-> {:ok true}
                                share-providers?
                                (assoc :providers {:tiles tiles-providers
                                                   :wms wms-providers}))))
      realm-enabled? (assoc :realm realm))))

(defmethod ig/halt-key! :component/config
  [_ {:keys [pool]}]
  (log/info "shutting down config component")
  (let [{:keys [mgr]} pool]
    (when mgr
      (log/info "closing connection manager")
      (cmgr/shutdown-manager mgr))))
