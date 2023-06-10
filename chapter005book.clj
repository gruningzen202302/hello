(ns chapter005book)

(comment
;;     Clojure from the ground up: macros     window.dataLayer = window.dataLayer || \[\]; function gtag(){dataLayer.push(arguments);} gtag('js', new Date()); gtag('config', 'G-MXDP37S6QL');
;;   
;;       
;;   
;;   *   [Aphyr](/)
;;   *   [About](/about)
;;   *   [Blog](/posts)
;;   *   [Photos](/photos)
;;   *   [Code](http://github.com/aphyr)
;;   
;;*   [Clojure from the ground up: macros](/posts/305-clojure-from-the-ground-up-macros)
;;   ==================================================================================
;;   
;;   [Software](/tags/software) [Clojure](/tags/clojure) [Clojure from the ground up](/tags/clojure-from-the-ground-up)
;;   
;;   2013-11-26
;;   
";;   In [Chapter 1](/posts/301-clojure-from-the-ground-up-welcome), I asserted that the grammar of Lisp is uniform: every expression is a list, beginning with a verb, and followed by some arguments. Evaluation proceeds from left to right, and every element of the list must be evaluated _before_ evaluating the list itself. Yet we just saw, at the end of [Sequences](/posts/304-clojure-from-the-ground-up-sequences), an expression which seemed to _violate_ these rules.
";;   
";;   Clearly, this is not the whole story.
";;   
;;   * [Macroexpansion](#macroexpansion)
;;   ---------------------------------
;;   
";;   There is another phase to evaluating an expression; one which takes place before the rules we’ve followed so far. That process is called _macro-expansion_. During macro-expansion, the _code itself_ is restructured according to some set of rules–rules which you, the programmer, can define.
";;  
)
     (defmacro ignore 
       "Cancels the evaluation of an expression, returning nil instead."
       [expr]
       nil)
;;       user=> 
(ignore (+ 1 2))

;;       nil
;;       
;;
(comment     
";;   `defmacro` looks a lot like `defn`: it has a name, an optional documentation string, an argument vector, and a body–in this case, just `nil`. In this case, it looks like it simply ignored the expr `(+ 1 2)` and returned `nil`–but it’s actually deeper than that. `(+ 1 2)` was _never evaluated at all_."
;;   
;;       user=> 
  (def x 1)
;;       #'user/x
;;       user=> 
  x
;;       1
;;       user=> (ignore (def x 2))
;;       nil
;;       user=> x
;;       1
;;       
;;   
";;   `def` should have defined `x` to be `2` _no matter what_–but that never happened. At macroexpansion time, the expression `(ignore (+ 1 2))` was _replaced_ by the expression `nil`, which was then evaluated to `nil`. Where functions rewrite _values_, macros rewrite _code_.
;;   
;;   To see these different layers in play, let’s try a macro which reverses the order of arguments to a function.
";;   
      (defmacro rev [fun & args]
        (cons fun (reverse args)))
      
;;   
";;   This macro, named `rev`, takes one mandatory argument: a function. Then it takes any number of arguments, which are collected in the list `args`. It constructs a new list, starting with the function, and followed by the arguments, in reverse order.
;;   
;;   First, we macro-expand:
";;   
;;       user=> 
  (macroexpand '(rev str "hi" (+ 1 2)))
       (str (+ 1 2) "hi")
;;       
;;   
";;   So the `rev` macro took `str` as the function, and "
 ;;`"hi""` and `(+ 1 2)` 
"as the arguments; then constructed a new list with the same function, but the arguments reversed. When we _evaluate_ that expression, we get:
";;   
;;       user=> 
      (eval (macroexpand '(rev str "hi" (+ 1 2))))
;;       "3hi"
;;       
;;   
";;   `macroexpand` takes an expression and returns that expression with all macros expanded. `eval` takes an expression and evaluates it. When you type an unquoted expression into the REPL, Clojure macroexpands, then evaluates. Two stages–the first transforming _code_, the second transforming _values_.
";;   
;;   * [Across languages](#across-languages)
;;   -------------------------------------
;;   
";;   Some languages have a _metalanguage_: a language for extending the language itself. In C, for example, macros are implemented by the [C preprocessor](http://www.rt-embedded.com/blog/archives/macros-in-the-c-programming-language/), which has its own syntax for defining expressions, matching patterns in the source code’s text, and replacing that text with other text. But that preprocessor is _not_ C–it is a separate language entirely, with special limitations. In Clojure, the metalanguage is _Clojure itself_–the full power of the language is available to restructure programs. This is called a _procedural_ macro system. Some Lisps, like Scheme, use a macro system based on templating expressions, and still others use more powerful models like _f-expressions_–but that’s a discussion for a later time.
;;   
;;   There is another key difference between Lisp macros and many other macro systems: in Lisp, the macros operate on _expressions_: the data structure of the code itself. Because Lisp code is _written_ explicitly as a data structure, a tree made out of lists, this transformation is natural. You can _see_ the structure of the code, which makes it easy to reason about its transformation. In the C preprocessor, macros operate only on _text_: there is no understanding of the underlying syntax. Even in languages like Scala which have syntactic macros, the fact that the code looks _nothing like_ the syntax tree makes it [cumbersome](http://docs.scala-lang.org/overviews/macros/overview.html) to truly restructure expressions.
;;   
;;   When people say that Lisp’s syntax is “more elegant”, or “more beautiful”, or “simpler”, this is part of what they they mean. By choosing to represent the program directly as a a data structure, we make it much easier to define complex transformations of code itself.
";;   
;;  * [Defining new syntax](#defining-new-syntax)
;;   -------------------------------------------
;;   
";;   What kind of transformations are best expressed with macros?
;;   
;;   Most languages encode special syntactic forms–things like “define a function”, “call a function”, “define a local variable”, “if this, then that”, and so on. In Clojure, these are called _special forms_. `if` is a special form, for instance. Its definition is built into the language core itself; it cannot be reduced into smaller parts.
";;   
       (if (< 3 x)
         "big"
         "small")
;;       
;;   
;;   Or in Javascript:
;;   
;;       if (3 < x) {
;;         return "big";
;;       } else {
;;         return "small";
;;       }
;;       
;;   
";;   In Javascript, Ruby, and many other languages, these special forms are _fixed_. You cannot define your own syntax. For instance, one cannot define `or` in a language like JS or Ruby: it must be defined _for_ you by the language author.
";;   
;;*   In Clojure, `or` is just a macro.
;;    user=> 
(source or)
      (defmacro or
        "Evaluates exprs one at a time, from left to right. If a form
        returns a logical true value, or returns that value and doesn't
        evaluate any of the other expressions, otherwise it returns the
        value of the last expression. (or) returns nil."
        {:added "1.0"}
        ([] nil)
        ([x] x)
        ([x & next]
            `(let [or# ~x]
               (if or# or# (or ~@next)))))
;      nil
;;       
;;   
";;   That `` ` `` operator–that’s called _syntax-quote_. It works just like regular quote–preventing evaluation of the following list–but with a twist: we can escape the quoting rule and substitute in regularly evaluated expressions using _unquote_ (`~`), and _unquote-splice_ (`~@`). Think of a syntax-quoted expression like a _template_ for code, with some parts filled in by evaluated forms.
";;   
;;       user=> 
(let [x 2] `(inc x))
;;       (clojure.core/inc user/x)
;;       user=> 
(let [x 2] `(inc ~x))
;;       (clojure.core/inc 2)
;;       
;;   
";;   See the difference? `~x` _substitutes_ the value of x, instead of using `x` as an unevaluated symbol. This code is essentially just shorthand for something like
";;   
;;       user=> 
(let [x 2] (list 'clojure.core/inc x))
;;       (inc 2)
;;       
;;   
";;   … where we explicitly constructed a new list with the quoted symbol `'inc` and the current value of `x`. Syntax quote just makes it easier to read the code, since the quoted and expanded expressions have similar shapes.
;;   
;;   The `~@` unquote splice works just like `~`, except it explodes a list into _multiple_ expressions in the resulting form:
";;   
;;       user=> 
`(foo ~[1 2 3])
;;       (user/foo [1 2 3])
;;       user=> 
`(foo ~@[1 2 3]) 
;;(user/foo 1 2 3)
;;       
;;   
";;   `~@` is particularly useful when a function or macro takes an _arbitrary_ number of arguments. In the definition of `or`, it’s used to expand `(or a b c)` _recursively_.
";;   
;;       user=> (pprint (macroexpand '(or a b c d)))
;;       (let*
;;        [or__3943__auto__ a]
;;        (if or__3943__auto__ or__3943__auto__ (clojure.core/or b c d)))
;;       
;;   
";;   We’re using `pprint` (for “pretty print”) to make this expression easier to read. `(or a b c d)` is defined in terms of _if_: if the first element is truthy we return it; otherwise we evaluate `(or b c d)` instead, and so on.
;;   
;;   The final piece of the puzzle here is that weirdly named symbol: `or__3943__auto__`. That variable was _automatically generated_ by Clojure, to prevent _conflicts_ with an existing variable name. Because macros rewrite code, they have to be careful not to interfere with local variables, or it could get very confusing. Whenever we need a new variable in a macro, we use `gensym` to _generate a new symbol_.
";;   
;;       user=> (gensym "hi")
;;       hi326
;;       user=> (gensym "hi")
;;       hi329
;;       user=> (gensym "hi")
;;       hi332
;;       
;;   
";;   Each symbol is different! If we tack on a `#` to the end of a symbol in a syntax-quoted expression, it’ll be expanded to a particular gensym:
";;   
;;       user=> `(let [x# 2] x#)
;;       (clojure.core/let [x__339__auto__ 2] x__339__auto__)
;;       
;;   
";;   Note that you can always escape this safety feature if you _want_ to override local variables. That’s called _symbol capture_, or an _anaphoric_ or _unhygenic_ macro. To override local symbols, just use `~'foo` instead of `foo#`.
;;   
;;   With all the pieces on the board, let’s compare the `or` macro and its expansion:
";;   
;;       (defmacro or
;;         "Evaluates exprs one at a time, from left to right. If a form
;;         returns a logical true value, or returns that value and doesn't
;;         evaluate any of the other expressions, otherwise it returns the
;;         value of the last expression. (or) returns nil."
;;         {:added "1.0"}
;;         ([] nil)
;;         ([x] x)
;;         ([x & next]
;;             `(let [or# ~x]
;;                (if or# or# (or ~@next)))))
;;       
;;       user=> (pprint (clojure.walk/macroexpand-all
;;                        '(or (mossy? stone) (cool? stone) (wet? stone))))
;;       (let*
;;        [or__3943__auto__ (mossy? stone)]
;;        (if
;;         or__3943__auto__
;;         or__3943__auto__
;;         (let*
;;          [or__3943__auto__ (cool? stone)]
;;          (if or__3943__auto__ or__3943__auto__ (wet? stone)))))
;;       
;;   
";;   See how the macro’s syntax-quoted `(let ...` has the same shape as the resulting code? `or#` is expanded to a variable named `or__3943__auto__`, which is bound to the expression `(mossy? stone)`. If that variable is truthy, we return it. Otherwise, we (and here’s the recursive part) rebind `or__3943__auto__` to `(cool? stone)` and try again. If _that_ fails, we fall back to evaluating `(wet? stone)`–thanks to the base case, the single-argument form of the `or` macro.
";;   
;; *   [Control flow](#control-flow)
;;   -----------------------------
;;   
;;   We’ve seen that `or` is a macro written in terms of the special form `if`–and because of the way the macro is structured, it does _not_ obey the normal execution order. In `(or a b c)`, only `a` is evaluated first–then, only if it is `false` or `nil`, do we evaluate `b`. This is called _short-circuiting_, and it works for `and` as well.
;;   
;;   Changing the order of evaluation in a language is called _control flow_, and lets programs make decisions based on varying circumstances. We’ve already seen `if`:
;;   
;;       user=> (if (= 2 2) :a :b)
;;       :a
;;       
;;   
;;   `if` takes a predicate and two expressions, and only evaluates one of them, depending on whether the predicate evaluates to a truthy or falsey value. Sometimes you want to evaluate _more than one_ expression in order. For this, we have `do`.
;;   
;;       user=> (if (pos? -5)
;;                (prn "-5 is positive")
;;                (do
;;                  (prn "-5 is negative")
;;                  (prn "Who would have thought?")))
;;       "-5 is negative"
;;       "Who would have thought?"
;;       nil
;;       
;;   
;;   `prn` is a function which has a _side effect_: it prints a message to the screen, and returns `nil`. We wanted to print _two_ messages, but `if` only takes a single expression per branch–so in our false branch, we used `do` to wrap up two `prn`s into a single expression, and evaluate them in order. `do` returns the value of the final expression, which happens to be `nil` here.
;;   
;;   When you only want to take one branch of an `if`, you can use `when`:
;;   
;;       user=> (when false
;;                (prn :hi)
;;                (prn :there))
;;       nil
;;       user=> (when true
;;                (prn :hi)
;;                (prn :there))
;;       :hi
;;       :there
;;       nil
;;       
;;   
;;   Because there is only one path to take, `when` takes any number of expressions, and evaluates them only when the predicate is truthy. If the predicate evaluates to `nil` or `false`, `when` does not evaluate its body, and returns `nil`.
;;   
;;   Both `when` and `if` have complementary forms, `when-not` and `if-not`, which simply invert the sense of their predicate.
;;   
;;       user=> (when-not (number? "a string")
;;                :here)
;;       :here
;;       user=> (if-not (vector? (list 1 2 3))
;;                :a
;;                :b)
;;       :a
;;       
;;   
;;   Often, you want to perform some operation, and if it’s truthy, re-use that value without recomputing it. For this, we have `when-let` and `if-let`. These work just like `when` and `let` combined.
;;   
;;       user=> (when-let [x (+ 1 2 3 4)]
;;                (str x))
;;       "10"
;;       user=> (when-let [x (first [])]
;;                (str x))
;;       nil
;;       
;;   
;;   `while` evaluates an expression so long as its predicate is truthy. This is generally useful only for side effects, like `prn` or `def`; things that change the state of the world.
;;   
;;       user=> (def x 0)
;;       #'user/x
;;       user=> (while (< x 5)
;;         #_=>   (prn x)
;;         #_=>   (def x (inc x)))
;;       0
;;       1
;;       2
;;       3
;;       4
;;       nil
;;       
;;   
;;   `cond` (for “conditional”) is like a multiheaded `if`: it takes _any number_ of test/expression pairs, and tries each test in turn. The first test which evaluates truthy causes the following expression to be evaluated; then `cond` returns that expression’s value.
;;   
;;       user=> (cond
;;         #_=>   (= 2 5) :nope
;;         #_=>   (= 3 3) :yep
;;         #_=>   (= 5 5) :cant-get-here
;;         #_=>   :else   :a-default-value)
;;       :yep
;;       
;;   
;;   If you find yourself making several similar decisions based on a value, try `condp`, for “cond with predicate”. For instance, we might categorize a number based on some ranges:
;;   
;;       (defn category
;;         "Determines the Saffir-Simpson category of a hurricane, by wind speed in meters/sec"
;;         [wind-speed]
;;         (condp <= wind-speed
;;           70 :F5
;;           58 :F4
;;           49 :F3
;;           42 :F2
;;              :F1)) ; Default value
;;       user=> (category 10)
;;       :F1
;;       user=> (category 50)
;;       :F3
;;       user=> (category 100)
;;       :F5
;;       
;;   
;;   `condp` generates code which combines the predicate `<=` with each number, and the value of `wind-speed`, like so:
;;   
;;       (if (<= 70 wind-speed) :F5
;;         (if (<= 58 wind-speed) :F4
;;           (if (<= 49 wind-speed) :F3
;;             (if (<= 42 wind-speed) :F2
;;               :F1))))
;;       
;;   
;;   Specialized macros like `condp` are less commonly used than `if` or `when`, but they still play an important role in simplifying repeated code. They clarify the meaning of complex expressions, making them easier to read and maintain.
;;   
;;   Finally, there’s `case`, which works a little bit like a map of keys to values–only the values are _code_, to be evaluated. You can think of `case` like `(condp = ...)`, trying to match an expression to a particular branch for which it is equal.
;;   
;;       (defn with-tax
;;         "Computes the total cost, with tax, of a purchase in the given state."
;;         [state subtotal]
;;         (case state
;;           :WA (* 1.065 subtotal)
;;           :OR subtotal
;;           :CA (* 1.075 subtotal)
;;           ; ... 48 other states ...
;;           subtotal)) ; a default case
;;       
;;   
;;   Unlike `cond` and `condp`, `case` does _not_ evaluate its tests in order. It jumps _immediately_ to the matching expression. This makes `case` much faster when there are many branches to take–at the cost of reduced generality.
;;   
;;   [Recursion](#recursion)
;;   -----------------------
;;   
;;   Previously, we defined recursive functions by having those functions call themselves explicitly.
;;   
;;       (defn sum [numbers]
;;         (if-let [n (first numbers)]
;;           (+ n (sum (rest numbers)))
;;           0))
;;       user=> (sum (range 10))
;;       45
;;       
;;   
;;   But this approach breaks down when we have the function call itself _deeply_, over and over again.
;;   
;;       user=> (sum (range 100000))
;;       
;;       StackOverflowError   clojure.core/range/fn--4269 (core.clj:2664)
;;       
;;   
;;   Every time you call a function, the arguments for that function are stored in memory, in a region called _the stack_. They remain there for as long as the function is being called–including any deeper function calls.
;;   
;;           (+ n (sum (rest numbers)))
;;       
;;   
;;   In order to add `n` and `(sum (rest numbers))`, we have to call `sum` _first_–while holding onto the memory for `n` and `numbers`. We can’t re-use that memory until _every single recursive call_ has completed. Clojure complains, after tens of thousands of stack frames are in use, that it has run out of space in the stack and can allocate no more.
;;   
;;   But consider this variation on `sum`:
;;   
;;       (defn sum
;;         ([numbers]
;;          (sum 0 numbers))
;;         ([subtotal numbers]
;;          (if-let [n (first numbers)]
;;            (recur (+ subtotal n) (rest numbers))
;;            subtotal)))
;;       user=> (sum (range 100000))
;;       4999950000
;;       
;;   
;;   We’ve added an additional parameter to the function. In its two-argument form, `sum` now takes an accumulator, `subtotal`, which represents the count so far. In addition, `recur` has taken the place of `sum`. Notice, however, that the final expression to be evaluated is not `+`, but `sum` (viz `recur`) itself. We don’t need to hang on to any of the variables in this function any more, because the final return value won’t depend on them. `recur` hints to the Clojure compiler that we _don’t need_ to hold on to the stack, and can re-use that space for other things. This is called a _tail-recursive_ function, and it requires only a single stack frame no matter how deep the recursive calls go.
;;   
;;   Use `recur` wherever possible. It requires much less memory and is much faster than the explicit recursion.
;;   
;;   You can also use `recur` within the context of the `loop` macro, where it acts just like an unnamed recursive function with initial values provided. Think of it, perhaps, like a recursive `let`.
;;   
;;       user=> (loop [i 0
;;                     nums []]
;;                (if (< 10 i)
;;                  nums
;;                  (recur (inc i) (conj nums i))))
;;       [0 1 2 3 4 5 6 7 8 9 10]
;;       
;;   
;;   [Laziness](#laziness)
;;   ---------------------
;;   
;;   In chapter 4 we mentioned that most of the sequences in Clojure, like `map`, `filter`, `iterate`, `repeatedly`, and so on, were _lazy_: they did not evaluate any of their elements until required. This too is provided by a macro, called `lazy-seq`.
;;   
;;       (defn integers
;;         [x]
;;         (lazy-seq
;;           (cons x (integers (inc x)))))
;;       user=> (def xs (integers 0))
;;       #'user/xs
;;       
;;   
;;   This sequence does not terminate; it is _infinitely_ recursive. Yet it returned instantaneously. `lazy-seq` interrupted that recursion and restructured it into a sequence which constructs elements only when they are requested.
;;   
;;       user=> (take 10 xs)
;;       (0 1 2 3 4 5 6 7 8 9)
;;       
;;   
;;   When using `lazy-seq` and its partner `lazy-cat`, you don’t have to use `recur`–or even be tail-recursive. The macros interrupt each level of recursion, preventing stack overflows.
;;   
;;   You can also delay evaluation of some expressions until later, using `delay` and `deref`.
;;   
;;       user=> (def x (delay
;;                       (prn "computing a really big number!")
;;                       (last (take 10000000 (iterate inc 0)))))
;;       #'user/x ; Did nothing, returned immediately
;;       user=> (deref x)
;;       "computing a really big number!" ; Now we have to wait!
;;       9999999
;;       
;;   
;;   [List comprehensions](#list-comprehensions)
;;   -------------------------------------------
;;   
;;   Combining recursion and laziness is the _list comprehension_ macro, `for`. In its simplest form, `for` works like `map`:
;;   
;;       user=> (for [x (range 10)] (- x))
;;       (0 -1 -2 -3 -4 -5 -6 -7 -8 -9)
;;       
;;   
;;   Like `let`, `for` takes a vector of `bindings`. Unlike `let`, however, `for` binds its variables to _each possible combination of elements in their corresponding sequences_.
;;   
;;       user=> (for [x [1 2 3]
;;                    y [:a :b]]
;;                [x y])
;;       ([1 :a] [1 :b] [2 :a] [2 :b] [3 :a] [3 :b])
;;       
;;   
;;   “For each x in the sequence `[1 2 3]`, and for each `y` in the sequence `[:a :b]`, find all `[x y]` pairs.” Note that the rightmost variable `y` iterates the fastest.
;;   
;;   Like most sequence functions, the `for` macro yields lazy sequences. You can filter them with `take`, `filter`, et al like any other sequence. Or you can use `:while` to tell `for` when to stop, or `:when` to filter out combinations of elements.
;;   
;;       (for [x     (range 5)
;;             y     (range 5)
;;             :when (and (even? x) (odd? y))]
;;         [x y])
;;       ([0 1] [0 3] [2 1] [2 3] [4 1] [4 3])
;;       
;;   
;;   Clojure includes a rich smörgåsbord of control-flow constructs; we’ll meet new ones throughout the book.
;;   
;;   [The threading macros](#the-threading-macros)
;;   ---------------------------------------------
;;   
;;   Sometimes you want to _thread_ a computation through several expressions, like a chain. Object-oriented languages like Ruby or Java are well-suited to this style:
;;   
;;       1.9.3p385 :004 > (0..10).select(&:odd?).reduce(&:+)
;;       25
;;       
;;   
;;   Start with the range `0` to `10`, then call `select` on that range, with the function `odd?`. Finally, take _that_ sequence of numbers, and reduce it with the `+` function.
;;   
;;   The Clojure threading macros do the same by restructuring a sequence of expressions, inserting each expression as the first (or final) argument in the next expression.
;;   
;;       user=> (pprint (clojure.walk/macroexpand-all 
;;                '(->> (range 10) (filter odd?) (reduce +))))
;;       (reduce + (filter odd? (range 10)))
;;       user=> (->> (range 10) (filter odd?) (reduce +))
;;       25
;;       
;;   
;;   `->>` took `(range 10)` and inserted it at the end of `(filter odd?)`, forming `(filter odd? (range 10))`. Then it took _that_ expression and inserted it at the end of `(reduce +)`. In essence, `->>` _flattens and reverses_ a nested chain of operations.
;;   
;;   `->`, by contrast, inserts each form in as the _first_ argument in the following expression.
;;   
;;       user=> (pprint (clojure.walk/macroexpand-all 
;;                '(-> {:proton :fermion} (assoc :photon :boson) (assoc :neutrino :fermion))))
;;       (assoc (assoc {:proton :fermion} :photon :boson) :neutrino :fermion)
;;       user=> (-> {:proton :fermion}
;;                  (assoc :photon :boson)
;;                  (assoc :neutrino :fermion))
;;       {:neutrino :fermion, :photon :boson, :proton :fermion}
;;       
;;   
;;   Clojure isn’t just `function-oriented` in its syntax; it can be object-oriented, and stack-oriented, and array-oriented, and so on–and _mix all of these styles freely, in a controlled way_. If you don’t like the way the language fits a certain problem, you can write a macro which defines a _new_ language, specifically for that subproblem.
;;   
;;   `cond`, `condp` and `case`, for example, express a language for branching based on predicates. `->`, `->>`, and `doto` express object-oriented and other expression-chaining languages.
;;   
;;   *   [core.match](https://github.com/clojure/core.match) is a set of macros which express powerful _pattern-matching_ and substitution languages.
;;   *   [core.logic](https://github.com/clojure/core.logic) expresses syntax for _logic programming_, for finding values which satisfy complex constraints.
;;   *   [core.async](http://clojure.com/blog/2013/06/28/clojure-core-async-channels.html) restructures Clojure code into _asynchronous_ forms so they can do many things at once.
;;   *   For those with a twisted sense of humor, [Swiss Arrows](https://github.com/rplevy/swiss-arrows) extends the threading macros into evil–but delightfully concise!–forms.
;;   
;;   We’ll see a plethora of macros, from simple to complex, through the course of this book. Each one shares the common pattern of _simplifying code_; reducing tangled or verbose expressions into something more concise, more meaningful, better suited to the problem at hand.
;;   
;;   [When to use macros](#when-to-use-macros)
;;   -----------------------------------------
;;   
;;   While it’s important to be aware of the purpose and behavior of the macro system, you don’t need to write your own macros to be productive with Clojure. For now, you’ll be just fine writing code which uses the existing macros in the language. When you _do_ need to delve deeper, come back to this guide and experiment. It’ll take some time to sink in.
;;   
;;   First, know that writing macros is _tricky_, even for experts. It requires you to think at two levels simultaneously, and to be mindful of the distinction between _expression_ and underlying _evaluation_. Writing a macro is essentially extending the language, the compiler, the syntax and evaluation model of Clojure, by restructuring _arbitrary_ expressions into ones the evaluation system understands. This is hard, and it’ll take practice to get used to.
;;   
;;   In addition, Clojure macros come with some important restrictions. Because they’re expanded prior to evaluation, macros are invisible to functions. They can’t be composed functionally–you can’t `(map or ...)`, for instance.
;;   
;;   So in general, if you _can_ solve a problem without writing a macro, _don’t write one_. It’ll be easier to debug, easier to understand, and easier to compose later. Only reach for macros when you need _new syntax_, or when performance demands the code be transformed at compile time.
;;   
;;   When you do write a macro, consider its scope carefully. Keep the transformation simple; and do as much in normal functions as possible. Provide an escape hatch where possible, by doing most of the work in a function, and writing a small wrapper macro which calls that function. Finally, remember the distinction between _code_ and what that code _evaluates to_. Use `let` whenever a value is to be re-used, to prevent it being evaluated twice by accident.
;;   
;;   For a deeper exploration of Clojure macros in a real-world application, try [Language Power](http://aphyr.com/posts/268-language-power).
;;   
;;   [Review](#review)
;;   -----------------
;;   
;;   In Chapter 4, deeply nested expressions led to the desire for a _simpler_, _more direct_ expression of a chain of sequence operations. We learned that the Clojure compiler first _expands_ expressions before evaluating them, using macros–special functions which take code and return other code. We used macros to define the short-circuiting `or` operator, and followed that with a tour of basic control flow, recursion, laziness, list comprehensions, and chained expressions. Finally, we learned a bit about when and how to write our own macros.
;;   
;;   Throughout this chapter we’ve brushed against the idea of _side effects_: things which change the outside world. We might change a var with `def`, or print a message to the screen with `prn`. Real languages must model a continually shifting universe, which leads us to [Chapter Six: Side effects and state](http://aphyr.com/posts/306-clojure-from-the-ground-up-state).
;;   
;;   [Problems](#problems)
;;   ---------------------
;;   
;;   1.  Using the control flow constructs we’ve learned, write a `schedule` function which, given an hour of the day, returns what you’ll be doing at that time. `(schedule 18)`, for me, returns `:dinner`.
;;       
;;   2.  Using the threading macros, find how many numbers from 0 to 9999 are palindromes: identical when written forwards and backwards. `121` is a palindrome, as is `7447` and `5`, but not `12` or `953`.
;;       
;;   3.  Write a macro `id` which takes a function and a list of args: `(id f a b c)`, and returns an expression which calls that function with the given args: `(f a b c)`.
;;       
;;   4.  Write a macro `log` which uses a var, `logging-enabled`, to determine whether or not to print an expression to the console at compile time. If `logging-enabled` is false, `(log :hi)` should macroexpand to `nil`. If `logging-enabled` is true, `(log :hi)` should macroexpand to `(prn :hi)`. Why would you want to do this check during _compilation_, instead of when running the program? What might you _lose_?
;;       
;;   5.  (Advanced) Using the `rationalize` function, write a macro `exact` which rewrites any use of `+`, `-`, `*`, or `/` to force the use of _ratios_ instead of [floating-point numbers](http://erlang.org/pipermail/erlang-questions/2013-November/076114.html). `(* 2452.45 100)` returns `245244.99999999997`, but `(exact (* 2452.45 100))` should return `245245N`
;;       
;;   
;;   ![rptabo](https://www.gravatar.com/avatar/294de3557d9d00b3d2d8a1e6aab028cf?r=pg&s=96&d=identicon "rptabo")
;;   
;;   rptabo on [2013-11-30](/posts/305-clojure-from-the-ground-up-macros#comment-1755)
;;   
;;   Kyle, this is fantastic stuff - your “blook” is the best Clojure resource I’ve found after lots of seeking, and I’ve enjoyed exploring some of your other posts.
;;   
;;   I poked around your site but maybe I missed it: is there any way to sign up for email updates (or some other notification) when you make a new post? If I must resort to it, I will check back periodically, but ideally I’d love to be notified as soon as you post something so I can devour it immediately!
;;   
;;   Again, great stuff…can’t wait for what comes next!
;;   
;;   Aphyr on [2013-11-30](/posts/305-clojure-from-the-ground-up-macros#comment-1756)
;;   
;;   Thanks rptabo. Glad to hear these posts are working out well for you. :)
;;   
;;   The index pages and tag pages have ATOM feeds, linked with the standard headers, but I think browsers have started to drift away from displaying those. If you’ve got an ATOM client, try [http://aphyr.com/tags/Clojure-from-the-ground-up.atom](http://aphyr.com/tags/Clojure-from-the-ground-up.atom), or [http://aphyr.com/posts.atom](http://aphyr.com/posts.atom) for all the posts.
;;   
;;   ![Aphyr](https://www.gravatar.com/avatar/e145b50faf662e70c066b13c98921900?r=pg&s=96&d=identicon "Aphyr")
;;   
;;   ![Wayne Conrad](https://www.gravatar.com/avatar/8ca29123b5669b26f9b6ca2c62d9d215?r=pg&s=96&d=identicon "Wayne Conrad")
;;   
;;   Wayne Conrad on [2013-12-03](/posts/305-clojure-from-the-ground-up-macros#comment-1761)
;;   
;;   Perfectly paced–not too fast, not too slow, and your explanations are easy to understand. Thanks for writing these.
;;   
;;   ![eigenlicht](https://www.gravatar.com/avatar/294de3557d9d00b3d2d8a1e6aab028cf?r=pg&s=96&d=identicon "eigenlicht")
;;   
;;   eigenlicht on [2013-12-03](/posts/305-clojure-from-the-ground-up-macros#comment-1762)
;;   
;;   Hey Kyle, great post. You mentioned the -> and ->> threading macros. How about mentioning the as-> macro too? One situation where it comes in handy is for example this: [http://dpassen1.github.io/software/2013/11/17/embracing-the-as–macro/](http://dpassen1.github.io/software/2013/11/17/embracing-the-as--macro/)
;;   
;;   Also, situations where you’d wrap certain function calls in fn’s, because those one or two function calls don’t fit into the pattern of the others inside the ->/->> form.
;;   
;;   ![x](https://www.gravatar.com/avatar/b282b4d44b0ef693a215a2c6eac59ee4?r=pg&s=96&d=identicon "x")
;;   
;;   x on [2013-12-04](/posts/305-clojure-from-the-ground-up-macros#comment-1763)
;;   
;;   +1 from me as well, looking forward to more.
;;   
;;   ![Elf M. Sternberg](https://www.gravatar.com/avatar/0c01a983961c34967d0ccedda9eb7d49?r=pg&s=96&d=identicon "Elf M. Sternberg")
;;   
;;   [Elf M. Sternberg](http://elfsternberg.com) on [2013-12-05](/posts/305-clojure-from-the-ground-up-macros#comment-1764)
;;   
;;   Kyle, awesome explanation, but one thing is bothering me. The (defmacro or…) example doesn’t make sense to me. It shows three expressions in a row. If ‘or’ already existed, I would understand how the first two (the empty set and a single set) short circuit to their definitions, and the actual quasiquoted stuff doesn’t kick in until we get more than one expression to evaluate, but ‘or’ doesn’t exist, you’re defining it here.
;;   
;;   Maybe my LISP is out of date (most of my Lisp experience is within Emacs), but I don’t see how this code resolves. Does ‘defmacro’ have some kind of pattern matching for resolving these zero-argument and one-argument cases before moving on to the multi-argument case?
;;   
;;   Aphyr on [2013-12-16](/posts/305-clojure-from-the-ground-up-macros#comment-1781)
;;   
;;   > Does ‘defmacro’ have some kind of pattern matching for resolving these zero-argument and one-argument cases before moving on to the multi-argument case?
;;   
;;   You’re right, Elf, defmacro (like defn) have arity dispatch provided by the compiler. As for the recursive macro call, where or is defined in terms of itself, that’s also handled by the Clojure compiler. Inside (defn foo \[\] …), ‘foo is lexically bound to the function itself, by the compiler.
;;   
;;   ![Aphyr](https://www.gravatar.com/avatar/e145b50faf662e70c066b13c98921900?r=pg&s=96&d=identicon "Aphyr")
;;   
;;   ![Fran Burstall](https://www.gravatar.com/avatar/f8366289a99ffe6e3a6a44e3f279e0e4?r=pg&s=96&d=identicon "Fran Burstall")
;;   
;;   Fran Burstall on [2014-04-27](/posts/305-clojure-from-the-ground-up-macros#comment-1861)
;;   
;;   Great tutorial but a mistake in the delay/deref example: the string that is printed should match the string “computing a really big number!” in the definition of x.
;;   
;;   ![ignace](https://www.gravatar.com/avatar/dfcca6f0979fa1dfa988fce979cc66eb?r=pg&s=96&d=identicon "ignace")
;;   
;;   ignace on [2015-04-11](/posts/305-clojure-from-the-ground-up-macros#comment-2283)
;;   
;;   Hi Kyle,
;;   
;;   im stuck with with the log macro (#4), not because i’ve tried ( id did try to get into what’s asked) but because i’ve a problem understanding the particulars of the question. I don’t want tips. but would it be possible to phrase the problem in another way, like for total new person that wants to learn clojure?
;;   
;;   ![new clojurian](https://www.gravatar.com/avatar/294de3557d9d00b3d2d8a1e6aab028cf?r=pg&s=96&d=identicon "new clojurian")
;;   
;;   new clojurian on [2017-12-07](/posts/305-clojure-from-the-ground-up-macros#comment-2919)
;;   
;;   Hi,
;;   
;;   for the advanced problem i found this solution:
;;   
;;   ``(defmacro exact [expr] `(rationalize (float ~expr)))``
;;   
;;   But this solution doesn’t work if you call the macro with a let for example.
;;   
;;   `(let [a 100] (exact (* 2452.45 a)))`
;;   
;;   Can someone give me a solution that works every time plz ?
;;   
;;   Thanks you.
;;   
;;   ![rubal](https://www.gravatar.com/avatar/a95db2e273bb6d51a7def374bc166ff7?r=pg&s=96&d=identicon "rubal")
;;   
;;   rubal on [2018-02-07](/posts/305-clojure-from-the-ground-up-macros#comment-2934)
;;   
;;   Great Stuff!
;;   
;;   ![Sid](https://www.gravatar.com/avatar/294de3557d9d00b3d2d8a1e6aab028cf?r=pg&s=96&d=identicon "Sid")
;;   
;;   Sid on [2019-08-21](/posts/305-clojure-from-the-ground-up-macros#comment-3052)
;;   
;;   Thank you for writing this book!
;;   
;;   Here’s my solution to #5, the `exact` macro, for those that may want something to compare against. The strategy is to map over the arguments in the expression and replace each `arg` with `(rationalize arg)` and then paste that back into the code using the syntax quote and unquote-splice (`~@`).
;;   
;;   ``(defmacro exact [expr] (let [op (first expr) op_assert `(assert (#{* / + -} ~op) "exact only operates on +-*/") args (rest expr) rationals (map (fn [arg] `(rationalize ~arg)) args)] `((do ~op_assert ~op) ~@rationals))) (exact (* 2452.45 100)) ; => 245245N``
;;   
;;   ![Joe Bentley](https://www.gravatar.com/avatar/7dfbaa091349bb59e4076b3c7cdafb6d?r=pg&s=96&d=identicon "Joe Bentley")
;;   
;;   Joe Bentley on [2019-12-12](/posts/305-clojure-from-the-ground-up-macros#comment-3105)
;;   
;;   Here’s my solution for the advanced problem. It just walks through the syntax tree and calls `rationalize` on every number
;;   
;;   `(use 'clojure.walk) (defmacro exact [expr] (prewalk (fn [x] (if (number? x) (rationalize x) x)) expr))`
;;   
;;   ![Joe Bentley](https://www.gravatar.com/avatar/7dfbaa091349bb59e4076b3c7cdafb6d?r=pg&s=96&d=identicon "Joe Bentley")
;;   
;;   Joe Bentley on [2019-12-12](/posts/305-clojure-from-the-ground-up-macros#comment-3106)
;;   
;;   Forgot to add examples of the above:
;;   
;;   `(macroexpand '(exact (+ 2.8 (* 3.7 4.3 5.5) (/ 4.4 5.7)))) ; => (+ 14/5 (* 37/10 43/10 11/2) (/ 22/5 57/10)) (exact (+ 2.8 (* 3.7 4.3 5.5) (/ 4.4 5.7))) ; => 1038277/11400 (exact (* 2452.45 100)) ; => 245245N ; works with let (let [a 100] (exact (* 2452.45 a))) ; => 245245N`
;;   
;;   ![Nikolay](https://www.gravatar.com/avatar/bb45e19fd908a870f49aa0d9ec776262?r=pg&s=96&d=identicon "Nikolay")
;;   
;;   Nikolay on [2020-12-28](/posts/305-clojure-from-the-ground-up-macros#comment-3272)
;;   
;;   This is what I came up with:
;;   
;;   ``(defmacro exact [exp] (let [f (first exp) args (rest exp)] `(~f ~@(map rationalize args))))``
;;   
;;   Not sure if it is correctly written, but it gives the correct results:
;;   
;;   ``(macroexpand `(exact (* 2452.45 100))) ;; (clojure.core/* 49049/20 100)``
;;   
;;   Aphyr on [2020-12-29](/posts/305-clojure-from-the-ground-up-macros#comment-3274)
;;   
;;   For folks wondering about `exact`–your solutions are great! This is a deliberately open-ended problem and there are lots of ways to solve it. Simple approaches may run into edge cases. For instance, they may not work well with variables, with recursively nested expressions, or only recur into some types of collections. A more complete solution requires some functions we haven’t talked about yet, but a little googling or reference to the clojure docs might give you enough. Here’s what I might write:
;;   
;;   ``(def exact-fn? "A set of functions which are treated specially by exact." '#{+ - * /}) (defn exact* "Recursive helper for exact" [expr] (cond ; For seqs, we recursively expand, then look for a pattern like (+ x ; y), where + is one of our exact functions. (seq? expr) (let [[f & args] (map exact* expr)] (cons f (if (exact-fn? f) (map (partial list `rationalize) args) args))) ; For other collections, we recursively expand them into a collection ; of the same type. You can do this more easily with clojure.walk, but ; we haven't talked about that yet. (coll? expr) (into (empty expr) (map exact* expr)) ; We leave non-collection objects unchanged. :else expr)) (defmacro exact "Transforms an expression, converting any use of +, -, *, or / to use ratios rather than (potentially) floating-point numbers." [expr] (exact* expr))``
;;   
;;   This works with non-literal numbers, and recurs into the major Clojure collections as one might expect. It _doesn’t_ work when `+`, `-`, etc. are rebound, but I wouldn’t worry too much about that right now. :-)
;;   
;;   `(let [x 1.1] (exact #{(+ 1.3 (- 7 x))})) ; => #{36/5}`
;;   
;;   ![Aphyr](https://www.gravatar.com/avatar/e145b50faf662e70c066b13c98921900?r=pg&s=96&d=identicon "Aphyr")
;;   
;;   ![Nitpick](https://www.gravatar.com/avatar/294de3557d9d00b3d2d8a1e6aab028cf?r=pg&s=96&d=identicon "Nitpick")
;;   
;;   Nitpick on [2021-04-28](/posts/305-clojure-from-the-ground-up-macros#comment-3344)
;;   
;;   Great post! Really enjoying your series of posts! There is a “sum (viz recur)” in this post, and I guess you meant “sum (via recur)”. Thanks.
;;   
;;   Aphyr on [2021-04-28](/posts/305-clojure-from-the-ground-up-macros#comment-3346)
;;   
;;   Thank you! “Viz” means “in other words”, or “namely”, and is used here to explain that `recur` can be treated as another name for `sum`. Different execution semantics, obviously! :-)
;;   
;;   ![Aphyr](https://www.gravatar.com/avatar/e145b50faf662e70c066b13c98921900?r=pg&s=96&d=identicon "Aphyr")
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
 
  )