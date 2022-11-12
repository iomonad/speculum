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
  [{:keys [config storage path-params query-params]}]
  (let [{:keys [wms-providers pool]} config
        {:keys [vendor service]} path-params
        {:keys [output-directory-wms]} storage
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
            (utils/mk-storage-image path)

            (= 404 code)
            {:status 404
             :body "Not Found"}

            :else (utils/error-texture)))
        {:status 404
         :body {:status :not-found
                :message "please double-check your configuration"
                :vendor vendor
                :service service
                :params query-params}})
      (catch Exception e
        (log/warn (.getMessage e))
        {:status 500
         :body (.getMessage e)}))))


(defn proxify-wms
  [{:keys [query-params storage path-params] :as request}]
  (let [{:keys [preview?]} query-params
        {:keys [path-exists?
                hypothetical-path]}
        (exists-in-path? storage path-params query-params)]
    (match [path-exists? (some? preview?)]
           [true         _]     (utils/mk-storage-image hypothetical-path)
           [false        true]  (utils/default-texture)
           [false        false] (process-mirroring request)
           :else {:status 400
                  :body "bad request"})))
