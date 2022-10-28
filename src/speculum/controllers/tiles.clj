(ns speculum.controllers.tiles
  (:require [clojure.tools.logging :as log]
            [clojure.java.io :as io]
            [speculum.utils :as utils]))

;;; %%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%
;;;  Tiles
;;; %%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%

(defn process-mirroring
  [{:keys [config storage path-params]} hash]
  (let [{:keys [tiles-providers pool]} config
        {:keys [output-directory-tiles tile-storage]} storage
        {:keys [vendor service x y z ext]} path-params]
    (if-let [origin (get-in tiles-providers
                            [(keyword vendor) (keyword service) :url])]
      (let [path-structure (format "%s/%s/%s.%s" x y z ext)
            fs-structure (format "%s/%s/%s/%s" output-directory-tiles
                                 vendor service path-structure)
            target-fragment (str origin path-structure)]
        ;; request & store tile
        (if-let [mirrored-path (utils/download-fragment!! pool target-fragment
                                                          fs-structure)]
          ;; If successfully mirrored, serve it...
          (do
            (swap! tile-storage assoc hash mirrored-path)
            (utils/mk-storage-image mirrored-path))
          (utils/error-texture)))
      (utils/error-texture))))


(defn proxify-tiles
  "Proxify & cache the Raster Tile Requests

  Params: `preview?`: don't cache and request the current
          implementation. Render only the current cached
          tiles if present in the filesystem, otherwise
          return the default tile
  "
  [{:keys [query-params uri storage] :as request}]
  (let [{:keys [preview?]} query-params
        {:keys [tile-storage]} storage
        uri-hash (utils/ressource->hashkey uri)]
    (if preview?
      (if-let [fragment-path (get @tile-storage uri-hash)]
        (utils/mk-storage-image fragment-path)
        ;; Else yield default texture
        (utils/default-texture))
      (if-let [fragment-path (get @tile-storage uri-hash)]
        (utils/mk-storage-image fragment-path)
        ;; Else fetch, store and return the fragment
        (try
          (process-mirroring request uri-hash)
          (catch Exception e
            (log/error (.getMessage e))))))))
