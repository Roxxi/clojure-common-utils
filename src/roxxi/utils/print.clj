(ns roxxi.utils.print)

(defmacro print-expr [e]
  `(let [e# ~e]
     (cond (nil? e#)
           (println
            (str "Expression " '~e " evaluates to nil\n+\n+")),
           (and (seq? e#))
           (println
            (str "Expression " '~e " evaluates to " (doall e#) " of type " (.getClass #^Object e#) "\n+\n+")),
           :else
           (println
            (str "Expression " '~e " evaluates to " e# " of type " (.getClass #^Object e#) "\n+\n+")))
     e#))

;; for primitives without type hinting
(defmacro print-prim [e]
  `(let [e# ~e]
     (cond (nil? e#)
           (println
            (str "Expression " '~e " evaluates to nil\n+\n+")),
           :else
           (println
            (str "Expression " '~e " evaluates to " e# "\n+\n+")))
     e#))


(defmacro multi-line-str [& strs]
  (clojure.string/join " " strs))


        

