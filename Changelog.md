# 0.0.8
`mask-map`

Given a mask-map whose structure is some subset of some-map's structure, extract the structure specified. For a path to be extracted the terminal value in the mask-map must be a non-false yielding value. 
If a function is provided as a terminal value in the mask, the function will be applied to the value in the source location, before being carried over to the resulting map.

If the mask yeilds no values, nil will be returned.

# 0.0.7

`map->collecton` helps convert a map into a collection of values,
can specify a function to merge the key-value pairs into singular values

`filter-map`  Like `filter` but takes a kv-pred that is assumed to operate on a keyval, and yields a map

# 0.0.6

Enabled Extract map to fold values that occur for the same key.

# 0.0.5

`multi-line-str` is a macro that allows you to insert multi-line-strings that are pre-compiled to a single string.

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