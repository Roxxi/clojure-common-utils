# 0.0.22

Fixed bug in `reassoc-in` and `reassoc-many`

Before:
`(reassoc-many {:q false} {:q :a})` => `{:q false}`
`(reassoc-many {:q nil} {:q :a})` => `{:q nil}`
After:
`(reassoc-many {:q false} {:q :a})` => `{:a false}`
`(reassoc-many {:q nil} {:q :a})` => `{:a nil}`

# 0.0.21

Added `print-expr-rec` and `print-expr-hella-rec`

`print-expr-rec` recursively calls `print-expr` on interesting
subforms, e.g

user> (macroexpand-1 '(print-expr-rec (+ (* x x) (* y y))))
(print-expr (+ (print-expr (* x x))
               (print-expr (* y y))))

`print-expr-hella-rec` recursively calls `print-expr` on all
subforms, even if they may not be that interesting, e.g

user> (macroexpand-1 '(print-expr-hella-rec (+ (* x x) (* y y))))
(print-expr ((print-expr +)
             (print-expr ((print-expr *) (print-expr x) (print-expr x)))
             (print-expr ((print-expr *) (print-expr y) (print-expr y)))))

# 0.0.20

Added `with-consistent-now`, which captures the current time and
allows you to use it by calling `consistent-now`.

# 0.0.19

Added `collify` because I'm tired of writing the same
if this isn't a collection, make it a collection code
over and over.

# 0.0.18

Fixed bug in `dissoc-in`

Before:
`(dissoc-in {:a 5} [:b :c])` =>  `{:b nil, :a 5}`
After:
`(dissoc-in {:a 5} [:b :c])` =>  `{:a 5}`

# 0.0.17

Fixed bug in `dissoc-in` that caused a crash if the path specified was
deeper than the map went

# 0.0.16

Added `deep-merge` and `deep-merge-with` from [clojure-contrib/map_utils](https://github.com/richhickey/clojure-contrib/blob/2ede388a9267d175bfaa7781ee9d57532eb4f20f/src/main/clojure/clojure/contrib/map_utils.clj)

# 0.0.15

Added `prune-map-scalars`

`prune-map-scalars` Takes a map and a predicate to apply to the scalar
values of a map (recurising down any nested collection of collections)
and removes the entire property paths to any values that meet the
condition specified by the predicate.

There are two optional arguments that can be specified:

1. `:prune-sigil` will allow you to provide an alternative value to
   use to mark objects to prune (default is nil- so if you want to
   persist nil's, you should specify something that will definitely
   not occur in your map)
2. `:prune-non-map-scalars` if set to true (or any non-false value)
   this will also prune corresponding values that occur in linear
   sequences (e.g. scalar-values in vectors, sets, etc.) Even if this
   parameter is not specified, maps inside of linear sequences will
   still have their leaves pruned, but the scalar elements of
   sequences will not be evaluated to be pruned unless this is specified.

# 0.0.14

Added `def-` and `acond`.

The `def-` macro is to `def` as `defn-` is to `defn`.
The `acond` macro is the standard anaphoric-cond from old Lispy
traditions (with `it` as the capturing symbol).



# 0.0.13

Added `walk-update-scalars`.  This function allows you to apply
a function `f` to every non-collection element in a map `m`.
If `m` contains, sets, vectors, seqs, or inner maps, `f` is applied
to their inner values, unless their inner values happen to be
sets, vectors, seqs, or inner maps- in which case, this is recursively
applied until non-seqable values are reached.


# 0.0.12



Added `dissoc-in`. The performs `dissoc` on a path (represented as
a vecter). This allows to `dissoc` nested keys, and is a big part of
`reassoc-in`.


Added `reassoc-in` and `reassoc-many`. In reality,
`reassoc-many` ends up calling `reassoc-in` for each mapping,
which is why the documentation is so similar. Only difference
is that `reassoc-in` takes one mapping (old path and new path), but `reassoc-many` takes a map of mappings.

`reassoc-in` Takes a map and relocates the value at the old path to
the new path.

If the old path is a vector it reads from that path;
If the old path is a string it treats it as a top-level path;
If the new path is a vector it writes to that path;
If the new path is a string it treats it as a top-level path;
If the new path is nil, it removes the key-value pair.


`reassoc-many`

Takes a set of field mappings and relocates the
fields specified by the key to the location
specified by the value.

If the key is a vector it reads from that path;
If the key is a string it treats it as a top-level path;
If the value is a vector it writes to that path;
If the value is a string it treats it as a top-level path;
If the value is nil, it removes the key-value pair.

# 0.0.11

This binary came out no different than 0.0.10, had to redeploy.

# 0.0.10

Added test for `pair-off` which is now just a wrapper around
`(partition-all 2 seq)` (I didn't know that existed!)


# 0.0.9
`seq->java-list`

Takes a seq and returns a java.util.List

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
