(ns speculum.controllers.tiles
  (:require [clojure.tools.logging :as log]
            [clojure.java.io :as io]))

;;; %%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%
;;;  Tiles
;;; %%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%

(defn proxify-tiles
  "Proxify & cache the Raster Tile Requests

  Params: `preview?`: don't cache and request the current
          implementation. Render only the current cached
          tiles if present in the filesystem, otherwise
          return the default tile
  "
  [{:keys [path-params query-params uri] :as request}]
  (let [{:keys [vendor service x y z ext]} path-params
        {:keys [preview?]} query-params]
    (if preview?
      (log/info "got proxy request in preview mode" uri)
      (log/info "got proxy request" uri))
    {:status 200
     :headers {"Content-Type" "image/png"}
     :body (io/input-stream
            (io/resource "empty.png"))}))
