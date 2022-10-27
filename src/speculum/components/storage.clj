(ns speculum.components.storage
  (:require [integrant.core :as ig]
            [clojure.tools.logging :as log]
            [clojure.java.io :as io]
            [me.raynes.fs :as fs]
            [clojure.string :as str]
            [speculum.utils :as utils]))

;;; %%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%
;;;  Storage
;;; %%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%

(defn rebuild-tiles-structure!
  [folder]
  (->> (file-seq (io/file folder))
       (filter fs/file?)
       (map #(.toString %))
       (reduce (fn [acc path]
                 (assoc acc (utils/ressource->hashkey
                             (str "/tiles" (str/replace path folder "")))
                        path)) {})))

(defmethod ig/init-key :component/storage
  [_ {:keys [output-directory auto-create?] :as sys}]
  (when-not (.isDirectory (io/file output-directory))
      (if auto-create?
        (.mkdir (io/file output-directory))
        (throw (ex-info "not a directory or inexistant"
                        {:path output-directory}))))
  (let [tile-storage (atom (rebuild-tiles-structure! output-directory))
        wms-storage  (atom (hash-map))]
    (log/info "starting storage component")
    (assoc sys
           :tile-storage tile-storage
           :wms-storage wms-storage)))

(defmethod ig/halt-key! :component/storage
  [_ {:keys []}]
  (log/info "stopping storage component"))

