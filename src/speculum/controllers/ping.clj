(ns speculum.controllers.ping
  (:require [clojure.tools.logging :as log]))

;;; %%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%
;;;  Ping
;;; %%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%

(defn ping-handler [app-infos resources]
  (let [pingable (filter (comp :ping-fn second) resources)]
    (fn [_]
      (let [pings (->> pingable
                       (map (fn [[k {ping :ping-fn}]]
                              (try
                                (let [start (System/currentTimeMillis)
                                      r (ping)
                                      delay (- (System/currentTimeMillis) start)]
                                  [k (assoc r :delay delay)])
                                (catch Exception e
                                  (log/error "ping" k (.getMessage e))
                                  {k {:ok false
                                      :message (.getMessage e)}}))))
                       (into {}))
            ok? (every? (comp :ok val) pings)]
        (log/trace "ping call handled")
        {:status (if ok? 200 599)
         :body
         (merge app-infos
                {:ok         ok?
                 :hostname   (.getHostName (java.net.InetAddress/getLocalHost))
                 :resources  (keys resources)
                 :components pings})}))))

