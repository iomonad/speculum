(ns speculum.components.workqueue
  (:require [integrant.core :as ig]
            [clojure.core.async
             :as a
             :refer [>! <! >!! <!! go chan buffer close! thread
                     alts! alts!! take! put! timeout]]
            [clojure.tools.logging :as log]))

;;; %%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%
;;;  Workqueue
;;; %%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%

(def config {:component/workqueue {}})

(defmethod ig/init-key :component/workqueue
  [_ {:keys [] :as sys}]
  (log/info "starting workqueue component")
  (let [queue (a/chan)]
    (assoc sys :queue queue)))

(defmethod ig/halt-key! :component/workqueue
  [_ {:keys [queue] :as sys}]
  (log/info "stopping workqueue component")
  (when queue
    (a/close! queue)))
