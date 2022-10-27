(ns speculum.utils
  (:require [buddy.core.hash :as hash]
            [buddy.core.codecs :refer [bytes->hex]]
            [clojure.tools.logging :as log]))


(defn ressource->hashkey
  "Hash current path params, used to retrieve the
  cached tiles fragment on the storage."
  [ressource]
  (log/trace "hashing" ressource)
  (bytes->hex
   (hash/blake2b-512 ressource)))
