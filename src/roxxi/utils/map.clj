(ns roxxi.utils.map)

(defn extract-map [some-seq
                   & {:keys [xform
                             key-extractor
                             value-extractor]
                      :or {xform identity
                           key-extractor identity,
                           value-extractor identity}}]
  (let [xform-assoc!
        (fn xform-assoc! [some-map elem]
          (let [xformed (xform elem)]
            (assoc! some-map (key-extractor xformed) (value-extractor xformed))))]
    (persistent!
     (loop [elems some-seq
            new-map (transient {})]
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