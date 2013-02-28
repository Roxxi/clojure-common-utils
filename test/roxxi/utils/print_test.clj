(ns roxxi.utils.print_test
  (:use clojure.test
        roxxi.utils.print))


(deftest multi-line-str-macro []
  (is (= (multi-line-str "hello"
                         "how are you?"
                         "i am fine")
         "hello how are you? i am fine")))

