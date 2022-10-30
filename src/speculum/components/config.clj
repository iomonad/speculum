(ns speculum.components.config
  (:require [integrant.core :as ig]
            [clj-http.conn-mgr :as cmgr]))

;;; %%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%
;;;  Config
;;; %%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%

(def config {:component/config
             {:share-providers? false}})

(defmethod ig/init-key :component/config
  [_ {:keys [tiles-providers wms-providers
             share-providers?] :as sys}]
  (let [pool (cmgr/make-reusable-conn-manager
              {:timeout 5
               :threads 4
               :default-per-route 10
               :insecure? true})]
    (assoc sys
           :pool pool
           :ping-fn (fn []
                      (cond-> {:ok true}
                        share-providers?
                        (assoc :providers {:tiles tiles-providers
                                           :wms wms-providers}))))))
