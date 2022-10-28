(ns speculum.routes
  (:require [speculum.controllers.ping :as ping]
            [speculum.controllers.tiles :as tiles]
            [ring.util.response :as r]
            [schema.core :as s]
            [speculum.interceptors :as itcp]
            [speculum.controllers.wms :as wms]))

;;; %%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%
;;;  Routes
;;; %%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%

(defn routes [ressources]
  [["/" {:name ::root
         :get {:handler (fn [_]
                          (-> (r/resource-response "index.html"
                                                   {:root "public"})
                              (r/content-type "text/html")))}}]
   ["/ping" {:name ::ping
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
                               :z s/Int}
                        :query {:preview? s/Bool}}}}]
   ["/wms/:vendor/:service/ows"
    {:name ::wms-proxy
     :get {:handler wms/proxify-wms
           :parameters {:path {:vendor s/Str
                               :service s/Str}
                        :query {:preview? s/Bool}}}}]])
