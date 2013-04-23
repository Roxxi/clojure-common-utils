(ns roxxi.utils.collections_test
  (:use clojure.test
        roxxi.utils.collections))


(deftest extract-map-with-fold []
  (is (= (extract-map [1 2 2 3 1 2 3 4 2 1]
                      :fold-values true
                      :fold-knil []
                      :fold-kons #(conj %2 %1))
         {1 [1 1 1], 2 [2 2 2 2], 3 [3 3], 4 [4]}))
  (is (= (extract-map [1 2 2 3 1 2 3 4 2 1] :fold-values true)
         {1 (list 1 1 1), 2 (list 2 2 2 2), 3 (list 3 3), 4 (list 4)}))
  (is (= (extract-map [1 2 2 3 1 2 3 4 2 1])
         {1 1, 2 2, 3 3, 4 4})))
         