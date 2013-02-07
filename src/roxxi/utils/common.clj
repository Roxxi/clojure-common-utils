(ns roxxi.utils.common
  "This is kind of the junk drawer file for things that don't [yet] fit cleanly elsewhere"
  )


(defn only-n-times [n]
  (let [counter (atom 0)]
    (fn []
      (if (< @counter n)
        (do (swap! counter inc)
            true)
        false))))