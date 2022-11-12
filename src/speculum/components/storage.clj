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

(def config {:component/storage
             {:config (ig/ref :component/config)
              :output-directory-tiles "out-tiles"
              :output-directory-wms "out-wms"
              :auto-create? true}})

(defn count-files [folder]
  (count (file-seq (io/file folder))))

(defmethod ig/init-key :component/storage
  [_ {:keys [output-directory-tiles
             output-directory-wms auto-create?] :as sys}]
  (doseq [outpath [output-directory-tiles output-directory-wms]]
    (when-not (.isDirectory (io/file outpath))
      (if auto-create?
        (.mkdir (io/file outpath))
        (throw (ex-info "not a directory or inexistant"
                        {:path outpath})))))
  (assoc sys
         :ping-fn
         (fn []
           {:ok true
            :indexed-chunks {:xyz (count-files output-directory-tiles)
                             :wms (count-files output-directory-wms)}})))

(defmethod ig/halt-key! :component/storage
  [_ {:keys []}]
  (log/info "stopping storage component"))
