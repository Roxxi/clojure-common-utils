(ns roxxi.utils.set)

(defn set-over [& xs]
  (persistent!
   (reduce #(conj! %1 %2) (transient #{}) xs)))
