(ns speculum.controllers.tiles
  (:require [clojure.tools.logging :as log]
            [clojure.core.match :refer [match]]
            [speculum.utils :as utils]))

;;; %%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%
;;;  Tiles
;;; %%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%

(defn process-mirroring
  [{:keys [config storage path-params]} hash]
  (let [{:keys [tiles-providers pool]} config
        {:keys [output-directory-tiles tile-storage]} storage
        {:keys [vendor service x y z ext]} path-params]
    (try
      (if-let [origin (get-in tiles-providers
                              [(keyword vendor) (keyword service) :url])]
        (let [path-structure (format "%s/%s/%s.%s" x y z ext)
              fs-structure (format "%s/%s/%s/%s" output-directory-tiles
                                   vendor service path-structure)
              target-fragment (str origin path-structure)
              {:keys [status path code]}
              (utils/download-fragment!! pool target-fragment
                                         fs-structure)]
          ;; request & store tile
          (cond
            (= :ok status)
            ;; If successfully mirrored, serve it...
            (do
              (swap! tile-storage assoc hash path)
              (utils/mk-storage-image path))

            (= 404 code)
            {:status 404
             :body "Not Found"}

            :else (utils/error-texture)))
        (utils/error-texture))
      (catch Exception e
        (log/error (.getMessage e))
        {:status 500
         :body (.getMessage e)}))))


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
        uri-hash (utils/ressource->hashkey uri)
        fragment-path (get @tile-storage uri-hash)]
    (match [(some? preview?) (some? fragment-path)]
           ;; Serve the current in preview mode
           [true             true]  (utils/mk-storage-image fragment-path)
           [true             false] (utils/default-texture)
           ;; Serve the current in mirrored mode
           [false            true]  (utils/mk-storage-image fragment-path)
           ;; Mirror the inexistent fragment
           [false            false] (process-mirroring request uri-hash))))
