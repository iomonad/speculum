(ns speculum.components.storage
  (:require [integrant.core :as ig]
            [clojure.tools.logging :as log]
            [clojure.java.io :as io]
            [me.raynes.fs :as fs]
            [clojure.string :as str]
            [speculum.utils :as utils]))

;;; %%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%
;;;  Storage
;;; %%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%

(def config {:component/storage
             {:config (ig/ref :component/config)
              :output-directory-tiles "out-tiles"
              :output-directory-wms "out-wms"
              :auto-create? true}})

(defn count-files [folder]
  (count (file-seq (io/file folder))))

(defn rebuild-tiles-structure!
  [folder]
  (->> (file-seq (io/file folder))
       (filter fs/file?)
       (map #(.toString %))
       (reduce (fn [acc path]
                 (assoc acc (utils/ressource->hashkey
                             (str "/tiles" (str/replace path folder "")))
                        path)) {})))

(defn rebuild-wms-structure!
  [{:keys [wms-providers]} folder]
  (->> (file-seq (io/file folder))
       (filter fs/file?)
       (map #(.toString %))
       (reduce
        (fn [acc path]
          (let [[_ vendor service] (str/split path #"/")
                implem-url (get-in wms-providers [(keyword vendor)
                                                  (keyword service)
                                                  :url])
                resolved-uri (when implem-url
                               (str/join "/"
                                         (rest (-> (str/replace implem-url #"https://" "")
                                                   (str/split #"/")))))
                file-bbox (some-> (fs/base-name path)
                             (str/replace (fs/extension path) ""))]
            (if resolved-uri
              (assoc acc (utils/ressource->hashkey
                          (str "/wms/" resolved-uri ":" file-bbox))
                     path)
              acc))) {})))


(defmethod ig/init-key :component/storage
  [_ {:keys [output-directory-tiles
             output-directory-wms auto-create?
             config] :as sys}]
  (doseq [outpath [output-directory-tiles output-directory-wms]]
    (when-not (.isDirectory (io/file outpath))
      (if auto-create?
        (.mkdir (io/file outpath))
        (throw (ex-info "not a directory or inexistant"
                        {:path outpath})))))
  (let [tile-storage (atom (rebuild-tiles-structure! output-directory-tiles))
        wms-storage  (atom (rebuild-wms-structure! config
                                                   output-directory-wms))]
    (log/infof "rebuild tile storage of size %d & wms of %d"
               (count @tile-storage) (count @wms-storage))
    (log/info "starting storage component")
    (assoc sys
           :tile-storage tile-storage
           :wms-storage wms-storage
           :ping-fn
           (fn []
             {:ok true
              :xyz {:table-sum (count @tile-storage)
                    :indexed-chunks (count-files output-directory-tiles)
                    :lag (- (count-files output-directory-tiles)
                            (count @tile-storage))}
              :wms {:table-sum (count @wms-storage)
                    :indexed-chunks (count-files output-directory-wms)
                    :lag (- (count-files output-directory-wms)
                            (count @wms-storage))}}))))

(defmethod ig/halt-key! :component/storage
  [_ {:keys []}]
  (log/info "stopping storage component"))
