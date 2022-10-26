(ns speculum.components.config
  (:require [integrant.core :as ig]))

;;; %%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%
;;;  Config
;;; %%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%

(defmethod ig/init-key :component/config
  [_ {:keys [providers] :as sys}]
  sys)
