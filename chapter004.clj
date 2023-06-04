(ns chapter004)

(comment
  
;;   
;;     
;;       Clojure from the ground up: sequences     window.dataLayer = window.dataLayer || \[\]; function gtag(){dataLayer.push(arguments);} gtag('js', new Date()); gtag('config', 'G-MXDP37S6QL');
;;   
;;       
;;   
;;   *   [Aphyr](/)
;;   *   [About](/about)
;;   *   [Blog](/posts)
;;   *   [Photos](/photos)
;;   *   [Code](http://github.com/aphyr)
;;   
   "[Clojure from the ground up: sequences]";(/posts/304-clojure-from-the-ground-up-sequences)
;;  * ========================================================================================
;;   
;;   [Software](/tags/software) [Clojure](/tags/clojure) [Clojure from the ground up](/tags/clojure-from-the-ground-up)
;;   
;;   2013-11-18
;;   
"   In [Chapter 3](/posts/303-clojure-from-the-ground-up-functions), we discovered functions as a way to _abstract_ expressions; to rephrase a particular computation with some parts missing. We used functions to transform a single value. But what if we want to apply a function to _more than one_ value at once? What about sequences?
;;   
   For example, we know that `(inc 2)` increments the number 2. What if we wanted to increment _every number_ in the vector `[1 2 3]`, producing `[2 3 4]`?
";;   
;;       user=> 
;   
;   (inc [1 2 3])
;;       ClassCastException clojure.lang.PersistentVector cannot be cast to java.lang.Number  clojure.lang.Numbers.inc (Numbers.java:110)
;;       
;;   
"  Clearly `inc` can only work on numbers, not on vectors. We need a different kind of tool.
";;   
;;*   [A direct approach](#a-direct-approach)
;;   ---------------------------------------
;;   
"  Let’s think about the problem in concrete terms. We want to increment each of three elements: the first, second, and third. We know how to get an element from a sequence by using nth, so let’s start with the first number, at index 0:
";;   
;;       user=> 
(def numbers [1 2 3])
;;       #'user/numbers
;;       user=> 
(nth numbers 0)
;;       1
;;       user=> 
(inc (nth numbers 0))
;;       2
numbers
;;       
;;   
;;  * So there’s the first element incremented. Now we can do the second:
;;   
;;       user=> 
(inc (nth numbers 1))
;;       3
;;       user=> 
(inc (nth numbers 2))
;;       4
;;       
;;   
;;*   And it should be straightforward to combine these into a vector…
;;   
;;       user=> 
[(inc (nth numbers 0)) (inc (nth numbers 1)) (inc (nth numbers 2))]
;;       [2 3 4]
;;       
;;   
;;*   Success! We’ve incremented each of the numbers in the list! How about a list with only two elements?
;;   
;;       user=> 
(def numbers [1 2])
;;       #'user/numbers
;;       user=> 
[(inc (nth numbers 0)) (inc (nth numbers 1)) (inc (nth numbers 2))]
;;       
;;       IndexOutOfBoundsException   clojure.lang.PersistentVector.arrayFor (PersistentVector.java:107)
;;       
;;   
;;   Shoot. We tried to get the element at index 2, but _couldn’t_, because `numbers` only has indices 0 and 1. Clojure calls that “index out of bounds”.
;;   
;;   We could just leave off the third expression in the vector; taking only elements 0 and 1. But the problem actually gets much worse, because we’d need to make this change _every_ time we wanted to use a different sized vector. And what of a vector with 1000 elements? We’d need 1000 `(inc (nth numbers ...))` expressions! Down this path lies madness.
;;   
;;   Let’s back up a bit, and try a slightly smaller problem.
;;   
;;   [Recursion](#recursion)
;;   -----------------------
;;   
;;   What if we just incremented the _first_ number in the vector? How would that work? We know that `first` finds the first element in a sequence, and `rest` finds all the remaining ones.
;;   
;;       user=> (first [1 2 3])
;;       1
;;       user=> (rest [1 2 3])
;;       (2 3)
;;       
;;   
;;   So there’s the _pieces_ we’d need. To glue them back together, we can use a function called `cons`, which says “make a list beginning with the first argument, followed by all the elements in the second argument”.
;;   
;;       user=> (cons 1 [2])
;;       (1 2)
;;       user=> (cons 1 [2 3])
;;       (1 2 3)
;;       user=> (cons 1 [2 3 4])
;;       (1 2 3 4)
;;       
;;   
;;   OK so we can split up a sequence, increment the first part, and join them back together. Not so hard, right?
;;   
;;       (defn inc-first [nums]
;;         (cons (inc (first nums))
;;               (rest nums)))
;;       user=> (inc-first [1 2 3 4])
;;       (2 2 3 4)
;;       
;;   
;;   Hey, there we go! First element changed. Will it work with any length list?
;;   
;;       user=> (inc-first [5])
;;       (6)
;;       user=> (inc-first [])
;;       
;;       NullPointerException   clojure.lang.Numbers.ops (Numbers.java:942)
;;       
;;   
;;   Shoot. We can’t increment the first element of this empty vector, because it doesn’t _have_ a first element.
;;   
;;       user=> (first [])
;;       nil
;;       user=> (inc nil)
;;       
;;       NullPointerException   clojure.lang.Numbers.ops (Numbers.java:942)
;;       
;;   
;;   So there are really _two_ cases for this function. If there is a first element in `nums`, we’ll increment it as normal. If there’s _no_ such element, we’ll return an empty list. To express this kind of conditional behavior, we’ll use a Clojure special form called `if`:
;;   
;;       user=> (doc if)
;;       -------------------------
;;       if
;;         (if test then else?)
;;       Special Form
;;         Evaluates test. If not the singular values nil or false,
;;         evaluates and yields then, otherwise, evaluates and yields else. If
;;         else is not supplied it defaults to nil.
;;       
;;         Please see http://clojure.org/special_forms#if
;;       
;;   
;;   To confirm our intuition:
;;   
;;       user=> (if true :a :b)
;;       :a
;;       user=> (if false :a :b)
;;       :b
;;       
;;   
;;   Seems straightforward enough.
;;   
;;       (defn inc-first [nums]
;;         (if (first nums)
;;           ; If there's a first number, build a new list with cons
;;           (cons (inc (first nums))
;;                 (rest nums))
;;           ; If there's no first number, just return an empty list
;;           (list)))
;;       
;;       user=> (inc-first [])
;;       ()
;;       user=> (inc-first [1 2 3])
;;       (2 2 3)
;;       
;;   
;;   Success! Now we can handle _both_ cases: empty sequences, and sequences with things in them. Now how about incrementing that _second_ number? Let’s stare at that code for a bit.
;;   
;;       (rest nums)
;;       
;;   
;;   Hang on. That list–`(rest nums)`–that’s a list of numbers too. What if we… used our inc-first function on _that_ list, to increment _its_ first number? Then we’d have incremented both the first _and_ the second element.
;;   
;;       (defn inc-more [nums]
;;         (if (first nums)
;;           (cons (inc (first nums))
;;                 (inc-more (rest nums)))
;;           (list)))
;;       user=> (inc-more [1 2 3 4])
;;       (2 3 4 5)
;;       
;;   
;;   Odd. That didn’t just increment the first two numbers. It incremented _all_ the numbers. We fell into the _complete_ solution entirely by accident. What happened here?
;;   
;;   Well first we… yes, we got the number one, and incremented it. Then we stuck that onto `(inc-first [2 3 4])`, which got two, and incremented it. Then we stuck that two onto `(inc-first [3 4])`, which got three, and then we did the same for four. Only _that_ time around, at the very end of the list, `(rest [4])` would have been _empty_. So when we went to get the first number of the empty list, we took the _second_ branch of the `if`, and returned the empty list.
;;   
;;   Having reached the _bottom_ of the function calls, so to speak, we zip back up the chain. We can imagine this function turning into a long string of `cons` calls, like so:
;;   
;;       (cons 2 (cons 3 (cons 4 (cons 5 '()))))
;;       (cons 2 (cons 3 (cons 4 '(5))))
;;       (cons 2 (cons 3 '(4 5)))
;;       (cons 2 '(3 4 5))
;;       '(2 3 4 5)
;;       
;;   
;;   This technique is called _recursion_, and it is a fundamental principle in working with collections, sequences, trees, or graphs… any problem which has small parts linked together. There are two key elements in a recursive program:
;;   
;;   1.  Some part of the problem which has a known solution
;;   2.  A relationship which connects one part of the problem to the next
;;   
;;   Incrementing the elements of an empty list returns the empty list. This is our _base case_: the ground to build on. Our _inductive_ case, also called the _recurrence relation_, is how we broke the problem up into incrementing the _first_ number in the sequence, and incrementing all the numbers in the _rest_ of the sequence. The `if` expression bound these two cases together into a single function; a function _defined in terms of itself_.
;;   
;;   Once the initial step has been taken, _every_ step can be taken.
;;   
;;       user=> (inc-more [1 2 3 4 5 6 7 8 9 10 11 12])
;;       (2 3 4 5 6 7 8 9 10 11 12 13)
;;       
;;   
;;   This is the beauty of a recursive function; folding an unbounded stream of computation over and over, onto itself, until only a single step remains.
;;   
;;   [Generalizing from inc](#generalizing-from-inc)
;;   -----------------------------------------------
;;   
;;   We set out to increment every number in a vector, but nothing in our solution actually depended on `inc`. It just as well could have been `dec`, or `str`, or `keyword`. Let’s _parameterize_ our `inc-more` function to use _any_ transformation of its elements:
;;   
;;       (defn transform-all [f xs]
;;         (if (first xs)
;;           (cons (f (first xs))
;;                 (transform-all f (rest xs)))
;;           (list)))
;;       
;;   
;;   Because we could be talking about _any_ kind of sequence, not just numbers, we’ve named the sequence `xs`, and its first element `x`. We also take a function `f` as an argument, and that function will be applied to each `x` in turn. So not only can we increment numbers…
;;   
;;       user=> (transform-all inc [1 2 3 4])
;;       (2 3 4 5)
;;       
;;   
;;   …but we can turn strings to keywords…
;;   
;;       user=> (transform-all keyword ["bell" "hooks"])
;;       (:bell :hooks)
;;       
;;   
;;   …or wrap every element in a list:
;;   
;;       user=> (transform-all list [:codex :book :manuscript])
;;       ((:codex) (:book) (:manuscript))
;;       
;;   
;;   In short, this function expresses a sequence in which each element is some function applied to the corresponding element in the underlying sequence. This idea is so important that it has its own name, in mathematics, Clojure, and other languages. We call it `map`.
;;   
;;       user=> (map inc [1 2 3 4])
;;       (2 3 4 5)
;;       
;;   
;;   You might remember maps as a datatype in Clojure, too–they’re dictionaries that relate keys to values.
;;   
;;       {:year  1969
;;        :event "moon landing"}
;;       
;;   
;;   The _function_ `map` relates one sequence to another. The _type_ map relates keys to values. There is a deep symmetry between the two: maps are usually sparse, and the relationships between keys and values may be arbitrarily complex. The map function, on the other hand, usually expresses the _same_ type of relationship, applied to a series of elements in _fixed order_.
;;   
;;   [Building sequences](#building-sequences)
;;   -----------------------------------------
;;   
;;   Recursion can do more than just `map`. We can use it to expand a single value into a sequence of values, each related by some function. For instance:
;;   
;;       (defn expand [f x count]
;;         (when (pos? count)
;;           (cons x (expand f (f x) (dec count)))))
;;       
;;   
;;   Our base case is `nil`, returned when `count` is zero, and `(pos? count)` fails. Our inductive case returns a list of x, followed by the expansion starting with (f x), and a count _one smaller_. This means the first element of our list will be x, the second (f x), the third (f (f x)), and so on. Each time we call `expand`, we count down by one using `dec`. Once the count is zero, the `if` returns `nil`, and evaluation stops. If we start with the number 0 and use inc as our function:
;;   
;;       user=> user=> (expand inc 0 10)
;;       (0 1 2 3 4 5 6 7 8 9)
;;       
;;   
;;   Clojure has a more general form of this function, called `iterate`.
;;   
;;       user=> (take 10 (iterate inc 0))
;;       (0 1 2 3 4 5 6 7 8 9)
;;       
;;   
;;   Since this sequence is _infinitely_ long, we’re using `take` to select only the first 10 elements. We can construct more complex sequences by using more complex functions:
;;   
;;       user=> (take 10 (iterate (fn [x] (if (odd? x) (+ 1 x) (/ x 2))) 10))
;;       (10 5 6 3 4 2 1 2 1 2)
;;       
;;   
;;   Or build up strings:
;;   
;;       user=> (take 5 (iterate (fn [x] (str x "o")) "y"))
;;       ("y" "yo" "yoo" "yooo" "yoooo")
;;       
;;   
;;   `iterate` is extremely handy for working with infinite sequences, and has some partners in crime. `repeat`, for instance, constructs a sequence where every element is the same.
;;   
;;       user=> (take 10 (repeat :hi))
;;       (:hi :hi :hi :hi :hi :hi :hi :hi :hi :hi)
;;       user=> (repeat 3 :echo)
;;       (:echo :echo :echo)
;;       
;;   
;;   And its close relative `repeatedly` simply calls a function `(f)` to generate an infinite sequence of values, over and over again, without any relationship between elements. For an infinite sequence of random numbers:
;;   
;;       user=> (rand)
;;       0.9002678382322784
;;       user=> (rand)
;;       0.12375594203332863
;;       user=> (take 3 (repeatedly rand))
;;       (0.44442397843046755 0.33668691162169784 0.18244875487846746)
;;       
;;   
;;   Notice that calling `(rand)` returns a different number each time. We say that `rand` is an _impure_ function, because it cannot simply be replaced by the same value every time. It does something different each time it’s called.
;;   
;;   There’s another very handy sequence function specifically for numbers: `range`, which generates a sequence of numbers between two points. `(range n)` gives n successive integers starting at 0. `(range n m)` returns integers from n to m-1. `(range n m step)` returns integers from n to m, but separated by `step`.
;;   
;;       user=> (range 5)
;;       (0 1 2 3 4)
;;       user=> (range 2 10)
;;       (2 3 4 5 6 7 8 9)
;;       user=> (range 0 100 5)
;;       (0 5 10 15 20 25 30 35 40 45 50 55 60 65 70 75 80 85 90 95)
;;       
;;   
;;   To extend a sequence by repeating it forever, use `cycle`:
;;   
;;       user=> (take 10 (cycle [1 2 3]))
;;       (1 2 3 1 2 3 1 2 3 1)
;;       
;;   
;;   [Transforming sequences](#transforming-sequences)
;;   -------------------------------------------------
;;   
;;   Given a sequence, we often want to find a _related_ sequence. `map`, for instance, applies a function to each element–but has a few more tricks up its sleeve.
;;   
;;       user=> (map (fn [n vehicle] (str "I've got " n " " vehicle "s"))
;;                [0 200 9]
;;                ["car" "train" "kiteboard"])
;;       ("I've got 0 cars" "I've got 200 trains" "I've got 9 kiteboards")
;;       
;;   
;;   If given multiple sequences, `map` calls its function with one element from each sequence in turn. So the first value will be `(f 0 "car")`, the second `(f 200 "train")`, and so on. Like a zipper, map folds together corresponding elements from multiple collections. To sum three vectors, column-wise:
;;   
;;       user=> (map + [1 2 3]
;;                     [4 5 6]
;;                     [7 8 9])
;;       (12 15 18)
;;       
;;   
;;   If one sequence is bigger than another, map stops at the end of the smaller one. We can exploit this to combine finite and infinite sequences. For example, to number the elements in a vector:
;;   
;;       user=> (map (fn [index element] (str index ". " element))
;;                   (iterate inc 0)
;;                   ["erlang" "ruby" "haskell"])
;;       ("0. erlang" "1. ruby" "2. haskell")
;;       
;;   
;;   Transforming elements together with their indices is so common that Clojure has a special function for it: `map-indexed`:
;;   
;;       user=> (map-indexed (fn [index element] (str index ". " element))
;;                           ["erlang" "ruby" "haskell"])
;;       ("0. erlang" "1. ruby" "2. haskell")
;;       
;;   
;;   You can also tack one sequence onto the end of another, like so:
;;   
;;       user=> (concat [1 2 3] [:a :b :c] [4 5 6])
;;       (1 2 3 :a :b :c 4 5 6)
;;       
;;   
;;   Another way to combine two sequences is to riffle them together, using `interleave`.
;;   
;;       user=> (interleave [:a :b :c] [1 2 3])
;;       (:a 1 :b 2 :c 3)
;;       
;;   
;;   And if you want to insert a specific element between each successive pair in a sequence, try `interpose`:
;;   
;;       user=> (interpose :and [1 2 3 4])
;;       (1 :and 2 :and 3 :and 4)
;;       
;;   
;;   To reverse a sequence, use `reverse`.
;;   
;;       user=> (reverse [1 2 3])
;;       (3 2 1)
;;       user=> (reverse "woolf")
;;       (\f \l \o \o \w)
;;       
;;   
;;   Strings are sequences too! Each element of a string is a _character_, written `\f`. You can rejoin those characters into a string with `apply str`:
;;   
;;       user=> (apply str (reverse "woolf"))
;;       "floow"
;;       
;;   
;;   …and break strings up into sequences of chars with `seq`.
;;   
;;       user=> (seq "sato")
;;       (\s \a \t \o)
;;       
;;   
;;   To randomize the order of a sequence, use `shuffle`.
;;   
;;       user=> (shuffle [1 2 3 4])
;;       [3 1 2 4]
;;       user=> (apply str (shuffle (seq "abracadabra")))
;;       "acaadabrrab"
;;       
;;   
;;   [Subsequences](#subsequences)
;;   -----------------------------
;;   
;;   We’ve already seen `take`, which selects the first n elements. There’s also `drop`, which removes the first n elements.
;;   
;;       user=> (range 10)
;;       (0 1 2 3 4 5 6 7 8 9)
;;       user=> (take 3 (range 10))
;;       (0 1 2)
;;       user=> (drop 3 (range 10))
;;       (3 4 5 6 7 8 9)
;;       
;;   
;;   And for slicing apart the other end of the sequence, we have `take-last` and `drop-last`:
;;   
;;       user=> (take-last 3 (range 10))
;;       (7 8 9)
;;       user=> (drop-last 3 (range 10))
;;       (0 1 2 3 4 5 6)
;;       
;;   
;;   `take-while` and `drop-while` work just like `take` and `drop`, but use a function to decide when to cut.
;;   
;;       user=> (take-while pos? [3 2 1 0 -1 -2 10])
;;       (3 2 1)
;;       
;;   
;;   In general, one can cut a sequence in twain by using `split-at`, and giving it a particular index. There’s also `split-with`, which uses a function to decide when to cut.
;;   
;;       (split-at 4 (range 10))
;;       [(0 1 2 3) (4 5 6 7 8 9)]
;;       user=> (split-with number? [1 2 3 :mark 4 5 6 :mark 7])
;;       [(1 2 3) (:mark 4 5 6 :mark 7)]
;;       
;;   
;;   Notice that because indexes start at zero, sequence functions tend to have predictable numbers of elements. `(split-at 4)` yields _four_ elements in the first collection, and ensures the second collection _begins at index four_. `(range 10)` has ten elements, corresponding to the first ten indices in a sequence. `(range 3 5)` has two (since 5 - 3 is two) elements. These choices simplify the definition of recursive functions as well.
;;   
;;   We can select particular elements from a sequence by applying a function. To find all positive numbers in a list, use `filter`:
;;   
;;       user=> (filter pos? [1 5 -4 -7 3 0])
;;       (1 5 3)
;;       
;;   
;;   `filter` looks at each element in turn, and includes it in the resulting sequence _only_ if `(f element)` returns a truthy value. Its complement is `remove`, which only includes those elements where `(f element)` is `false` or `nil`.
;;   
;;       user=> (remove string? [1 "turing" :apple])
;;       (1 :apple)
;;       
;;   
;;   Finally, one can group a sequence into chunks using `partition`, `partition-all`, or `partition-by`. For instance, one might group alternating values into pairs:
;;   
;;       user=> (partition 2 [:cats 5 :bats 27 :crocodiles 0])
;;       ((:cats 5) (:bats 27) (:crocodiles 0))
;;       
;;   
;;   Or separate a series of numbers into negative and positive runs:
;;   
;;       (user=> (partition-by neg? [1 2 3 2 1 -1 -2 -3 -2 -1 1 2])
;;       ((1 2 3 2 1) (-1 -2 -3 -2 -1) (1 2))
;;       
;;   
;;   [Collapsing sequences](#collapsing-sequences)
;;   ---------------------------------------------
;;   
;;   After transforming a sequence, we often want to collapse it in some way; to derive some smaller value. For instance, we might want the number of times each element appears in a sequence:
;;   
;;       user=> (frequencies [:meow :mrrrow :meow :meow])
;;       {:meow 3, :mrrrow 1}
;;       
;;   
;;   Or to group elements by some function:
;;   
;;       user=> (pprint (group-by :first [{:first "Li"    :last "Zhou"}
;;                                        {:first "Sarah" :last "Lee"}
;;                                        {:first "Sarah" :last "Dunn"}
;;                                        {:first "Li"    :last "O'Toole"}]))
;;       {"Li"    [{:last "Zhou", :first "Li"}   {:last "O'Toole", :first "Li"}],
;;        "Sarah" [{:last "Lee", :first "Sarah"} {:last "Dunn", :first "Sarah"}]}
;;       
;;   
;;   Here we’ve taken a sequence of people with first and last names, and used the `:first` keyword (which can act as a function!) to look up those first names. `group-by` used that function to produce a _map_ of first names to lists of people–kind of like an index.
;;   
;;   In general, we want to _combine_ elements together in some way, using a function. Where `map` treated each element independently, reducing a sequence requires that we bring some information along. The most general way to collapse a sequence is `reduce`.
;;   
;;       user=> (doc reduce)
;;       -------------------------
;;       clojure.core/reduce
;;       ([f coll] [f val coll])
;;         f should be a function of 2 arguments. If val is not supplied,
;;         returns the result of applying f to the first 2 items in coll, then
;;         applying f to that result and the 3rd item, etc. If coll contains no
;;         items, f must accept no arguments as well, and reduce returns the
;;         result of calling f with no arguments.  If coll has only 1 item, it
;;         is returned and f is not called.  If val is supplied, returns the
;;         result of applying f to val and the first item in coll, then
;;         applying f to that result and the 2nd item, etc. If coll contains no
;;         items, returns val and f is not called.
;;       
;;   
;;   That’s a little complicated, so we’ll start small. We need a function, `f`, which combines successive elements of the sequence. `(f state element)` will return the state for the _next_ invocation of `f`. As `f` moves along the sequence, it carries some changing state with it. The final state is the return value of `reduce`.
;;   
;;       user=> (reduce + [1 2 3 4])
;;       10
;;       
;;   
;;   `reduce` begins by calling `(+ 1 2)`, which yields the state `3`. Then it calls `(+ 3 3)`, which yields `6`. Then `(+ 6 4)`, which returns `10`. We’ve taken a function over _two_ elements, and used it to combine _all_ the elements. Mathematically, we could write:
;;   
;;       1 + 2 + 3 + 4
;;           3 + 3 + 4
;;               6 + 4
;;                  10
;;       
;;   
;;   So another way to look at `reduce` is like sticking a function _between_ each pair of elements. To see the reducing process in action, we can use `reductions`, which returns a sequence of all the intermediate states.
;;   
;;       user=> (reductions + [1 2 3 4])
;;       (1 3 6 10)
;;       
;;   
;;   Oftentimes we include a _default_ state to start with. For instance, we could start with an empty set, and add each element to it as we go along:
;;   
;;       user=> (reduce conj #{} [:a :b :b :b :a :a])
;;       #{:a :b}
;;       
;;   
;;   Reducing elements into a collection has its own name: `into`. We can conj `[key value]` vectors into a map, for instance, or build up a list:
;;   
;;       user=> (into {} [[:a 2] [:b 3]])
;;       {:a 2, :b 3}
;;       user=> (into (list) [1 2 3 4])
;;       (4 3 2 1)
;;       
;;   
;;   Because elements added to a list appear at the _beginning_, not the end, this expression reverses the sequence. Vectors `conj` onto the end, so to emit the elements in order, using `reduce`, we might try:
;;   
;;       user=> (reduce conj [] [1 2 3 4 5])
;;       (reduce conj [] [1 2 3 4 5])
;;       [1 2 3 4 5]
;;       
;;   
;;   Which brings up an interesting thought: this looks an awful lot like `map`. All that’s missing is some kind of transformation applied to each element.
;;   
;;       (defn my-map [f coll]
;;         (reduce (fn [output element]
;;                   (conj output (f element)))
;;                 []
;;                 coll))
;;       user=> (my-map inc [1 2 3 4])
;;       [2 3 4 5]
;;       
;;   
;;   Huh. `map` is just a special kind of `reduce`. What about, say, `take-while`?
;;   
;;       (defn my-take-while [f coll]
;;         (reduce (fn [out elem]
;;                   (if (f elem)
;;                     (conj out elem)
;;                     (reduced out)))
;;                 []
;;                 coll))
;;       
;;   
;;   We’re using a special function here, `reduced`, to indicate that we’ve completed our reduction _early_ and can skip the rest of the sequence.
;;   
;;       user=> (my-take-while pos? [2 1 0 -1 0 1 2])
;;       [2 1]
;;       
;;   
;;   `reduce` really is the uberfunction over sequences. Almost any operation on a sequence can be expressed in terms of a reduce–though for various reasons, many of the Clojure sequence functions are not written this way. For instance, `take-while` is _actually_ defined like so:
;;   
;;       user=> (source take-while)
;;       (defn take-while
;;         "Returns a lazy sequence of successive items from coll while
;;         (pred item) returns true. pred must be free of side-effects."
;;         {:added "1.0"
;;          :static true}
;;         [pred coll]
;;         (lazy-seq
;;          (when-let [s (seq coll)]
;;              (when (pred (first s))
;;                (cons (first s) (take-while pred (rest s)))))))
;;       
;;   
;;   There’s a few new pieces here, but the structure is _essentially_ the same as our initial attempt at writing `map`. When the predicate matches the first element, cons the first element onto `take-while`, applied to the rest of the sequence. That `lazy-seq` construct allows Clojure to compute this sequence _as required_, instead of right away. It defers execution to a later time.
;;   
;;   Most of Clojure’s sequence functions are lazy. They don’t do anything until needed. For instance, we can increment every number from zero to infinity:
;;   
;;       user=> (def infseq (map inc (iterate inc 0)))
;;       #'user/infseq
;;       user=> (realized? infseq)
;;       false
;;       
;;   
;;   That function returned _immediately_. Because it hasn’t done any work yet, we say the sequence is _unrealized_. It doesn’t increment any numbers at all until we ask for them:
;;   
;;       user=> (take 10 infseq)
;;       (1 2 3 4 5 6 7 8 9 10)
;;       user=> (realized? infseq)
;;       true
;;       
;;   
;;   Lazy sequences also _remember_ their contents, once evaluated, for faster access.
;;   
;;   [Putting it all together](#putting-it-all-together)
;;   ---------------------------------------------------
;;   
;;   We’ve seen how recursion generalizes a function over _one_ thing into a function over _many_ things, and discovered a rich landscape of recursive functions over sequences. Now let’s use our knowledge of sequences to solve a more complex problem: find the sum of the products of consecutive pairs of the first 1000 odd integers.
;;   
;;   First, we’ll need the integers. We can start with 0, and work our way up to infinity. To save time printing an infinite number of integers, we’ll start with just the first 10.
;;   
;;       user=> (take 10 (iterate inc 0))
;;       (0 1 2 3 4 5 6 7 8 9)
;;       
;;   
;;   Now we need to find only the ones which are odd. Remember, `filter` pares down a sequence to only those elements which pass a test.
;;   
;;       user=> (take 10 (filter odd? (iterate inc 0)))
;;       (1 3 5 7 9 11 13 15 17 19)
;;       
;;   
;;   For consecutive pairs, we want to take `[1 3 5 7 ...]` and find a sequence like `([1 3] [3 5] [5 7] ...)`. That sounds like a job for `partition`:
;;   
;;       user=> (take 3 (partition 2 (filter odd? (iterate inc 0))))
;;       ((1 3) (5 7) (9 11))
;;       
;;   
;;   Not quite right–this gave us non-overlapping pairs, but we wanted overlapping ones too. A quick check of `(doc partition)` reveals the `step` parameter:
;;   
;;       user=> (take 3 (partition 2 1 (filter odd? (iterate inc 0))))
;;       ((1 3) (3 5) (5 7))
;;       
;;   
;;   Now we need to find the product for each pair. Given a pair, multiply the two pieces together… yes, that sounds like `map`:
;;   
;;       user=> (take 3 (map (fn [pair] (* (first pair) (second pair)))
;;                           (partition 2 1 (filter odd? (iterate inc 0)))))
;;       (3 15 35)
;;       
;;   
;;   Getting a bit unwieldy, isn’t it? Only one final step: sum all those products. We’ll adjust the `take` to include the first 1000, not the first 3, elements.
;;   
;;       user=> (reduce +
;;                      (take 1000
;;                            (map (fn [pair] (* (first pair) (second pair)))
;;                                 (partition 2 1
;;                                           (filter odd?
;;                                                   (iterate inc 0)))))
;;       1335333000
;;       
;;   
;;   The sum of the first thousand products of consecutive pairs of the odd integers starting at 0. See how each part leads to the next? This expression looks a lot like the way we phrased the problem in English–but both English and Lisp expressions are sort of backwards, in a way. The part that _happens first_ appears _deepest_, _last_, in the expression. In a chain of reasoning like this, it’d be nicer to write it in order.
;;   
;;       user=> (->> 0
;;                   (iterate inc)
;;                   (filter odd?)
;;                   (partition 2 1)
;;                   (map (fn [pair]
;;                          (* (first pair) (second pair))))
;;                   (take 1000)
;;                   (reduce +))
;;       1335333000
;;       
;;   
;;   Much easier to read: now everything flows in order, from top to bottom, and we’ve flattened out the deeply nested expressions into a single level. This is how object-oriented languages structure their expressions: as a chain of function invocations, each acting on the previous value.
;;   
;;   But how is this possible? Which expression gets evaluated first? `(take 1000)` isn’t even a valid call–where’s its second argument? How are _any_ of these forms evaluated?
;;   
;;   What kind of arcane function _is_ `->>`?
;;   
;;   All these mysteries, and more, in [Chapter 5: Macros](/posts/305-clojure-from-the-ground-up-macros).
;;   
;;   [Problems](#problems)
;;   ---------------------
;;   
;;   1.  Write a function to find out if a string is a palindrome–that is, if it looks the same forwards and backwards.
;;   2.  Find the number of ’c’s in “abracadabra”.
;;   3.  Write your own version of `filter`.
;;   4.  Find the first 100 prime numbers: 2, 3, 5, 7, 11, 13, 17, ….
;;   
;;   ![Bridget](https://www.gravatar.com/avatar/ab3859e14af1de8343e3dd7cb825aa4e?r=pg&s=96&d=identicon "Bridget")
;;   
;;   Bridget on [2013-11-18](/posts/304-clojure-from-the-ground-up-sequences#comment-1742)
;;   
;;   Thank you! This is a great resource. Please keep it going. Let us know how we can help support you.
;;   
;;   ![Greg](https://www.gravatar.com/avatar/8309770d0c07a8fb7c8d0009c1a13bae?r=pg&s=96&d=identicon "Greg")
;;   
;;   Greg on [2013-11-18](/posts/304-clojure-from-the-ground-up-sequences#comment-1744)
;;   
;;   Are you sure the implementation of inc-more is correct? I believe it should call itself recursively instead of inc-first.
;;   
;;   Aphyr on [2013-11-18](/posts/304-clojure-from-the-ground-up-sequences#comment-1745)
;;   
;;   Thanks Greg, that is a typo. Fixed now. :)
;;   
;;   ![Aphyr](https://www.gravatar.com/avatar/e145b50faf662e70c066b13c98921900?r=pg&s=96&d=identicon "Aphyr")
;;   
;;   ![wuschel](https://www.gravatar.com/avatar/294de3557d9d00b3d2d8a1e6aab028cf?r=pg&s=96&d=identicon "wuschel")
;;   
;;   wuschel on [2013-11-21](/posts/304-clojure-from-the-ground-up-sequences#comment-1749)
;;   
;;   Thank you for posting this.
;;   
;;   I am looking into Lisp based languages these days, and as such I find it always good to get more basic information on language semantics.
;;   
;;   There are a couple of things however that make a transition to Clojure very hard, and I think it would do some good if more information about these obstacles could be written down:
;;   
;;   1) A lot of work needs to be invested to get a IDE going. I wasted a lot of time with emacs and Sublime Text 2/SublimeREPL before settling for Light Table. Note: I find the first two choices to be excellent editors, it is just that it is hard to get them going the way one wants to.
;;   
;;   2) The JVM is said to be one of Clojures greatest assets. For me - I started programming with Python - reading java lingo error tracebacks and browsing the source code of JVM packages is very unpleasent experience.
;;   
;;   An introduction on how to tackle the JVM library ‘problem’ in an efficient way a la Python would be great.
;;   
;;   3) Compilation to JAR, running a service on GAE/Amazon without loosing much time on JVM startup and other practical things might be a nice to have in your guide.
;;   
;;   4) Since multithreading is one of the key characteristics that sets Clojure apart from other lisp/scheme based languages e.g. the easy but great Chicken Scheme, it would be great to have a good look on parallel computing in the tutorial.
;;   
;;   Cheers!
;;   
;;   ![Bryan Lott](https://www.gravatar.com/avatar/1788568147f6e1540bd46ae7b2792571?r=pg&s=96&d=identicon "Bryan Lott")
;;   
;;   Bryan Lott on [2013-11-22](/posts/304-clojure-from-the-ground-up-sequences#comment-1750)
;;   
;;   Thank you for posting this!!! This is the first tutorial I’ve done on Clojure that I feel has actually gotten me comfortable with the language. Can’t wait for the next installment :)
;;   
;;   ![John Sanda](https://www.gravatar.com/avatar/e02e10bf82e7b1eee699a9d8631b752f?r=pg&s=96&d=identicon "John Sanda")
;;   
;;   John Sanda on [2013-11-23](/posts/304-clojure-from-the-ground-up-sequences#comment-1751)
;;   
;;   This is a great series. Content and delivery are both excellent! I have one question on the section about the infseq lazy sequence. How come that couldn’t simply be expressed as (take 10 (iterate inc 0))?
;;   
;;   ![Boyan Stoyanov](https://www.gravatar.com/avatar/0484037770a20f913d97a865eb3322ad?r=pg&s=96&d=identicon "Boyan Stoyanov")
;;   
;;   [Boyan Stoyanov](http://www.boyanstoyanov.com/) on [2013-11-23](/posts/304-clojure-from-the-ground-up-sequences#comment-1752)
;;   
;;   As a beginner in Clojure (and LISP in general), I could say that this is the most inspiring tutorial I know of for now! Looking forward for the next article.
;;   
;;   ![](https://www.gravatar.com/avatar/294de3557d9d00b3d2d8a1e6aab028cf?r=pg&s=96&d=identicon)
;;   
;;   anonymous on [2013-11-27](/posts/304-clojure-from-the-ground-up-sequences#comment-1754)
;;   
;;   > How come that couldn’t simply be expressed as (take 10 (iterate inc 0))?
;;   
;;   Just demonstrating that lazy operations can be chained together. We’ll talk more about execution order and side effects in a later chapter. :)
;;   
;;   ![Metin Amiroff](https://www.gravatar.com/avatar/85847e68ae13eeb341e5e3add7bb3633?r=pg&s=96&d=identicon "Metin Amiroff")
;;   
;;   [Metin Amiroff](https://Amiroff.com) on [2013-12-06](/posts/304-clojure-from-the-ground-up-sequences#comment-1765)
;;   
;;   Wow, what an expressive and powerful language Clojure is! The last code example really nailed it for me. Thanks for these series, as others already pointed out, they’re the best on the net. Keep them coming please…
;;   
;;   ![Harish N](https://www.gravatar.com/avatar/47c852fc2e90f1a8485472950de2dfc5?r=pg&s=96&d=identicon "Harish N")
;;   
;;   Harish N on [2013-12-22](/posts/304-clojure-from-the-ground-up-sequences#comment-1786)
;;   
;;   Thank you for this Clojure series The exercises are also a great help as they do test one’s learnings
;;   
;;   ![Scott Feeney](https://www.gravatar.com/avatar/76a48cdbd74de8da5e8bc417a1b84fbe?r=pg&s=96&d=identicon "Scott Feeney")
;;   
;;   [Scott Feeney](https://scott.mn) on [2013-12-24](/posts/304-clojure-from-the-ground-up-sequences#comment-1794)
;;   
;;   Hey, great tutorial! I’ve been programming Clojure for a little while, but I’m always looking for resources I can point people to who want to learn, and this is perfect. You also mentioned a few neat functions I didn’t know about (split-with, map-indexed, reductions, and reduced).
;;   
;;   If you’ll pardon my nitpicking, I believe the solution to the problem under “Putting it all together” is incorrect. You state the problem as relating to “consecutive pairs of the first 1000 odd integers”, but your code operates on “the first 1000 consecutive pairs of odd integers”. That is, you include the pair ‘(1999 2001), even though 2001 is the 1001st odd integer. This could be fixed either by changing the problem statement, or by moving “(take 1000)” before/inside the “(partition …)”, yielding the answer 1331333001.
;;   
;;   ![Marko Bonaci](https://www.gravatar.com/avatar/08cff047d246096cee251b541594a52f?r=pg&s=96&d=identicon "Marko Bonaci")
;;   
;;   [Marko Bonaci](http://github.com/mbonaci) on [2014-01-01](/posts/304-clojure-from-the-ground-up-sequences#comment-1797)
;;   
;;   I stumbled upon this tutorial via your superb talks on distributed systems. Now I’m learning Clojure and currently going through “The joy of Clojure” book.
;;   
;;   IMHO, `recur` would fit so nicely within the recursion topic. It’s interesting because I never came across something similar in Java, Scala nor (of course) in JS (maybe it’s there somewhere, but it’s certainly not widely used).
;;   
;;   BTW, inspiring tutorial intro.
;;   
;;   ![Tim McCoy](https://www.gravatar.com/avatar/2deb4308b31391e3fe3b55cb3116bb6f?r=pg&s=96&d=identicon "Tim McCoy")
;;   
;;   Tim McCoy on [2014-01-12](/posts/304-clojure-from-the-ground-up-sequences#comment-1809)
;;   
;;   Well I am new to Clojure. I started with “Clojure Programming” - but this introduction is just what I was looking for. Suggested edit - “The type map relates keys to values.” perhaps could read “The type map relates a set of keys to list of values.”
;;   
;;   ![karri](https://www.gravatar.com/avatar/294de3557d9d00b3d2d8a1e6aab028cf?r=pg&s=96&d=identicon "karri")
;;   
;;   karri on [2014-02-17](/posts/304-clojure-from-the-ground-up-sequences#comment-1821)
;;   
;;   Brilliant. Excellent series on introduction to clojure.
;;   
;;   ![Zvi](https://www.gravatar.com/avatar/c9c5ec4a938031d6d914b86c359be743?r=pg&s=96&d=identicon "Zvi")
;;   
;;   [Zvi](http://twitter.com/nivertech) on [2014-03-19](/posts/304-clojure-from-the-ground-up-sequences#comment-1848)
;;   
;;   What’s more idiomatic:
;;   
;;   (fn pair (second pair))
;;   
;;   or
;;   
;;   #(apply \* %)
;;   
;;   ?
;;   
;;   ![Marc](https://www.gravatar.com/avatar/1aab36c9c6bcd7962cb23f7114bae38a?r=pg&s=96&d=identicon "Marc")
;;   
;;   [Marc](http://github.com/falzm) on [2014-03-21](/posts/304-clojure-from-the-ground-up-sequences#comment-1849)
;;   
;;   Hi Kyle
;;   
;;   Thank you for these very nice series of article on Clojure. Could you give us “the” solution — or the more idiomatic one — to the problems you list at the end of this article? I started solving them, but I’m kind of stuck on the third :/ [https://gist.github.com/falzm/9543903](https://gist.github.com/falzm/9543903)
;;   
;;   Cheers,
;;   
;;   m.
;;   
;;   Aphyr on [2014-06-09](/posts/304-clojure-from-the-ground-up-sequences#comment-1894)
;;   
;;   Marc: Your version is just fine for this stage of the book. More idiomatically, you can use (mapcat …) in place of (apply concat (map …)). Probably the _cleanest_ solution given the tools we’ve explored so far in the book would be
;;   
;;   `(defn filter [f coll] (reduce (fn [results x] (if (f x) (conj results x) results)) coll))`
;;   
;;   But if I were writing this in idiomatic Clojure, I’d use a recursive lazy-seq, which we haven’t covered yet. ;-)
;;   
;;   ![Aphyr](https://www.gravatar.com/avatar/e145b50faf662e70c066b13c98921900?r=pg&s=96&d=identicon "Aphyr")
;;   
;;   ![ajay](https://www.gravatar.com/avatar/294de3557d9d00b3d2d8a1e6aab028cf?r=pg&s=96&d=identicon "ajay")
;;   
;;   ajay on [2014-08-06](/posts/304-clojure-from-the-ground-up-sequences#comment-1943)
;;   
;;   Thank you for this great tutorial.
;;   
;;   ![larrylv](https://www.gravatar.com/avatar/56aebe7431dc8cfec47697da54426a21?r=pg&s=96&d=identicon "larrylv")
;;   
;;   [larrylv](http://larrylv.com) on [2014-09-06](/posts/304-clojure-from-the-ground-up-sequences#comment-1950)
;;   
;;   Hi Kyle
;;   
;;   I think your solution given above might be missing a default value for `results`.
;;   
;;   `(defn my-filter [f coll] (reduce (fn [results x] (if (f x) (conj results x) results)) [] coll))`
;;   
;;   ![larrylv](https://www.gravatar.com/avatar/56aebe7431dc8cfec47697da54426a21?r=pg&s=96&d=identicon "larrylv")
;;   
;;   [larrylv](http://larrylv.com) on [2014-09-06](/posts/304-clojure-from-the-ground-up-sequences#comment-1951)
;;   
;;   And, just forgot to say. Thanks for this series of article on Clojure! Learned a lot from them.
;;   
;;   ![m](https://www.gravatar.com/avatar/294de3557d9d00b3d2d8a1e6aab028cf?r=pg&s=96&d=identicon "m")
;;   
;;   m on [2014-10-30](/posts/304-clojure-from-the-ground-up-sequences#comment-1974)
;;   
;;   This is my favorite intro to recursion example I’ve seen so far!
;;   
;;   ![nowherekai](https://www.gravatar.com/avatar/0e129c3fa7d426e1fe73fed159ac70b4?r=pg&s=96&d=identicon "nowherekai")
;;   
;;   nowherekai on [2014-11-07](/posts/304-clojure-from-the-ground-up-sequences#comment-1980)
;;   
;;   these articles about clojure are awesome, most clojure book is hard to study for beginners of clojure, help me a lot.
;;   
;;   ![hil](https://www.gravatar.com/avatar/294de3557d9d00b3d2d8a1e6aab028cf?r=pg&s=96&d=identicon "hil")
;;   
;;   hil on [2015-01-23](/posts/304-clojure-from-the-ground-up-sequences#comment-2051)
;;   
;;   great!
;;   
;;   ![Ron](https://www.gravatar.com/avatar/f6992d2102fd30c7e2bd9d4d73a03df8?r=pg&s=96&d=identicon "Ron")
;;   
;;   Ron on [2015-03-19](/posts/304-clojure-from-the-ground-up-sequences#comment-2159)
;;   
;;   Great Work. I can tell you it’s not easy to find the clear explanations for a newbie. Most people lose track of what you need to know first when you don’t know. Their stuff is good once your going but it’s a rare teacher who can GET you going so you start to intuit how programming goes. Many THX
;;   
;;   ![ignace](https://www.gravatar.com/avatar/a140ea65cedaf06eae65aed3bbf15641?r=pg&s=96&d=identicon "ignace")
;;   
;;   ignace on [2015-03-21](/posts/304-clojure-from-the-ground-up-sequences#comment-2163)
;;   
;;   \->> (Kyle) (Awesome)
;;   
;;   ![joe](https://www.gravatar.com/avatar/c5f3e1b6edacfb4d44a3af35b24e8313?r=pg&s=96&d=identicon "joe")
;;   
;;   joe on [2015-03-29](/posts/304-clojure-from-the-ground-up-sequences#comment-2193)
;;   
;;   Hey, I was wondering about tail optimization since Clojure is based on the JVM. So I tried the “transform-all” example on a very big vector and as excepted I got a StackOverflowError. I’ve since read that “recur” is aimed to circumvent this but I’m still learning about it so I can’t explain any further :) Maybe that’s something that could interest your readers (or it is mentionned later in the tutorial and haven’t read it yet)
;;   
;;   Un gros merci pour ce beau tutoriel ! Jo
;;   
;;   ![Ashutosh Pandit](https://www.gravatar.com/avatar/6250ca31b04574c3ca2e4226a05706c7?r=pg&s=96&d=identicon "Ashutosh Pandit")
;;   
;;   Ashutosh Pandit on [2015-07-11](/posts/304-clojure-from-the-ground-up-sequences#comment-2466)
;;   
;;   This is how tutorials should be. Precise and crisp! Thank you! This is my official “learn clojure in 3 days” guide :)
;;   
;;   ![Betty](https://www.gravatar.com/avatar/7fb0d2d54d7c059e5eb3eda8378dc33e?r=pg&s=96&d=identicon "Betty")
;;   
;;   Betty on [2015-08-03](/posts/304-clojure-from-the-ground-up-sequences#comment-2474)
;;   
;;   Hi guys,
;;   
;;   Here are my solutions to the problems. Hope some of you find them helpful.
;;   
;;   1.
;;   
;;   (defn palindrome? \[x\]
;;   
;;   `(= (seq x) (reverse (seq x)) )`
;;   
;;   )
;;   
;;   2.
;;   
;;   ( (frequencies (seq “abracadabra”)) \\c )
;;   
;;   3.
;;   
;;   (defn my-filter \[f x\] (reduce (fn \[output element\]
;;   
;;   `(if (f element) (conj output element) output ) ) [] x )`
;;   
;;   )
;;   
;;   4.
;;   
;;   (defn isprime? n) (range 2 n)))))
;;   
;;   (take 10 (filter isprime? (iterate inc 0)))
;;   
;;   ![Jeff](https://www.gravatar.com/avatar/294de3557d9d00b3d2d8a1e6aab028cf?r=pg&s=96&d=identicon "Jeff")
;;   
;;   Jeff on [2015-08-19](/posts/304-clojure-from-the-ground-up-sequences#comment-2492)
;;   
;;   Hi Kyle,
;;   
;;   Thanks for the amazing clojure tutorial; I’m really enjoying it so far!
;;   
;;   One question about the complex problem at the end of this one: I’m pretty sure that in your solution, you’re taking the first 1000 pairs of odd integers rather than pairing the first 1000 odd integers. From my understanding, the solution to the problem as stated should read:
;;   
;;   `(reduce + (map (fn [pair] (* (first pair) (second pair))) (partition 2 1 (take 1000 (filter odd? (iterate inc 0))))))`
;;   
;;   ![John C](https://www.gravatar.com/avatar/294de3557d9d00b3d2d8a1e6aab028cf?r=pg&s=96&d=identicon "John C")
;;   
;;   John C on [2015-11-21](/posts/304-clojure-from-the-ground-up-sequences#comment-2567)
;;   
;;   I also want to say how much I appreciate these tutorials, this sequence-page alone has given me a lot of insight into how Clojure works. (In fact it’s the only page I’ve read so far, it came up when I was googling sequences - I definitely look forward to the rest of the articles).
;;   
;;   I thought I’d post my solution to writing a filter also - not because it’s especially _good_ code, but because it’s bad - or at least very _procedural_.
;;   
;;   `(defn my_filter [func coll] (loop [output [] new_coll coll] (if (empty? new_coll) (println "output is " output) (if (func (first new_coll)) (recur (conj output (first new_coll)) (rest new_coll)) (recur output (rest new_coll))))))`
;;   
;;   ![not chuck](https://www.gravatar.com/avatar/294de3557d9d00b3d2d8a1e6aab028cf?r=pg&s=96&d=identicon "not chuck")
;;   
;;   not chuck on [2016-05-14](/posts/304-clojure-from-the-ground-up-sequences#comment-2644)
;;   
;;   Check for palindrome:
;;   
;;   (defn Palin? s))))
;;   
;;   Returns true if the word is a palindrome, otherwise false.
;;   
;;   Lovin' the tutorial. Coming from over 20 years of C style syntax this is really easing the learning curve. Thanks!
;;   
;;   ![](https://www.gravatar.com/avatar/294de3557d9d00b3d2d8a1e6aab028cf?r=pg&s=96&d=identicon)
;;   
;;   anonymous on [2016-05-14](/posts/304-clojure-from-the-ground-up-sequences#comment-2648)
;;   
;;   `(defn Palin? [s] (zero? (compare s (apply str (reverse s)))))`
;;   
;;   ![not chuck](https://www.gravatar.com/avatar/294de3557d9d00b3d2d8a1e6aab028cf?r=pg&s=96&d=identicon "not chuck")
;;   
;;   not chuck on [2016-05-14](/posts/304-clojure-from-the-ground-up-sequences#comment-2649)
;;   
;;   Sorry for making such a mess above. I failed to read the fine print before posting and then didn’t follow through on the first attempt.
;;   
;;   Still lovin' the tutorial…
;;   
;;   ![leordev](https://www.gravatar.com/avatar/5527f305f7e0912de4e7ce197e1bc03a?r=pg&s=96&d=identicon "leordev")
;;   
;;   [leordev](http://leordev.github.io/portfolio) on [2016-07-06](/posts/304-clojure-from-the-ground-up-sequences#comment-2672)
;;   
;;   Hey Not Chuck, why not just do this?
;;   
;;   (defn Palin? s)))
;;   
;;   ;)
;;   
;;   ![leordev](https://www.gravatar.com/avatar/5527f305f7e0912de4e7ce197e1bc03a?r=pg&s=96&d=identicon "leordev")
;;   
;;   [leordev](http://leordev.github.io/portfolio) on [2016-07-06](/posts/304-clojure-from-the-ground-up-sequences#comment-2673)
;;   
;;   (sorry for the mess above) This is the correct piece of code to simplify your function:
;;   
;;   `(defn palin? "returns true if its a palindrome, otherwise false" [x] (= x (apply str (reverse x))))`
;;   
;;   ![Chad Stovern](https://www.gravatar.com/avatar/8ae1c0eb4d330eb21f2f348c164dfc7b?r=pg&s=96&d=identicon "Chad Stovern")
;;   
;;   [Chad Stovern](http://www.chadstovern.com) on [2016-10-07](/posts/304-clojure-from-the-ground-up-sequences#comment-2701)
;;   
;;   Hey Kyle, loving this series so far. Any feedback on my version of filter?
;;   
;;   `(defn my-filter "takes a predicate and a collection, applies the predicate to each item in the collection, returns a collection of all items that return true." [predicate collection] (if-let [item (first collection)] (if-let [true-item (predicate item)] (cons item (my-filter predicate (rest collection))) (my-filter predicate (rest collection))) collection))`
;;   
;;   ![Chad Stovern](https://www.gravatar.com/avatar/8ae1c0eb4d330eb21f2f348c164dfc7b?r=pg&s=96&d=identicon "Chad Stovern")
;;   
;;   [Chad Stovern](http://www.chadstovern.com) on [2016-10-07](/posts/304-clojure-from-the-ground-up-sequences#comment-2702)
;;   
;;   ^ just realized i wasn’t using `true-item` and changed that `if-let` to an `if`
;;   
;;   Aphyr on [2016-10-07](/posts/304-clojure-from-the-ground-up-sequences#comment-2703)
;;   
;;   Looks good, Chad. :)
;;   
;;   ![Aphyr](https://www.gravatar.com/avatar/e145b50faf662e70c066b13c98921900?r=pg&s=96&d=identicon "Aphyr")
;;   
;;   ![Bansari](https://www.gravatar.com/avatar/c657555ce4338dd8a5e8605cbf0460f0?r=pg&s=96&d=identicon "Bansari")
;;   
;;   Bansari on [2017-04-04](/posts/304-clojure-from-the-ground-up-sequences#comment-2807)
;;   
;;   Thank you so much for sharing this tutorial.
;;   
;;   ![Radu](https://www.gravatar.com/avatar/acb14839281bb4dd08b561eccfa2f529?r=pg&s=96&d=identicon "Radu")
;;   
;;   Radu on [2018-04-02](/posts/304-clojure-from-the-ground-up-sequences#comment-2946)
;;   
;;   Awesome guide! Thanks!
;;   
;;   ![Lutz](https://www.gravatar.com/avatar/58e8c06ae19b3b6d880e097cf2460eda?r=pg&s=96&d=identicon "Lutz")
;;   
;;   Lutz on [2018-07-05](/posts/304-clojure-from-the-ground-up-sequences#comment-2965)
;;   
;;   Thanks again for the nice tutorial.
;;   
;;   I want to suggest an improvement that is purely aesthetic (subject to personal taste).
;;   
;;   I suggest replacing the sequence in this expression: `(take 10 (iterate (fn [x] (if (odd? x) (+ 1 x) (/ x 2))) 10))` by the much more interesting [Collatz sequence](https://en.wikipedia.org/wiki/Collatz_conjecture): `(take 10 (iterate (fn [x] (if (odd? x) (+ 1 (* 3 x)) (/ x 2))) 10))`
;;   
;;   We don’t know if this sequence ends in `...2 1 2 1 2 1` for each initial value eventually, whereas for the first sequence that is pretty easy to prove.
;;   
;;   ![bartuka](https://www.gravatar.com/avatar/78f1e9630a8ec156aaab02737e772a39?r=pg&s=96&d=identicon "bartuka")
;;   
;;   [bartuka](http://bartuka.com) on [2018-08-05](/posts/304-clojure-from-the-ground-up-sequences#comment-2973)
;;   
;;   Hi, very nice tutorial!! Thanks for very clear approach in the right pace.
;;   
;;   Post a Comment
;;   ==============
;;   
;;   Comments are moderated. Links have `nofollow`. Seriously, spammers, give it a rest.
;;   
;;   Please avoid writing anything here unless you're a computer. Captcha  This is also a trap: Comment
;;   
;;   Name 
;;   
;;   E-Mail (for [Gravatar](https://gravatar.com), not published) 
;;   
;;   Personal URL 
;;   
;;   Comment
;;   
;;   Supports [Github-flavored Markdown](https://guides.github.com/features/mastering-markdown/), including `[links](http://foo.com/)`, `*emphasis*`, `_underline_`, `` `code` ``, and `> blockquotes`. Use ` ```clj ` on its own line to start an (e.g.) Clojure code block, and ` ``` ` to end the block.    
;;   
;;   Copyright © 2023 Kyle Kingsbury.  
;;   Also on: [Mastodon](https://woof.group/@aphyr) and [Github](https://github.com/aphyr).
;;   
;;   var \_gaq = \_gaq || \[\]; \_gaq.push(\['\_setAccount', 'UA-9527251-1'\]); \_gaq.push(\['\_trackPageview'\]); (function() { var ga = document.createElement('script'); ga.type = 'text/javascript'; ga.async = true; ga.src = ('https:' == document.location.protocol ? 'https://ssl' : 'http://www') + '.google-analytics.com/ga.js'; var s = document.getElementsByTagName('script')\[0\]; s.parentNode.insertBefore(ga, s); })();
;;     
;;     
;;     
;;     
;;     
;;     
;;     text

)