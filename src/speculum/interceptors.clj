(ns speculum.interceptors
  (:require [clojure.tools.logging :as log]
            [clojure.stacktrace :as stack]
            [io.pedestal.log :as plog]
            [reitit.interceptor :as interceptor]
            [io.pedestal.http.impl.servlet-interceptor]
            [io.pedestal.interceptor :as pd]
            [macrometer
             [timers :as t]
             [gauges :as g]]
            [speculum.utils :refer [success?]])
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
  (let [cause (stack/root-cause exception)]
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
#_(defonce stylobate
    (io.pedestal.interceptor/interceptor
     {:name ::stylobate
      :enter @#'io.pedestal.http.impl.servlet-interceptor/enter-stylobate
      :leave @#'io.pedestal.http.impl.servlet-interceptor/leave-stylobate
      :error error-stylobate}))

;;; Monitoring

(defn route->name [{route-name :name}]
  (cond
    (string? route-name) route-name
    (simple-keyword? route-name) (name route-name)
    (qualified-keyword? route-name) (str (namespace route-name) "." (name route-name))
    :else :unknown))

(defonce ^:private active-requests (atom {}))
(defn- active-reqs [route] (get @active-requests route))
(defn compiled-add-metrics
  "Adds several micrometer metrics to the request.

   Ugly kw opts for now, we should have settings for different monitoring systems"
  [{:keys [registry success-pred timer-opts with-status?] :as conf}]
  (when conf
    {:name ::add-metrics
     :compile (fn compile-add-metrics [{:keys [no-metrics] :as data} _]
                (let [route-name (route->name data)]
                  (when-not no-metrics
                    {:enter (fn [ctx]
                              (let [tok (t/start registry)]
                                (g/gauge "http.requests.active.count"
                                         (partial active-reqs route-name)
                                         :registry registry
                                         :tags {:route route-name})
                                (swap! active-requests update route-name (fnil inc 0))
                                (assoc ctx ::timer tok ::route route-name)))
                     :leave (fn [ctx]
                              (let [status (or (str (get-in ctx [:response :status])) "unknown")
                                    success (str (boolean (success? ctx success-pred)))
                                    timer (apply t/timer "http.requests"
                                                 :registry registry
                                                 :tags (cond-> {:route route-name
                                                                :success success}
                                                         with-status? (assoc :status status))
                                                 (mapcat identity timer-opts))
                                    tok (::timer ctx)
                                    chrono (t/stop timer tok)
                                    latency (int (/ chrono 1000000.0))]
                                (swap! active-requests update route-name dec)
                                (update ctx :request assoc :latency latency)))})))}))
