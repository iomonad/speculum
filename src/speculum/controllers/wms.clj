(ns speculum.controllers.wms
  (:require [speculum.utils :as utils]
            [clojure.tools.logging :as log]
            [clojure.core.match :refer [match]]
            [me.raynes.fs :as fs]))

;;; %%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%
;;;  WMS
;;; %%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%

(defn- exists-in-path?
  [{:keys [output-directory-wms]}
   {:keys [vendor service]}
   {:keys [bbox layers]}]
  (let [target (format "%s/%s/%s/%s/%s.png" output-directory-wms
                       vendor service layers bbox)]
    {:path-exists? (fs/exists? target)
     :hypothetical-path target}))

(defn process-mirroring
  [{:keys [config storage path-params query-params]} hash]
  (let [{:keys [wms-providers pool]} config
        {:keys [vendor service]} path-params
        {:keys [output-directory-wms wms-storage]} storage
        {:keys [bbox layers]} query-params]
    (try
      (if-let [origin (get-in wms-providers
                              [(keyword vendor) (keyword service) :url])]
        (let [local-path (format "%s/%s/%s/%s/%s.png"
                                 output-directory-wms vendor service layers bbox)
              {:keys [status path code]}
              (utils/download-fragment!! pool origin
                                         local-path query-params)]
          (cond
            (= :ok status)
            ;; If successfully mirrored, serve it...
            (do
              (swap! wms-storage assoc hash path)
              (utils/mk-storage-image path))

            (= 404 code)
            {:status 404
             :body "Not Found"}

            :else (utils/error-texture)))
        (utils/error-texture))
      (catch Exception e
        (log/warn (.getMessage e))
        {:status 500
         :body (.getMessage e)}))))


(defn proxify-wms
  [{:keys [query-params uri storage path-params] :as request}]
  (let [{:keys [bbox preview?]} query-params
        {:keys [wms-storage]} storage
        seed (str uri ":" bbox)
        uri-hash (utils/ressource->hashkey seed)
        fragment-path (get @wms-storage uri-hash)
        {:keys [path-exists?
                hypothetical-path]}
        (exists-in-path? storage path-params query-params)]
    (match [path-exists? (some? preview?) (some? fragment-path)]
           [true                _ _]  (utils/mk-storage-image hypothetical-path)
           [_ true             true]  (utils/mk-storage-image fragment-path)
           [_ true             false] (utils/default-texture)
           [_ false            true]  (utils/mk-storage-image fragment-path)
           [_ false            false] (process-mirroring request uri-hash))))
