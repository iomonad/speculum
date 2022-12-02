(ns speculum.generators
  (:require [clojure.core.matrix :as m]
            [clojure.core.async :as a]))


(defn- generate-level
  "Generate a flat worklist of selected
   range"
  [z [gx gy] [tx ty]]
  (for [lx (range gx tx)
        ly (range gy ty)]
    [z lx ly]))

(defn- level->chan
  [z genese target]
  (-> (generate-level z genese target)
      (a/into (a/chan))))

(comment
  (level->chan 1 [2 2] [7 7])
  (count (shuffle (generate-level 1 [2 2] [200 200])))
  )
