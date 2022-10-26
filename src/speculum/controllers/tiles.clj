(ns speculum.controllers.tiles
  (:require [clojure.tools.logging :as log]
            [clojure.java.io :as io]))

;;; %%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%
;;;  Tiles
;;; %%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%

(defn proxify-tiles
  "Proxy Handler"
  [{:keys [path-params uri] :as request}]
  (log/info "got proxy request" uri)
  {:status 200
   :headers {"Content-Type" "image/png"}
   :body (io/input-stream
          (io/resource "empty.png"))})
