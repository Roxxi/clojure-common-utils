(ns roxxi.utils.print)

(defmacro print-expr [e]
  `(let [e# ~e]
     (cond (nil? e#)
           (println
            (str "Expression " '~e " evaluates to nil\n+\n+")),
       (and (seq? e#) (not (realized? e#)))
       (println
        (str "Expression " '~e " evaluates to " (vec e#) " of type " (.getClass #^Object e#) "\n+\n+"))
       :else
       (println
        (str "Expression " '~e " evaluates to " e# " of type " (.getClass #^Object e#) "\n+\n+")))
     e#))