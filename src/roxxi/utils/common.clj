(ns roxxi.utils.common
  "This is kind of the junk drawer file for things that don't [yet]
fit cleanly elsewhere"
  {:author "Alex Bahouth, Matt Halverson"
   :date "Dec 2012"})

(defn only-n-times [n]
  (let [counter (atom 0)]
    (fn []
      (if (< @counter n)
        (do (swap! counter inc)
            true)
        false))))

(defmacro def-
  "same as def, yielding non-public def, except you can also
put doc strings on vars!"
  ([name val]
     `(def- ~name nil ~val))
  ([name doc-str val]
     `(def ~(with-meta name {:private true :doc doc-str}) ~val)))

(defmacro acond
  "Takes a set of test/expr pairs. Evaluates each test (one at a time, in
order). If a test evaluates to logical true, cond-let evaluates and returns
expr with `it` bound to the value of test (in the grand tradition of anaphoric
macros). If no test evaluates to logical true, cond-let evalutes to nil.
To provide a default value, make the last test be a literal (such as
:else) that evaluates to logical true.

(acond) and (acond single-expr) evalute to nil."
  [& clauses]
  (when-let [[test expr & more] clauses]
      (if (= test :else)
        expr
        `(if-let [~'it ~test]
           ~expr
           (acond ~@more)))))
