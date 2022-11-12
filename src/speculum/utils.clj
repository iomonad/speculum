(ns speculum.utils
  (:require [buddy.core.hash :as hash]
            [buddy.core.codecs :refer [bytes->hex]]
            [clojure.tools.logging :as log]
            [me.raynes.fs :as fs]
            [clojure.java.io :as io]
            [clj-http.client :as http]))


(defn ressource->hashkey
  "Hash current path params, used to retrieve the
  cached tiles fragment on the storage."
  [ressource]
  (log/trace "hashing" ressource)
  (bytes->hex
   (hash/blake2b-512 ressource)))


(defn download-fragment!!
  ([pool target path]
   (download-fragment!! pool target path {}))
  ([{:keys [mgr factory] :as pool} target path query-params]
   ;; Ensure path exists
   (try
     (let [{:keys [status body]}
           (http/get target
                     (cond-> {:as :stream
                              :throw-exceptions false
                              :socket-timeout 3000
                              :connection-timeout 3000
                              :insecure true}
                       pool
                       (assoc :http-client factory
                              :connection-manager mgr)
                       ;; %
                       ((complement empty?) query-params)
                       (assoc :query-params query-params)))
           parent-structure (.toString (fs/parent path))]
       ;; 404 return is a server design, so just ommit
       ;; it if user is outside the AOI, to avoid useless
       ;; logs.
       (if (= 200 status)
         ;; Ensure path exists before storing the
         ;; image
         (do
           (when (fs/mkdirs parent-structure)
             (log/debugf "create tree structure - %s" parent-structure))
           (io/copy body (io/file path))
           ;; Also yield to directly render
           {:status :ok
            :code 200
            :path path})
         (do
           (log/debugf "server returned %s - %s" status target)
           {:status :warn
            :code status})))
     (catch Exception e
       (log/errorf "error while mirroring %s - %s"
                   target e)
       {:status :failure}))))


(defn mk-image [path]
  {:status 200
   :headers {"Content-Type" "image/png"}
   :body (io/input-stream
          (io/resource path))})

(defn mk-storage-image [path]
  {:status 200
   :headers {"Content-Type" "image/png"}
   :body (io/input-stream
          (io/file path))})


(defn default-texture [] (mk-image "empty.png"))
(defn error-texture [] (mk-image "error.png"))
