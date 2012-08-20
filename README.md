clojure-common-utils
==========

Common generic utilities for Clojure

Get it
------

Leiningen:
```clojure
[roxxi/clojure-common-utils "0.0.1"]
```

In your code:
```clojure
(ns your.namespace
    (:use roxxi.utils.print)
    ...)
```    
       
Currently only contains one function that's so common that I can't
help but want it everywhere.

1. `print-expr` prints the expression it contains, and returns the
   result of the expression, so that messages can be seen in place. The
   following expression:
    
    ```clojure
    (defn foo [x y]
        (print-expr (+ (print-expr x) (print-expr y))))
        
    (foo 5 6)
    ```
    
    yields
    
    ```log
    Expression x evaluates to 5 of type class java.lang.Long
    +
    +
    Expression y evaluates to 6 of type class java.lang.Long
    +
    +
    Expression (+ (print-expr x) (print-expr y)) evaluates to 11 of type class java.lang.Long
    +
    +    
    ```
    
    and returns `11`
