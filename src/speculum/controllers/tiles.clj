(ns speculum.controllers.tiles
  (:require [clojure.tools.logging :as log]
            [clojure.java.io :as io]
            [speculum.utils :as utils]))

;;; %%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%
;;;  Tiles
;;; %%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%

(defn mk-default [path]
  {:status 200
   :headers {"Content-Type" "image/png"}
   :body (io/input-stream
          (io/resource path))})

(defn default-texture [] (mk-default "empty.png"))
(defn error-texture [] (mk-default "error.png"))

(defn process-mirroring
  [{:keys [config storage path-params]} hash]
  (let [{:keys [tiles-providers]} config
        {:keys [vendor service x y z ext]} path-params]
    (if-let [origin (get-in tiles-providers [vendor service])]
      (let [target-fragment (format "%s/%d/%d/%d.%s" origin x y z ext)]
        (log/trace "mirroring" target-fragment)
        (error-texture))
      (error-texture))
    (error-texture)))

(defn proxify-tiles
  "Proxify & cache the Raster Tile Requests

  Params: `preview?`: don't cache and request the current
          implementation. Render only the current cached
          tiles if present in the filesystem, otherwise
          return the default tile
  "
  [{:keys [path-params query-params uri storage config] :as request}]
  (let [{:keys [vendor service x y z ext]} path-params
        {:keys [preview?]} query-params
        {:keys [tile-storage]} storage]
    (let [uri-hash (utils/ressource->hashkey uri)]
      (if preview?
        (if-let [fragment-path (get @tile-storage uri-hash)]
          {:status 200
           :headers {"Content-Type" "image/png"}
           :body (io/input-stream
                  (io/resource fragment-path))}
          ;; Else yield default texture
          (default-texture))
        (if-let [fragment-path (get @tile-storage uri-hash)]
          {:status 200
           :headers {"Content-Type" "image/png"}
           :body (io/input-stream
                  (io/resource fragment-path))}
          ;; Else fetch, store and return the fragment
          (try
            (process-mirroring request uri-hash)
            (catch Exception e
              (log/error (.getMessage e)))))))))
