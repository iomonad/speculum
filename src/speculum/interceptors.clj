(ns speculum.interceptors
  (:require [clojure.tools.logging :as log]
            [io.pedestal.log :as plog]
            [reitit.interceptor :as interceptor]
            [io.pedestal.http.impl.servlet-interceptor]
            [buddy.auth :as a]
            [buddy.auth.middleware :as bm]
            [io.pedestal.interceptor.chain :as interceptor.chain]
            [io.pedestal.interceptor.error :refer [error-dispatch]])
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

;; Auth interceptor

(defn authentication-interceptor
  "Port of buddy-auth's wrap-authentication middleware."
  [backend]
  (io.pedestal.interceptor/interceptor
   {:name ::authenticate
    :enter (fn [ctx]
             (update ctx :request bm/authentication-request
                     backend))}))


(defn authorization-interceptor
  "Port of buddy-auth's wrap-authorization middleware."
  [backend]
  (error-dispatch
   [ctx ex]
   [{:exception-type :clojure.lang.ExceptionInfo :stage :enter}]
   (try
     (assoc ctx :response
            (bm/authorization-error (:request ctx)
                                    ex backend))
     (catch Exception e
       (assoc ctx ::interceptor.chain/error e)))
   :else (assoc ctx ::interceptor.chain/error ex)))


(def check-permissions
  "Check permission from current request"
  {:name ::check-permissions
   :enter
   (fn [{:keys [request] :as ctx}]
     (if (a/authenticated? request) ctx
       (-> ctx
           interceptor.chain/terminate
           (assoc :response {:status 401
                             :body {:status 401
                                    :message "Unauthorized"}}))))})
