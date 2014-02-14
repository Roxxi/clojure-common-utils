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


(deftest collify-test []
  (testing "Making sure I can collectionify things!"
    (testing "String"
      (is (coll? (collify "hello"))))
    (testing "Number"
      (is (coll? (collify 6))))
    (testing "Set"
      (is (coll? (collify #{1 2 3})))
      (is (= (collify #{1 2 3}) #{1 2 3})))
    (testing "Vector"
      (is (coll? (collify [1 2 3])))
      (is (= (collify [1 2 3]) [1 2 3])))
    (testing "Map"
      (is (coll? (collify {1 2 3 4})))
      (is (= (collify {1 2 3 4}) {1 2 3 4})))
    (testing "List"
      (is (coll? (collify '(1 2 3))))
      (is (= (collify '(1 2 3)) '(1 2 3))))))

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
  (testing "dissoc-in"
    (let [test-map
          {:q 10, :a 1, :b 2, :c {:d 4, :e 5}, :f {:g 6}, :h {:i {:j 7}}}]
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
                 {:q 10, :a 1, :b 2, :c {:d 4, :e 5}, :f {:g 6}})))
        (testing "Providing a path that goes deeper than
the actual available map"
          (is (= (dissoc-in test-map [:c :d :e]) test-map)))
        (testing "Providing a path that goes deeper than the root keys,
and uses a root-key that isn't part of the map"
          (is (= (dissoc-in {:a 5} [:b :c]) {:a 5})))
        (testing "That an empty map does not yield nil for the
key, but is removed"
          (is (= (dissoc-in {:a {}} [:a]) {})))))))

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
                 {:a 1, :b 2, :c {:e 5}, :q 10, :c2 {:d2 4}})))))
    (testing "Works for falsy values:"
      (is (= (reassoc-in {:q 10} :q :a)
             {:a 10}))
      (is (= (reassoc-in {:q false} :q :a)
             {:a false}))
      (is (= (reassoc-in {:q nil} :q :a)
             {:a nil})))))

(deftest reassoc-many-test
  (let [test-map {:q 10, :a 1, :b 2, :c {:d 4, :e 5, :f 6}}
        test-transforms {:q nil, :a :a2, :b [:b2 :b3], [:c :d] nil, [:c :e] :e2, [:c :f] [:c2 :f2]}]
    (testing "Tests all the types of reassocs possible at once"
      (is (= (reassoc-many test-map test-transforms)
             {:a2 1, :b2 {:b3 2}, :e2 5, :c2 {:f2 6}})))
    (testing "Works for falsy values:"
      (is (= (reassoc-many {:q 10} {:q :a})
             {:a 10}))
      (is (= (reassoc-many {:q false} {:q :a})
             {:a false}))
      (is (= (reassoc-many {:q nil} {:q :a})
             {:a nil})))))


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

(deftest prune-map-scalars-test
  (testing "Testing prune-map-scalars"
    (let [test-map
          {;; root level numbers
           :a 1
           :b 2
           ;; vector and map in a vector
           :c [3 4 5 6 {:c1 1 :c2 2 :c3 {:c31 1 :c32 2 :c33 3 :c34 4}} 7 8 9]
           ;; set and map in a set, and vector in a map in a set
           :d #{1 2 3 4 5 {:c1 1 :c2 2
                           :c3 {:c31 1 :c32 2 :c33 3 :c34 4 :c35 [1 2 3 4]}}}
           ;; a seq
           :e (range 1 10)
           ;; a map in a map with a nested vector, set, and seq
           :f {:f1 1
               :f2 2,
               :f3 {:f31 12, :f32 [1 2 3 4 5 {:f321 2}], :f33 #{1 2 3 4 5 6}},
               :f4 '(0 1 2 3 4 5 6 7 8 9)}
           ;; a place where nothing should change
           :g [1 3]
           ;; a vector that should be removed when prune-non-map-scalars
           :h [2 4]}]
      (testing "to ensure we correctly leave values that contain values
that should be filtered, but aren't because they are key-value pairs"
        (is (= (prune-map-scalars test-map even?)
               {:a 1,
                :c [3 4 5 6 {:c1 1, :c3 {:c31 1, :c33 3}} 7 8 9],
                :d #{1 2 3 4 5 {:c1 1, :c3 {:c31 1, :c33 3, :c35 [1 2 3 4]}}},
                :e '(1 2 3 4 5 6 7 8 9),
                :f
                {:f1 1,
                 :f3 {:f33 #{1 2 3 4 5 6}, :f32 [1 2 3 4 5]},
                 :f4 '(0 1 2 3 4 5 6 7 8 9)},
                :g [1 3]
                :h [2 4]})))
      (testing "to ensure we can prune non-map values"
        (is (= (prune-map-scalars test-map even? :prune-non-map-scalars true)
               {:a 1,
                :c [3 5 {:c1 1, :c3 {:c31 1, :c33 3}} 7 9],
                :d #{1 3 5 {:c1 1, :c3 {:c31 1, :c33 3, :c35 [1 3]}}},
                :e '(1 3 5 7 9),
                :f {:f1 1, :f3 {:f33 #{1 3 5}, :f32 [1 3 5 ]}, :f4 '(1 3 5 7 9)},
                :g [1 3]})))
      (testing "to ensure we can specify a different prune-sigil to persist nulls"
        (let [test-map
              {:a nil
               :b 2
               :c [nil 4 nil 6 {:c1 1, :c3 {:c31 1, :c32 2 :c33 3 :c34 nil}} 7 8 9 nil],
               :d #{1 nil 3 4 5
                    {:c1 1 :c2 2
                     :c3 {:c31 1 :c32 2 :c33 nil :c34 4 :c35 [1 2 3 4 nil]}}}}]
          (testing "and that if we don't specify a prune-sigil, that our nils would
be gone. (All of the nils shouldn't be gone here, just the ones that are leaves of nested
maps- depite the fact we said prune non-map-scalars, since the values in the non-maps
don't satisify the prune condition"
            (is (= (prune-map-scalars test-map #(and (number? %) (even? %))
                                      :prune-non-map-scalars true)
                   {:c [nil nil {:c1 1, :c3 {:c31 1, :c33 3}} 7 9 nil],
                    :d #{nil 1 3 5 {:c1 1, :c3 {:c31 1, :c35 [1 3 nil]}}}})))
          (testing "by specifying a different prune-sigil"
            (is (= (prune-map-scalars test-map #(and (number? %) (even? %))
                                      :prune-sigil :prune_dis)
                   {:a nil,
                    :c [nil 4 nil 6 {:c1 1, :c3 {:c31 1, :c33 3, :c34 nil}} 7 8 9 nil],
                    :d #{nil 1 3 4 5 {:c1 1, :c3 {:c31 1, :c33 nil, :c35 [1 2 3 4 nil]}}}}))))))))
