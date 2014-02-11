(ns roxxi.utils.time)

(def ^:dynamic *nowy-now* nil)

(defmacro with-consistent-now [& body]
  `(binding [*nowy-now* (or *nowy-now* (System/currentTimeMillis))]
    ~@body))

(defn consistent-now []
  (or *nowy-now*
      (throw
       (RuntimeException.
        (str "Attempting to get a consistent now when no "
             "now has been captured. "
             "Did you use `with-consistent-now`?")))))

(defn consistent-now-sec []
  (quot (consistent-now) 1000))
