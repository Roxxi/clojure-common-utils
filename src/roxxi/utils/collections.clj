(ns roxxi.utils.collections
  (:use roxxi.utils.print))


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

(comment (defn pair-off  [some-seq]
  (loop [pairs []
         a-seq some-seq]
    (if (or (empty? a-seq) (< (count a-seq) 2))
      pairs
      (recur (conj pairs (take 2 a-seq))
             (drop 2 a-seq))))))

(defn pair-off [some-seq]
  (partition-all 2 some-seq))

(defn seq->java-list ^java.util.List [coll]
  (java.util.ArrayList. coll))

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

(defn filter-map
  "Like `filter` but kv-pred is assumed to operate on a keyval,
and yields a map"
  [kv-pred some-map]
  (extract-map (filter kv-pred some-map)
               :key-extractor key
               :value-extractor val))

(declare mask-map)

(defn- mask-map-triage-kv [kv a-mask]
  (let [[k v] [(key kv) (val kv)]]
    (if-let [mask-v (get a-mask k)]
      (cond (fn? mask-v) {k (mask-v v)}
            (and (map? mask-v) (map? v))
            {k (mask-map v mask-v)}
            :else {k v}))))

(defn mask-map
  "Given a mask-map whose structure is some subset of some-map's
structure, extract the structure specified. For a path to be extracted
the terminal value in the mask-map must be a non-false yielding value.

If a function is provided as a terminal value in the mask, the function
will be applied to the value in the source location, before being
carried over to the resulting map.

If the mask yeilds no values, nil will be returned."
  [some-map map-mask]
  (apply merge (remove nil? (map #(mask-map-triage-kv % map-mask) some-map))))

;; by default just returns the value
(defn- default-map->collection-combiner [_ v]
  v)

(defn map->collection
  "Takes a map, and a vector of keys and applies project-kv to the
key and corresponding value. By default returns values."
  [some-map key-order
   & {:keys [project-kv]
      :or {project-kv default-map->collection-combiner}}]
  (map #(project-kv % (get some-map %)) key-order))


(defn dissoc-in
  "Removes the entry in the map at the path that is given."
  [map path]
  (cond (empty? path) map
        (= (count path) 1) (dissoc map (first path))

        :else
        (let [prop (first path)
              value (get map prop)
              new-value  (dissoc-in value (rest path))]
          (if (empty? new-value)
            (dissoc map prop)
          (assoc map prop new-value)))))

(defn- have-something-to-move? [json-map path]
  (get-in json-map path))

(defn- relocate-value [json-map old-path new-path]
  (if-let [value (have-something-to-move? json-map old-path)]
    ;; remove the old value, and insert the new value
    (assoc-in (dissoc-in json-map old-path) new-path value)
    json-map))

(defn- vectorify [thing]
  (if (vector? thing)
    thing
    (vector thing)))

(defn- nil-path?
  "Because we vectorify kind of blindly, if we had a nil,
we would've made [nil]- this tests for that"
  [path]
  (= [nil] path))

(defn reassoc-in
  "Takes a map and relocates the value at the old path to
the new path.

If the old path is a vector it reads from that path;
If the old path is a string it treats it as a top-level path;
If the new path is a vector it writes to that path;
If the new path is a string it treats it as a top-level path;
If the new path is nil, it removes the key-value pair."

  [map old-path new-path]
  (let [old-path (vectorify old-path)
        new-path (vectorify new-path)]
    (if (nil-path? new-path)
      ;; just remove the value
      (dissoc-in map old-path)
      (relocate-value map old-path new-path))))

(defn reassoc-many
  "Takes a set of field mappings and relocates the
fields specified by the key to the location
specified by the value.

If the key is a vector it reads from that path;
If the key is a string it treats it as a top-level path;
If the value is a vector it writes to that path;
If the value is a string it treats it as a top-level path;
If the value is nil, it removes the key-value pair."
  [map mappings]
  (let [mapping (first mappings)]
    (if (empty? mappings)
      map
      (recur (reassoc-in map (key mapping) (val mapping)) (rest mappings)))))


(declare walk-update-scalars)

(defn- walk-apply
  "Applies f to a value if it is not any kind of collection,
otherwise applies f to each element in the collection;
in the case of maps, only the value is supplied as the operand
to f"
  [maybe-scalar-value f]
  (cond (map? maybe-scalar-value)
        (walk-update-scalars maybe-scalar-value f)
        (vector? maybe-scalar-value)
        (vec (map #(walk-apply % f) maybe-scalar-value))
        (seq? maybe-scalar-value)
        (map #(walk-apply % f) maybe-scalar-value)
        (set? maybe-scalar-value)
        (into #{} (map #(walk-apply % f) maybe-scalar-value))
        :else
        (f maybe-scalar-value)))

(defn walk-update-scalars
  "If the map is a tree, this would apply f to each leaf node-
if an element happens to be a seq or a vector, this would also apply
it to each element- i.e. collections are not scalars."
  [some-map f]
  (project-map some-map :value-xform #(walk-apply % f)))


(defn- scalar? [x]
  (not (coll? x)))

(defn- empty-coll? [c]
  (and (coll? c)
       (empty? c)))

(defn prune-map-scalars
  "Removes property paths for each value that prune? returns
 true for.

The default prune-sigil used to identify what values to remove
is 'nil', you can supply a different prune sigil in the event
you want to prevent the removal of nil values.

Scalar values inside of sets, vectors, sequences will not be pruned.

For example, 2 in the vector below won't be pruned, but the 2
nested in the map will be pruned.

`(prune-map-scalars {:a [1 2 3 {:a 1 :b 2}]} even?)` => `{:a [1 2 3 {:a 1}]}`

If, however, you want your other collections (vectors, sets, seqs) to also
have their elements pruned, simply specify :prune-non-map-scalars to true
and then 

`(prune-map-scalars {:a [1 2 3 {:a 1 :b 2}]} even? :prune-non-map-scalars true)`
 => `{:a [1 3 {:a 1}]}` <-- note the lack of the 2 in the array.
"
  [some-map prune? & {:keys [prune-sigil
                             prune-non-map-scalars]
                      :or {prune-sigil nil
                           prune-non-map-scalars false}}]
  (let [pruned? #(or (= (second %) prune-sigil) (empty-coll? (second %)))
        ;; if we have a scalar, there's no need to recurse
        ;; otherwise this captures the passing of our
        ;; keyword arguments (hence we're not using recur)
        recurse (fn recurse [v]
                  (prune-map-scalars v prune?
                                     :prune-sigil prune-sigil
                                     :prune-non-map-scalars
                                     prune-non-map-scalars))
        recurse-non-scalars #(if (scalar? %) % (recurse %))
        ;; when working through a seq of items,
        ;; we may not want to consider non scalars
        ;; unless we should prune-non-map-scalars
        ;; we also want to not leave empty collections
        ;; hanging around, which is why we remove them
        prune-elems (if prune-non-map-scalars
                      (fn [coll]
                        (remove empty-coll?
                                (map recurse-non-scalars
                                     (remove #(and (scalar? %) (prune? %)) coll))))
                      (fn [coll]
                        (remove empty-coll?
                                (map recurse-non-scalars coll))))
        by-type (fn by-type [v]
                  (cond
                   (map? v) (recurse v)
                   (coll? v) (let [pruned-seq (prune-elems v)]
                               (cond
                                (vector? v) (vec pruned-seq)
                                (seq?    v) pruned-seq
                                (set?    v) (set pruned-seq)))
                   :else (if (prune? v) prune-sigil v)))
        recursive-prune (fn recursive-prune [[k v]] [k (by-type v)])
        pruned-kvs (remove pruned? (map recursive-prune some-map))]
    (extract-map pruned-kvs :key-extractor first :value-extractor second)))
