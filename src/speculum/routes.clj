(ns speculum.routes
  (:require [speculum.controllers.ping :as ping]
            [speculum.controllers.tiles :as tiles]))

;;; %%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%
;;;  Routes
;;; %%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%

(defn routes [ressources]
  ["/ping" {:name ::ping
            :resources [:config]
            :get {:handler (ping/ping-handler
                            {:app-name "speculum"}
                            ressources)}}])
