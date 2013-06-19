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
         {1 1, 2 2, 3 3, 4 4}))
  (is (= (extract-map
          (pair-off [1 2 3 4 5 6 7 8 1 3 1 5 1 7 3 2 3 4 2 8])
          :key-extractor first
          :value-extractor second
          :fold-values true
          :fold-knil []
          :fold-kons #(conj %2 %1))
         {1 [2 3 5 7], 3 [4 2 4], 5 [6], 7 [8], 2 [8]})))



(deftest filter-map-tests
  (let [test-map {:a 1 :b 2 :c 3}]
    (is (= (filter-map #(odd? (val %)) test-map) {:a 1 :c 3}))
    (is (= (filter-map #(= :b (key %)) test-map) {:b 2}))
    (is (= (filter-map #(or (= :b (key %)) (odd? (val %))) test-map)
           {:a 1 :b 2 :c 3}))))

(deftest map->collection-test
  (let [test-map {:a 1 :b 2 :c 3}]
    (is (= (map->collection test-map [:a :b :c]) [1 2 3]))
    (is (= (map->collection test-map [:a :b :c]
                            :project-kv (fn [key _] key))
                            [:a :b :c]))
    (is (= (map->collection test-map [:a :b :c]
                            :project-kv #(str % %2))
           [":a1" ":b2" ":c3"]))))

(deftest mask-map-test
  (let [test-map {:a 1 :b 2 :c 3
                  :d {:a 9 :b 10}
                  :e {:a {:a 5 :b 6}}}]
    (testing "Subset of keys"
      (is (= (mask-map test-map {:a true :b true :c true})
             {:b 2, :c 3, :a 1})))
    (testing "Masking missing values"
      (is (= (mask-map test-map {:a true :b true :c true :z true :q 1})
             {:b 2, :c 3, :a 1})))
    (testing "Transformation function"
      (is (= (mask-map test-map {:a (fn [x] (+ x x)) :b true })
             {:b 2, :a 2})))
    (testing "nil as a mask value"
      (is (= (mask-map test-map {:a nil}) nil)))
    (testing "false as mask value"
      (is (= (mask-map test-map {:a false}) nil)))
    (testing "nil as a mask value and another"
      (is (= (mask-map test-map {:a nil :c 1}) {:c 3})))
    (testing "false as mask value and another"
      (is (= (mask-map test-map {:a false :c 1}) {:c 3})))
    (testing "macro-fn #() transform"
      (is (= (mask-map test-map {:a #(+ % %) :b 5 :d 7})
             {:d {:a 9, :b 10}, :b 2, :a 2})))
    (testing "selection of a nested value"
      (is (= (mask-map test-map {:d {:a 7}})
             {:d {:a 9}})))
    (testing "selection of a double nested value"
      (is (= (mask-map test-map {:e {:a {:a 7}}})
             {:e {:a {:a 5}}})))
    (testing "selection of a single nested map"
      (is (= (mask-map test-map {:e {:a 7}})
             {:e {:a {:a 5, :b 6}}})))))