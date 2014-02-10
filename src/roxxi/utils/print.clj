(ns roxxi.utils.print)

(defmacro print-expr [e]
  `(let [e# ~e]
     (cond (nil? e#)
           (println
            (str "Expression " '~e " evaluates to nil\n+\n+")),
           (and (seq? e#))
           (println
            (str "Expression " '~e " evaluates to " (vec e#) " of type " (.getClass #^Object e#) "\n+\n+")),
           :else
           (println
            (str "Expression " '~e " evaluates to " e# " of type " (.getClass #^Object e#) "\n+\n+")))
     e#))

(defn- process-form-recursive [form macro-name & {:keys [hella]
                                                  :or {hella false}}]
  (cond
   (list? form)
   `(~macro-name ~(map #(process-form-recursive % macro-name :hella hella) form))
   hella
   `(~macro-name ~form)
   :else
   form))
(defmacro print-expr-rec [form]
  (process-form-recursive form 'print-expr))
(defmacro print-expr-hella-rec [form]
  (process-form-recursive form 'print-expr :hella true))

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
