(ns speculum.components.metrics
  (:require [integrant.core :as ig]
            [clojure.tools.logging :as log])
  (:import io.micrometer.core.instrument.Metrics
           io.micrometer.prometheus.PrometheusConfig
           io.micrometer.prometheus.PrometheusMeterRegistry
           io.micrometer.core.instrument.Counter
           io.micrometer.core.instrument.Counter$Builder
           io.micrometer.core.instrument.Gauge
           io.micrometer.core.instrument.MeterRegistry
           io.micrometer.core.instrument.MeterRegistry$Config
           io.micrometer.core.instrument.Metrics
           io.micrometer.core.instrument.Timer
           io.micrometer.core.instrument.Timer$Builder
           io.micrometer.core.instrument.binder.jvm.ClassLoaderMetrics
           io.micrometer.core.instrument.binder.jvm.JvmGcMetrics
           io.micrometer.core.instrument.binder.jvm.JvmMemoryMetrics
           io.micrometer.core.instrument.binder.jvm.JvmThreadMetrics
           io.micrometer.core.instrument.binder.system.FileDescriptorMetrics
           io.micrometer.core.instrument.binder.system.UptimeMetrics
           io.micrometer.core.instrument.binder.system.ProcessorMetrics)
  (:import java.util.concurrent.TimeUnit
           java.util.function.Supplier))

;;; %%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%
;;;  Metrics
;;; %%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%

(defn- ->tags
  "Converts a map of tags to an array of string"
  [tags]
  (into-array String
              (->> tags
                   (map (fn [[k v]] [(name k) (name v)]))
                   flatten)))

(defn mk-registry
  "Build a Micrometer premetheus registry"
  [tags]
  (let [^PrometheusMeterRegistry registry (PrometheusMeterRegistry. PrometheusConfig/DEFAULT)]
    (.commonTags ^MeterRegistry$Config
                 (.config registry)
                 ^"[Ljava.lang.String;" (->tags tags))
    (Metrics/addRegistry registry)
    (.bindTo (ClassLoaderMetrics.) registry)
    (.bindTo (JvmGcMetrics.) registry)
    (.bindTo (JvmMemoryMetrics.) registry)
    (.bindTo (JvmThreadMetrics.) registry)
    (.bindTo (FileDescriptorMetrics.) registry)
    (.bindTo (UptimeMetrics.) registry)
    (.bindTo (ProcessorMetrics.) registry)
    registry))


(defn get-timer!
  "get a timer by name and tags"
  [^MeterRegistry registry n tags]
  (.register ^Timer$Builder (doto (Timer/builder (name n))
                              (.publishPercentiles (double-array [0.5 0.75 0.98 0.99]))
                              (.tags ^"[Ljava.lang.String;" (->tags tags)))
             registry))


(defn record
  [^MeterRegistry registry n tags duration]
  (when registry
    (let [timer (get-timer! registry n tags)]
      (.record ^Timer timer duration TimeUnit/MILLISECONDS))))


(defmacro with-time
  [^MeterRegistry registry n tags & body]
  `(if ~registry
     (let [^Timer timer# (get-timer! ~registry ~n ~tags)
           current# (java.time.Instant/now)]
       (try
         (do ~@body)
         (finally
           (let [end# (java.time.Instant/now)]
             (.record timer# (java.time.Duration/between current# end#))))))
     (do ~@body)))


(defn get-counter!
  [^MeterRegistry registry n tags]
  (.register ^Counter$Builder
             (doto (Counter/builder (name n))
               (.tags ^"[Ljava.lang.String;" (->tags tags)))
             registry))


(defn increment!
  "increments a counter"
  ([^MeterRegistry registry counter tags]
   (increment! registry counter tags 1))
  ([^MeterRegistry registry counter tags n]
   (when registry
     (let [builder (doto (Counter/builder (name counter))
                     (.tags ^"[Ljava.lang.String;" (->tags tags)))
           counter (.register builder registry)]
       (.increment counter n)))))


(defn- ^Supplier gauge-fn
  [producer-fn]
  (reify Supplier
    (get [this]
      (producer-fn))))


(defn gauge!
  [^MeterRegistry registry gauge tags producer-fn]
  (when registry
    (doto (Gauge/builder (name gauge) (gauge-fn producer-fn))
      (.strongReference true)
      (.tags ^"[Ljava.lang.String;" (->tags tags))
      (.register registry))))


(defn scrape
  [^PrometheusMeterRegistry registry]
  (some-> registry .scrape))


;;; Integrant

(def config
  {:component/config {:registry-opts {:app :gtekportail}
                      :route "/metrics"
                      :enabled? true}})


(defmethod ig/init-key :component/metrics
  [_ {:keys [registry-opts] :as sys}]
  (let [registry (mk-registry registry-opts)]
    ;; Custom Gauge go here
    (log/info "starting metrics component")
    (assoc sys :registry registry)))


(defmethod ig/halt-key! :component/metrics
  [_ {:keys []}]
  (log/info "starting metrics component"))
