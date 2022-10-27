(ns speculum.interceptors)

(defn speculum-context [system]
  {:name ::speculum-context
   :enter (fn [ctx]
            (update ctx :request merge system))})
