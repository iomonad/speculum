(ns speculum.controllers.wms
  (:require [speculum.utils :as utils]
            [clojure.tools.logging :as log]))

(defn process-mirroring
  [{:keys [config storage path-params query-params]} hash]
  (let [{:keys [wms-providers pool]} config
        {:keys [vendor service]} path-params
        {:keys [output-directory-wms wms-storage]} storage
        {:keys [bbox layers]} query-params]
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
      (utils/error-texture))))


(defn proxify-wms
  [{:keys [query-params uri storage] :as request}]
  (let [{:keys [bbox preview?]} query-params
        {:keys [wms-storage]} storage
        seed (str uri ":" bbox)
        uri-hash (utils/ressource->hashkey seed)]
    (if preview?
      (if-let [fragment-path (get @wms-storage uri-hash)]
        (utils/mk-storage-image fragment-path)
        (utils/default-texture))
      (if-let [fragment-path (get @wms-storage uri-hash)]
        (utils/mk-storage-image fragment-path)
        (process-mirroring request uri-hash)))))
