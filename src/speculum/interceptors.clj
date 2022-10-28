(ns speculum.interceptors
  (:require [clojure.tools.logging :as log]
            [io.pedestal.log :as plog]
            [reitit.interceptor :as interceptor]
            [io.pedestal.http.impl.servlet-interceptor])
  (:import (io.pedestal.interceptor Interceptor)))

(extend-protocol interceptor/IntoInterceptor
  Interceptor
  (into-interceptor [this data opts]
    (interceptor/into-interceptor (into {} this) data opts)))

(defn speculum-context [system]
  {:name ::speculum-context
   :enter (fn [ctx]
            (update ctx :request merge system))})

;; Filtering out Broken pipe reporting
;; io.pedestal.http.impl.servlet-interceptor/error-stylobate
(defn error-stylobate [{:keys [servlet-response] :as context} exception]
  (let [cause (clojure.stacktrace/root-cause exception)]
    (if (and (instance? java.io.IOException cause)
             (or (= "Broken pipe" (.getMessage cause))
                 (= "Connection reset by peer" (.getMessage cause))))
      (log/debugf "Ignoring java.io.IOException: %s" (.getMessage cause))
      (plog/error
       :msg "error-stylobate triggered"
       :exception exception
       :context context))
    (@#'io.pedestal.http.impl.servlet-interceptor/leave-stylobate context)))

;; io.pedestal.http.impl.servlet-interceptor/stylobate
(def stylobate
  (io.pedestal.interceptor/interceptor
   {:name ::stylobate
    :enter @#'io.pedestal.http.impl.servlet-interceptor/enter-stylobate
    :leave @#'io.pedestal.http.impl.servlet-interceptor/leave-stylobate
    :error error-stylobate}))
