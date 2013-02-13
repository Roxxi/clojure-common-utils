# 0.0.4 

`extract-map` now takes an keyword arg `:initial` where a base map can be specified to be added to.

# 0.0.3 

Added `cross`, `every`

`cross` recursively produces the cross product of any number of sequences of different lengths.

`(every pred? coll)` returns true iff `pred?` is true for each element in `coll`

# 0.0.2 

Added `extract-map`, `project-map`, `set-over` and `only-n-times`.

`extract-map` Allows you to process a collection element by element, and transform each element into a key-value pair.

`project-map` Takes a map and allows you to transform the keys and values

`set-over` allows you to merge a collection of collections into a single set of elements

`only-n-times` returns a function that returns true n-times, then false forever after. Useful for printing debugging messages a few times.