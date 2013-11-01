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

(defmacro cond-let
  "Takes a binding-form and a set of test/expr pairs. Evaluates each test
  one at a time. If a test returns logical true, cond-let evaluates and
  returns expr with binding-form bound to the value of test and doesn't
  evaluate any of the other tests or exprs. To provide a default value
  either provide a literal that evaluates to logical true and is
  binding-compatible with binding-form, or use :else as the test and don't
  refer to any parts of binding-form in the expr. (cond-let binding-form)
  returns nil."
  [bindings & clauses]
  (let [binding (first bindings)]
    (when-let [[test expr & more] clauses]
      (if (= test :else)
        expr
        `(if-let [~binding ~test]
           ~expr
           (cond-let ~bindings ~@more))))))
