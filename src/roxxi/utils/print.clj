(ns roxxi.utils.print)

(defmacro print-expr [e]
  `(let [e# ~e]
     (println (str "Expression " '~e " evaluates to " e# " of type " (.getClass e#) "\n+\n+"))
     e#))