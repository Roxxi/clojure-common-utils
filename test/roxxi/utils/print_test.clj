(ns roxxi.utils.print_test
  (:use clojure.test
        roxxi.utils.print))


(deftest multi-line-str-macro []
  (is (= (multi-line-str "hello"
                         "how are you?"
                         "i am fine")
         "hello how are you? i am fine")))

(deftest print-expr-rec-test []
  (testing "Does the correct string get written to std-out"
    (let [pythag (fn [x y]
                   (print-expr-rec (+ (* x x) (* y y))))
          what-got-written-to-output (with-out-str (pythag 3 4))]
      (is (= what-got-written-to-output
             "Expression (* x x) evaluates to 9 of type class java.lang.Long\n+\n+\nExpression (* y y) evaluates to 16 of type class java.lang.Long\n+\n+\nExpression (+ (print-expr (* x x)) (print-expr (* y y))) evaluates to 25 of type class java.lang.Long\n+\n+\n")))))
