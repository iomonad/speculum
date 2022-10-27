(ns speculum.components.storage
  (:require [integrant.core :as ig]
            [clojure.tools.logging :as log]
            [clojure.java.io :as io]))

;;; %%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%
;;;  Storage
;;; %%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%

(defmethod ig/init-key :component/storage
  [_ {:keys [output-directory auto-create?] :as sys}]
  (let [tile-storage (atom (hash-map))
        wms-storage  (atom (hash-map))]
    (when-not (.isDirectory (io/file output-directory))
      (if auto-create?
        (.mkdir (io/file output-directory))
        (throw (ex-info "not a directory or inexistant"
                        {:path output-directory}))))
    (log/info "starting storage component")
    (assoc sys
           :tile-storage tile-storage
           :wms-storage wms-storage)))

(defmethod ig/halt-key! :component/storage
  [_ {:keys []}]
  (log/info "stopping storage component"))
