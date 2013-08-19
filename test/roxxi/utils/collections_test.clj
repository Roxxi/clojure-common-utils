(ns roxxi.utils.collections_test
  (:use clojure.test
        roxxi.utils.collections))


(deftest pair-off-test []
  (is (= (pair-off [1 2 3 4 5 6]) (list (list 1 2)
                                        (list 3 4)
                                        (list 5 6))))
  (testing "Trying to pair off an odd number of items"
    (is (= (pair-off [1 2 3 4 5 ]) (list (list 1 2)
                                         (list 3 4)
                                         (list 5))))))

(deftest seq->java-list-test []
  (testing "Conversion of different clojure seqs into a java.util.ArrayList:"
    (testing "Vector"
      (is (= (into [] (seq->java-list [1 2 3])) [1 2 3])))
    (testing "List"
      (is (= (into [] (seq->java-list '(1 2 3))) [1 2 3])))
    (testing "Lazy sequence"
      (is (= (into [] (seq->java-list (range 1 4))) [1 2 3])))))

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


(deftest dissoc-in-test
  (let [test-map {:q 10, :a 1, :b 2, :c {:d 4, :e 5}, :f {:g 6}, :h {:i {:j 7}}}]
    (testing "Remove top level element (should work like a normal dissoc)"
      (is (= (dissoc-in test-map [:q])
             {:a 1, :b 2, :c {:d 4, :e 5}, :f {:g 6}, :h {:i {:j 7}}})))
    (testing "Remove nested element, that has other same-level elements"
      (is (= (dissoc-in test-map [:c :d])
             {:q 10, :a 1, :b 2, :c {:e 5}, :f {:g 6}, :h {:i {:j 7}}}))
      (testing "Remove nested element that has no other same-level elemenets.
Expect empty map to not be there."
        (is (= (dissoc-in test-map [:f :g])
             {:q 10, :a 1, :b 2, :c {:d 4, :e 5}, :h {:i {:j 7}}})))
      (testing "More nested elements that would all return empty maps.
Expect to be dropped at the top level"
        (is (= (dissoc-in test-map [:h :i :j])
               {:q 10, :a 1, :b 2, :c {:d 4, :e 5}, :f {:g 6}}))))))
  
(deftest reassoc-in-test
  (let [test-map {:q 10, :a 1, :b 2, :c {:d 4, :e 5}}]
    (testing "Different old->new path transformations that can happen:"
      (testing "Old path is on the top level:"
        (testing "New path is nil, should delete entry"
          (is (= (reassoc-in test-map :q nil)
                 {:a 1, :b 2, :c {:d 4, :e 5}})))
         (testing "New path is top level, should move value to top level"
          (is (= (reassoc-in test-map :q :q2)
                 {:q2 10, :a 1, :b 2, :c {:d 4, :e 5}})))
          (testing "New path is a vector, should move value to nested key"
          (is (= (reassoc-in test-map :q [:q2 :q3])
                 {:a 1, :b 2, :c {:d 4, :e 5}, :q2 {:q3 10}}))))
      (testing "Old path is nested:"
        (testing "New path is nil, should delete entry"
          (is (= (reassoc-in test-map [:c :d] nil)
                 {:q 10, :a 1, :b 2, :c {:e 5}})))
         (testing "New path is top level, should move value to top level"
          (is (= (reassoc-in test-map [:c :d] :d2)
                 {:q 10, :a 1, :b 2, :c {:e 5}, :d2 4})))
          (testing "New path is a vector, should move value to nested key"
          (is (= (reassoc-in test-map [:c :d] [:c2 :d2])
                 {:a 1, :b 2, :c {:e 5}, :q 10, :c2 {:d2 4}})))))))

(deftest reassoc-many-test
  (let [test-map {:q 10, :a 1, :b 2, :c {:d 4, :e 5, :f 6}}
        test-transforms {:q nil, :a :a2, :b [:b2 :b3], [:c :d] nil, [:c :e] :e2, [:c :f] [:c2 :f2]}]
    (testing "Tests all the types of reassocs possible at once"
      (is (= (reassoc-many test-map test-transforms)
             {:a2 1, :b2 {:b3 2}, :e2 5, :c2 {:f2 6}})))))


(deftest walk-update-scalars-test
  (let [test-map {:a 5,
                  :b "words",
                  :c [:hi :hello "how are you" {:d 6, :e ["something"]}],
                  :f
                  {:f1 "hello",
                   :f2 {:f21 12, 
                        :f22 [1 2 3 4 5], ;; vector
                        :f23 #{:a :c :b}}, ;; set / inner map
                   :f3 (range 10) ;; returns a sequence
                   }}]
    (testing "All leaf node values should be returned as strings,
and each data structure (set, seq, vector, inner map, etc should be
preserved")
    (is (= (walk-update-scalars test-map str)
           {:a "5",
            :b "words",
            :c [":hi" ":hello" "how are you" {:d "6", :e ["something"]}],
            :f
            {:f1 "hello",
             :f2 {:f21 "12", :f22 ["1" "2" "3" "4" "5"], :f23 #{":a" ":b" ":c"}},
             :f3 '("0" "1" "2" "3" "4" "5" "6" "7" "8" "9")}}))))
      