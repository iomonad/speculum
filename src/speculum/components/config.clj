(ns speculum.components.config
  (:require [integrant.core :as ig]
            [clj-http.conn-mgr :as cmgr]))

;;; %%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%
;;;  Config
;;; %%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%

(defmethod ig/init-key :component/config
  [_ {:keys [tiles-providers wms-providers] :as sys}]
  (let [pool (cmgr/make-reusable-conn-manager
              {:timeout 5
               :threads 4
               :default-per-route 10
               :insecure? true})]
    (assoc sys
           :pool pool
           :ping-fn (fn []
                      {:ok true
                       :providers {:tiles tiles-providers
                                   :wms wms-providers}}))))
