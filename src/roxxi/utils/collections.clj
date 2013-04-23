(ns roxxi.utils.collections)


;; # Collections

(defn every [pred coll]
  (loop [coll (seq coll)]    
    (if (empty? coll)
      true
      (if (pred (first coll))
        (recur (next coll))
        false))))



(defn cross [& seqs]
  (when seqs
    (if-let [s (first seqs)]
      (if-let [ss (next seqs)]
        (for [x  s
              ys (apply cross ss)]
          (cons x ys))
        (map list s)))))

(defn pair-off  [some-seq]
  (loop [pairs []
         a-seq some-seq]
    (if (or (empty? a-seq) (< (count a-seq) 2))
      pairs
      (recur (conj pairs (take 2 a-seq))
             (drop 2 a-seq)))))

;; # Sets

(defn set-over [& xs]
  (persistent!
   (reduce #(conj! %1 %2) (transient #{}) xs)))


;; # Maps

(defn extract-map [some-seq
                   & {:keys [xform
                             key-extractor
                             value-extractor
                             fold-values
                             fold-kons
                             fold-knil
                             initial]
                      :or {xform identity
                           key-extractor identity
                           value-extractor identity
                           fold-values false
                           fold-kons cons
                           fold-knil nil
                           initial {}}}]
  (let [xform-assoc!
        (if fold-values
          (fn folding-xform-assoc! [some-map elem]
            (let [xformed (xform elem)
                  the-key (key-extractor xformed)
                  the-value (value-extractor xformed)
                  whats-there (get some-map the-key)]
              (if whats-there
                (assoc! some-map the-key (fold-kons the-value whats-there))
                (assoc! some-map the-key (fold-kons the-value fold-knil)))))
          (fn xform-assoc! [some-map elem]
            (let [xformed (xform elem)]
              (assoc! some-map (key-extractor xformed) (value-extractor xformed)))))]
    (persistent!
     (loop [elems some-seq
            new-map (transient initial)]
       (if (empty? elems)
         new-map
         (recur (rest elems)
                (xform-assoc! new-map (first elems))))))))
  
                   
(defn project-map [some-map
                   & {:keys [key-xform
                             value-xform]
                      :or {key-xform identity,
                           value-xform identity}}]
  (let [xform-assoc!
        (fn xform-assoc! [some-map kv]
          (assoc! some-map (key-xform (key kv)) (value-xform (val kv))))]
    (persistent!
     (loop [kvs (seq some-map)
            new-map (transient {})]
       (if (empty? kvs)
         new-map
         (recur (rest kvs)
                (xform-assoc! new-map (first kvs))))))))