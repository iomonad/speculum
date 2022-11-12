(ns speculum.controllers.tiles
  (:require [clojure.tools.logging :as log]
            [clojure.core.match :refer [match]]
            [speculum.utils :as utils]
            [me.raynes.fs :as fs]))

;;; %%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%
;;;  Tiles
;;; %%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%

(defn- exists-in-path?
  [{:keys [output-directory-tiles]}
   {:keys [vendor service z x y]}]
  (let [target (format "%s/%s/%s/%s/%s/%s.png" output-directory-tiles
                       vendor service x y z)]
    {:path-exists? (fs/exists? target)
     :hypothetical-path target}))


(defn process-mirroring
  [{:keys [config storage path-params]}]
  (let [{:keys [tiles-providers pool]} config
        {:keys [output-directory-tiles]} storage
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
            (utils/mk-storage-image path)

            (= 404 code)
            {:status 404
             :body "Not Found"}

            :else (utils/error-texture)))
        {:status 404
         :body {:message "please double-check your configuration"
                :status :not-found
                :vendor vendor
                :service service
                :params path-params}})
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
  [{:keys [query-params storage path-params] :as request}]
  (let [{:keys [preview?]} query-params
        {:keys [path-exists?
                hypothetical-path]}
        (exists-in-path? storage path-params)]
    (match [path-exists? (some? preview?)]
           [true         _]     (utils/mk-storage-image hypothetical-path)
           [false        true]  (utils/default-texture)
           [false        false] (process-mirroring request)
           :else {:status 400
                  :body "bad request"})))
