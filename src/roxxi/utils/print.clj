(ns roxxi.utils.print)

(defmacro print-expr [e]
  `(let [e# ~e]
     (println (str "Expression " '~e " evaluates to " e# " of type " (.getClass #^Object e#) "\n+\n+"))
     e#))