(ns roxxi.utils.common-test
  (:use clojure.test
        roxxi.utils.common))

(deftest acond-test []
  (is (nil? (acond)))
  (is (nil? (acond 10)))
  (is (nil? (acond
             false "foo"
             "test that's missing its xpr")))
  (is (= 16 (acond
             (+ 5 5) (+ it 6))))
  (is (= 16 (acond
             (+ 5 5) (+ it 6)
             nil (+ it 150))))
  (is (= 16 (acond
             nil (+ it 150)
             (+ 5 5) (+ it 6))))
  (is (= 16 (acond
             false (+ it 150)
             (+ 5 5) (+ it 6))))
  (is (acond
       true it))
  (is (= 25 (acond
             nil (+ it 150)
             :else 25)))
  (is (= 25 (acond
             nil (+ it 150)
             :else 25
             (+ 5 5) (+ it 6)))))
