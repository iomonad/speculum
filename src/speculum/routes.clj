(ns speculum.routes
  (:require [speculum.controllers.ping :as ping]
            [speculum.controllers.tiles :as tiles]
            [schema.core :as s]))

;;; %%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%
;;;  Routes
;;; %%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%

(defn routes [ressources]
  [["/ping" {:name ::ping
             :get {:handler (ping/ping-handler
                             {:app-name "speculum"}
                             ressources)}}]
   ;; =======================================
   ["/tiles/:vendor/:service/:x/:y/{z}.{ext}"
    {:name ::tiles-proxy
     :get {:handler tiles/proxify-tiles
           :parameters {:path {:vendor s/Str
                               :service s/Str
                               :x s/Int
                               :y s/Int
                               :z s/Int}}}}]])
