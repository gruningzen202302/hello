(ns welcome-to-clojure
  (:require [clojure.repl :refer [source apropos dir pst doc find-doc]]
            [clojure.string :as string]
            [clojure.test :refer [is are]]))

;;   (comment
;;     Clojure from the ground up: state     window.dataLayer = window.dataLayer || \[\]; function gtag(){dataLayer.push(arguments);} gtag('js', new Date()); gtag('config', 'G-MXDP37S6QL');
;;   
;;       
;;   
;;   *   [Aphyr](/)
;;   *   [About](/about)
;;   *   [Blog](/posts)
;;   *   [Photos](/photos)
;;   *   [Code](http://github.com/aphyr)
;;   
;;   [Clojure from the ground up: state](/posts/306-clojure-from-the-ground-up-state)
;;   ================================================================================
;;   
;;   [Software](/tags/software) [Clojure](/tags/clojure) [Clojure from the ground up](/tags/clojure-from-the-ground-up)
;;   
;;   2013-12-01
;;   
;;   _Previously: [Macros](http://aphyr.com/posts/305-clojure-from-the-ground-up-macros)._
;;   
;;   Most programs encompass _change_. People grow up, leave town, fall in love, and take new names. Engines burn through fuel while their parts wear out, and new ones are swapped in. Forests burn down and their logs become nurseries for new trees. Despite these changes, we say “She’s still Nguyen”, “That’s my motorcycle”, “The same woods I hiked through as a child.”
;;   
;;   Identity is a skein we lay across the world of immutable facts; a single entity which encompasses change. In programming, identities unify different values over time. Identity types are _mutable references_ to _immutable values_.
;;   
;;   In this chapter, we’ll move from immutable references to complex concurrent transactions. In the process we’ll get a taste of _concurrency_ and _parallelism_, which will motivate the use of more sophisticated identity types. These are not easy concepts, so don’t get discouraged. You don’t have to understand this chapter fully to be a productive programmer, but I do want to hint at _why_ things work this way. As you work with state more, these concepts will solidify.
;;   
;; *  [Immutability](#immutability)
;;   -----------------------------
;;   
"   The references we’ve used in `let` bindings and function arguments are _immutable_: they never change."
;;   
;;       user=> 
(let [x 1]
                (prn (inc x))
                (prn (inc x)))
;;       2
;;       2
;;       
;;   
;;   The expression `(inc x)` did not _alter_ `x`: `x` remained `1`. The same applies to strings, lists, vectors, maps, sets, and most everything else in Clojure:
;;   
;;       user=> 
(let [x [1 2]]
                (prn (conj x :a))
                (prn (conj x :b)))
;;       [1 2 :a]
;;       [1 2 :b]
;;   
;;   Immutability also extends to `let` bindings, function arguments, and other symbols. Functions _remember_ the values of those symbols at the time the function was constructed.
;;   
;;       (defn present
;;         [gift]
;;         (fn [] gift))
;;       
;;       user=> (def green-box (present "clockwork beetle"))
;;       #'user/green-box
;;       user=> (def red-box (present "plush tiger"))
;;       #'user/red-box
;;       user=> (red-box)
;;       "plush tiger"
;;       user=> (green-box)
;;       "clockwork beetle"
;;       
;;   
;;   The `present` function _creates a new function_. That function takes no arguments, and always returns the gift. Which gift? Because `gift` is not an argument to the inner function, it refers to the value from the _outer function body_. When we packaged up the red and green boxes, the functions we created carried with them a memory of the `gift` symbol’s value.
;;   
;;   This is called _closing over_ the `gift` variable; the inner function is sometimes called _a closure_. In Clojure, new functions close over _all_ variables except their arguments–the arguments, of course, will be provided when the function is invoked.
;;   
;;   [Delays](#delays)
;;   -----------------
;;   
;;   Because functions _close over_ their arguments, they can be used to _defer_ evaluation of expressions. That’s how we introduced functions originally–like `let` expressions, but with a number (maybe zero!) of symbols _missing_, to be filled in at a later time.
;;   
;;       user=> (do (prn "Adding") (+ 1 2))
;;       "Adding"
;;       3
;;       user=> (def later (fn [] (prn "Adding") (+ 1 2)))
;;       #'user/later
;;       user=> (later)
;;       "Adding"
;;       3
;;       
;;   
;;   Evaluating `(def later ...)` did _not_ evaluate the expressions in the function body. Only when we invoked the function `later` did Clojure print `"Adding"` to the screen, and return `3`. This is the basis of _concurrency_: evaluating expressions outside their normal, sequential order.
;;   
;;   This pattern of deferring evaluation is so common that there’s a standard macro for it, called `delay`:
;;   
;;       user=> (def later (delay (prn "Adding") (+ 1 2)))
;;       #'user/later
;;       user=> later
;;       #<Delay@2dd31aac: :pending>
;;       user=> (deref later)
;;       "Adding"
;;       3
;;       
;;   
;;   Instead of a function, `delay` creates a special type of Delay object: an identity which _refers_ to expressions which should be evaluated later. We extract, or _dereference_, the value of that identity with `deref`. Delays follow the same rules as functions, closing over lexical scope–because `delay` actually macroexpands into an anonymous function.
;;   
;;       user=> (source delay)
;;       (defmacro delay
;;         "Takes a body of expressions and yields a Delay object that will
;;         invoke the body only the first time it is forced (with force or deref/@), and
;;         will cache the result and return it on all subsequent force
;;         calls. See also - realized?"
;;         {:added "1.0"}
;;         [& body]
;;           (list 'new 'clojure.lang.Delay (list* `^{:once true} fn* [] body)))
;;       
;;   
;;   Why the `Delay` object instead of a plain old function? Because unlike function invocation, delays only evaluate their expressions _once_. They remember their value, after the first evaluation, and return it for every successive `deref`.
;;   
;;       user=> (deref later)
;;       3
;;       user=> (deref later)
;;       3
;;       
;;   
;;   By the way, there’s a shortcut for `(deref something)`: the wormhole operator `@`:
;;   
;;       user=> @later ; Interpreted as (deref later)
;;       3
;;       
;;   
;;   Remember how `map` returned a sequence immediately, but didn’t actually perform any computation until we asked for elements? That’s called _lazy_ evaluation. Because delays are lazy, we can avoid doing expensive operations until they’re really needed. Like an IOU, we use delays when we aren’t ready to do something just yet, but when someone calls in the favor, we’ll make sure it happens.
;;   
;;   [Futures](#futures)
;;   -------------------
;;   
;;   What if we wanted to _opportunistically_ defer computation? Modern computers have multiple cores, and operating systems let us share a core between two tasks. It would be great if we could use that multitasking ability to say, “I don’t need the result of evaluating these expressions _yet_, but I’d like it _later_. Could you start working on it in the meantime?”
;;   
;;   Enter the _future_: a delay which is evaluated _in parallel_. Like delays, futures return immediately, and give us an _identity_ which will point to the value of the last expression in the future–in this case, the value of `(+ 1 2)`.
;;   
;;       user=> (def x (future (prn "hi") (+ 1 2)))
;;       "hi"
;;       #'user/x
;;       user=> (deref x)
;;       3
;;       
;;   
;;   Notice how the future printed “hi” right away. That’s because futures are evaluated in a new _thread_. On multicore computers, two threads can run in _parallel_, on different cores the same time. When there are more threads than cores, the cores _trade off_ running different threads. Both parallel and non-parallel evaluation of threads are _concurrent_ because expressions from different threads can be evaluated out of order.
;;   
;;       user=> (dotimes [i 5] (future (prn i)))
;;       14
;;       
;;       3
;;       0
;;       2
;;       nil
;;       
;;   
;;   Five threads running at once. Notice that the thread printing `1` didn’t even get to move to a new line before `4` showed up–then both threads wrote new lines at the same time. There are techniques to control this concurrent execution so that things happen in some well-defined sequence, like agents and locks, but we’ll discuss those later.
;;   
;;   Just like delays, we can deref a future as many times as we want, and the expressions are only evaluated once.
;;   
;;       user=> (def x (future (prn "hi") (+ 1 2)))
;;       #'user/x"hi"
;;       
;;       user=> @x
;;       3
;;       user=> @x
;;       3
;;       
;;   
;;   Futures are the most generic parallel construct in Clojure. You can use futures to do CPU-intensive computation faster, to wait for multiple network requests to complete at once, or to run housekeeping code periodically.
;;   
;;   [Promises](#promises)
;;   ---------------------
;;   
;;   Delays _defer_ evaluation, and futures _parallelize_ it. What if we wanted to defer something we _don’t even have yet_? To hand someone an empty box and, later, before they open it, sneak in and replacing its contents with an actual gift? Surely I’m not the only one who does birthday presents this way.
;;   
;;       user=> (def box (promise))
;;       #'user/box
;;       user=> box
;;       #<core$promise$reify__6310@1d7762e: :pending>
;;       
;;   
;;   This box is _pending_ a value. Like futures and delays, if we try to open it, we’ll get _stuck_ and have to wait for something to appear inside:
;;   
;;       user=> (deref box)
;;       
;;   
;;   But unlike futures and delays, this box won’t be filled automatically. Hold the `Control` key and hit `c` to give up on trying to open that package. Nobody else is in this REPL, so we’ll have to buy our own presents.
;;   
;;       user=> (deliver box :live-scorpions!)
;;       #<core$promise$reify__6310@1d7762e: :live-scorpions!>
;;       user=> (deref box)
;;       :live-scorpions!
;;       
;;   
;;   Wow, that’s a _terrible_ gift. But at least there’s something there: when we dereference the box, it opens immediately and live scorpions skitter out. Can we get a do-over? Let’s try a nicer gift.
;;   
;;       user=> (deliver box :puppy)
;;       nil
;;       user=> (deref box)
;;       :live-scorpions!
;;       
;;   
;;   Like delays and futures, there’s no going back on our promises. Once delivered, a promise _always_ refers to the same value. This is a simple identity type: we can set it to a value once, and read it as many times as we want. `promise` is also a _concurrency primitive_: it guarantees that any attempt to read the value will _wait_ until the value has been written. We can use promises to _synchronize_ a program which is being evaluated concurrently–for instance, this simple card game:
;;   
;;       user=> (def card (promise))
;;       #'user/card
;;       user=> (def dealer (future 
;;                            (Thread/sleep 5000)
;;                            (deliver card [(inc (rand-int 13))
;;                                           (rand-nth [:clubs :spades :hearts :diamonds])])))
;;       #'user/dealer
;;       user=> (deref card)
;;       [5 :diamonds]
;;       
;;   
;;   In this program, we set up a `dealer` thread which waits for five seconds (5000 milliseconds), then delivers a random card. While the dealer is sleeping, we try to deref our card–and have to wait until the five seconds are up. Synchronization and identity in one package.
;;   
;;   Where delays are lazy, and futures are parallel, promises are concurrent _without specifying how the evaluation occurs_. We control exactly when and how the value is delivered. You can think of both delays and futures as being built atop promises, in a way.
;;   
;;   [Vars](#vars)
;;   -------------
;;   
;;   So far the identities we’ve discussed have referred (eventually) to a _single_ value, but the real world needs names that refer to _different_ values at different points in time. For this, we use _vars_.
;;   
;;   We’ve touched on vars before–they’re transparent mutable references. Each var has a value associated with it, and that value can change over time. When a var is evaluated, it is replaced by its _present_ value transparently–everywhere in the program.
;;   
;;       user=> (def x :mouse)
;;       #'user/x
;;       user=> (def box (fn [] x))
;;       #'user/box
;;       user=> (box)
;;       :mouse
;;       user=> (def x :cat)
;;       #'user/x
;;       user=> (box)
;;       :cat
;;       
;;   
;;   The `box` function closed over `x`–but calling `(box)` returned _different_ results depending on the current value of `x`. Even though the _var_ `x` remained unchanged throughout this example, the _value associated with that var_ did change!
;;   
;;   Using mutable vars allows us to write programs which we can redefine as we go along.
;;   
;;       user=> (defn decouple [glider]
;;         #_=>   (prn "bolts released"))
;;       #'user/decouple
;;       user=> (defn launch [glider]
;;         #_=>   (decouple glider)
;;         #_=>   (prn glider "away!"))
;;       #'user/launch
;;       user=> (launch "albatross")
;;       "bolts released"
;;       "albatross" "away!"
;;       nil
;;       
;;       user=> (defn decouple [glider]
;;         #_=>   (prn "tether released"))
;;       #'user/decouple
;;       user=> (launch "albatross")
;;       "tether released"
;;       "albatross" "away!"
;;       
;;   
;;   A reference which is the same everywhere is called a _global variable_, or simply a _global_. But vars have an additional trick up their sleeve: with a _dynamic_ var, we can override their value only within the scope of a particular function call, and nowhere else.
;;   
;;       user=> (def ^:dynamic *board* :maple)
;;       #'user/*board*
;;       
;;   
;;   `^:dynamic` tells Clojure that this var can be overridden in one particular scope. By convention, dynamic variables are named with asterisks around them–this reminds us, as programmers, that they are likely to change. Next, we define a function that uses that dynamic var:
;;   
;;       user=> (defn cut [] (prn "sawing through" *board*))
;;       #'user/cut
;;       
;;   
;;   Note that `cut` closes over the var `*board*`, but not the _value_ :maple. Every time the function is invoked, it looks up the _current_ value of `*board*`.
;;   
;;       user=> (cut)
;;       "sawing through" :maple
;;       nil
;;       user=> (binding [*board* :cedar] (cut))
;;       "sawing through" :cedar
;;       nil
;;       user=> (cut)
;;       "sawing through" :maple
;;       
;;   
;;   Like `let`, the `binding` macro assigns a value to a name–but where `fn` and `let` create immutable _lexical scope_, `binding` creates _dynamic scope_. The difference? Lexical scope is constrained to the literal text of the `fn` or `let` expression–but dynamic scope propagates _through function calls_.
;;   
;;   Within the `binding` expression, and in every function called from that expression, and every function called from _those_ functions, and so on, `*board*` has the value `:cedar`. Outside the `binding` expression, the value is still `:maple`. This safety property holds even when the program is executed in multiple threads: only the thread which evaluated the `binding` expression uses that value. Other threads are unaffected.
;;   
;;   While we use `def` all the time in the REPL, in real programs you should only mutate vars sparingly. They’re intended for naming functions, important bits of global data, and for tracking the _environment_ of a program–like where to print messages with `prn`, which database to talk to, and so on. Using vars for mutable program state is a recipe for disaster, as we’re about to see.
;;   
;;   [Atoms](#atoms)
;;   ---------------
;;   
;;   Vars can be read, set, and dynamically bound–but they aren’t easy to _evolve_. Imagine building up a set of integers:
;;   
;;       user=> (def xs #{})
;;       #'user/xs
;;       user=> (dotimes [i 10] (def xs (conj xs i)))
;;       user=> xs
;;       #{0 1 2 3 4 5 6 7 8 9}
;;       
;;   
;;   For each number from 0 to 9, we take the current set of numbers `xs`, add a particular number `i` to that set, and redefine `xs` as the result. This is a common idiom in imperative language like C, Ruby, Javascript, or Java–all variables are mutable by default.
;;   
;;       ImmutableSet xs = new ImmutableSet();
;;       for (int i = 0; i++; i < 10) {
;;         xs = xs.add(i);
;;       }
;;       
;;   
;;   It seems straightforward enough, but there are serious problems lurking here. Specifically, this program is not _thread safe_.
;;   
;;       user=> (def xs #{})
;;       user=> (dotimes [i 10] (future (def xs (conj xs i))))
;;       #'user/xs
;;       nil
;;       user=> xs
;;       #{1 4 5 7}
;;       
;;   
;;   This program runs 10 threads in parallel, and each reads the current value of `xs`, adds its particular number, and defines `xs` to be that new set of numbers. This read-modify-update process assumed that all updates would be _consecutive_–not _concurrent_. When we allowed the program to do two read-modify-updates at the same time, updates were lost.
;;   
;;   1.  Thread 2 read `#{0 1}`
;;   2.  Thread 3 read `#{0 1}`
;;   3.  Thread 2 wrote `#{0 1 2}`
;;   4.  Thread 3 wrote `#{0 1 3}`
;;   
;;   This interleaving of operations allowed the number `2` to slip through the cracks. We need something stronger–an identity which supports safe transformation from one state to another. Enter atoms.
;;   
;;       user=> (def xs (atom #{}))
;;       #'user/xs
;;       user=> xs
;;       #<Atom@30bb8cc9: #{}>
;;       
;;   
;;   The initial value of this atom is `#{}`. Unlike vars, atoms are not transparent. When evaluated, they don’t return their underlying values–but notice that when printed, the current value is hiding inside. To get the current value out of an atom, we have to use `deref` or `@`.
;;   
;;       user=> (deref xs)
;;       #{}
;;       user=> @xs
;;       #{}
;;       
;;   
;;   Like vars, atoms can be set to a particular value–but instead of `def`, we use `reset!`. The exclamation point (sometimes called a _bang_) is there to remind us that this function _modifies_ the state of its arguments–in this case, changing the value of the atom.
;;   
;;       user=> (reset! xs :foo)
;;       :foo
;;       user=> xs
;;       #<Atom@30bb8cc9: :foo>
;;       
;;   
;;   Unlike vars, atoms can be safely _updated_ using `swap!`. `swap!` uses a pure function which takes the current value of the atom and returns a _new_ value. Under the hood, Clojure does some tricks to ensure that these updates are _linearizable_, which means:
;;   
;;   1.  All updates with \`swap! complete in what _appears_ to be a single consecutive order.
;;   2.  The effect of a swap! never takes place before calling `swap!`.
;;   3.  The effect of a swap! is visible to everyone once swap! returns.
;;   
;;       user=> (def x (atom 0))
;;       #'user/x
;;       user=> (swap! x inc)
;;       1
;;       user=> (swap! x inc)
;;       2
;;       
;;   
;;   The first `swap!` reads the value `0`, calls `(inc 0)` to obtain `1`, and writes `1` back to the atom. Each call to `swap!` returns the value that was just written.
;;   
;;   We can pass additional arguments to the function `swap!` calls. For instance, `(swap! x + 5 6)` will call `(+ x 5 6)` to find the new value. Now we have the tools to correct our parallel program from earlier:
;;   
;;       user=> (def xs (atom #{}))
;;       #'user/xs
;;       user=> (dotimes [i 10] (future (swap! xs conj i)))
;;       nil
;;       user=> @xs
;;       #{0 1 2 3 4 5 6 7 8 9}
;;       
;;   
;;   Note that the function we use to update an atom must be _pure_–must not mutate any state–because when resolving conflicts between multiple threads, Clojure might need to call the update function more than once. Clojure’s reliance on immutable datatypes, immutable variables, and pure functions _enables_ this approach to linearizable mutability. Languages which emphasize mutable datatypes need to use other constructs.
;;   
;;   Atoms are the workhorse of Clojure state. They’re lightweight, safe, fast, and flexible. You can use atoms with any immutable datatype–for instance, a map to track complex state. Reach for an atom whenever you want to update a single thing over time.
;;   
;;   [Refs](#refs)
;;   -------------
;;   
;;   Atoms are a great way to represent state, but they are only linearizable _individually_. Updates to an atom aren’t well-ordered with respect to other atoms, so if we try to update more than one atom at once, we could see the same kinds of bugs that we did with vars.
;;   
;;   For multi-identity updates, we need a stronger safety property than single-atom linearizability. We want _serializability_: a global order. For this, Clojure has an identity type called a _Ref_.
;;   
;;       user=> (def x (ref 0))
;;       #'user/x
;;       user=> x
;;       #<Ref@1835d850: 0>
;;       
;;   
;;   Like all identity types, refs are dereferencable:
;;   
;;       user=> @x
;;       0
;;       
;;   
;;   But where atoms are updated individually with `swap!`, refs are updated in _groups_ using `dosync` transactions. Just as we `reset!` an atom, we can set refs to new values using `ref-set`–but unlike atoms, we can change more than one ref at once.
;;   
;;       user=> (def x (ref 0))
;;       user=> (def y (ref 0))
;;       user=> (dosync
;;                (ref-set x 1)
;;                (ref-set y 2))
;;       2
;;       user=> [@x @y]
;;       [1 2]
;;       
;;   
;;   The equivalent of `swap!`, for a ref, is `alter`:
;;   
;;       user=> (def x (ref 1))
;;       user=> (def y (ref 2))
;;       user=> (dosync
;;                (alter x + 2)
;;                (alter y inc))
;;       3
;;       user=> [@x @y]
;;       [3 3]
;;       
;;   
;;   All `alter` operations within a `dosync` take place atomically–their effects are never interleaved with other transactions. If it’s OK for an operation to take place out of order, you can use `commute` instead of `alter` for a performance boost:
;;   
;;       user=> (dosync
;;                (commute x + 2)
;;                (commute y inc))
;;       
;;   
;;   These updates are _not_ guaranteed to take place in the same order–but if all our transactions are equivalent, we can _relax_ the ordering constraints. x + 2 + 3 is equal to x + 3 + 2, so we can do the additions in either order. That’s what _commutative_ means: the same result from all orders. It’s a weaker, but faster kind of safety property.
;;   
;;   Finally, if you want to read a value from one ref and use it to update another, use `ensure` instead of `deref` to perform a _strongly consistent read_–one which is guaranteed to take place in the same logical order as the `dosync` transaction itself. To add `y`’s current value to `x`, use:
;;   
;;       user=> (dosync
;;                (alter x + (ensure y)))
;;       
;;   
;;   Refs are a powerful construct, and make it easier to write complex transactional logic safely. However, that safety comes at a cost: refs are typically an order of magnitude slower to update than atoms.
;;   
;;   Use refs only where you need to update multiple pieces of state independently–specifically, where different transactions need to work with distinct but _partly overlapping_ pieces of state. If there’s no overlap between updates, use distinct atoms. If all operations update the same identities, use a single atom to hold a map of the system’s state. If a system requires complex interlocking state spread throughput the program–that’s when to reach for refs.
;;   
;;   [Summary](#summary)
;;   -------------------
;;   
;;   We moved beyond immutable programs into the world of _changing state_–and discovered the challenges of concurrency and parallelism. Where symbols provide immutable and transparent names for values objects, Vars provide _mutable_ transparent names. We also saw a host of anonymous identity types for different purposes: delays for lazy evaluation, futures for parallel evaluation, and promises for arbitrary handoff of a value. Updates to vars are unsafe, so atoms and refs provide linearizable and serializable identities where transformations are _safe_.
;;   
;;   Where reading a symbol or var is _transparent_–they evaluate directly to their current values–reading these new identity types requires the use of `deref`. Delays, futures, and promises _block_: deref must wait until the value is ready. This allows synchronization of concurrent threads. Atoms and refs, by contrast, can be read immediately at any time–but _updating_ their values should occur within a `swap!` or `dosync` transaction, respectively.
;;   
;;   Type
;;   
;;   Mutability
;;   
;;   Reads
;;   
;;   Updates
;;   
;;   Evaluation
;;   
;;   Scope
;;   
;;   Symbol
;;   
;;   Immutable
;;   
;;   Transparent
;;   
;;   Lexical
;;   
;;   Var
;;   
;;   Mutable
;;   
;;   Transparent
;;   
;;   Unrestricted
;;   
;;   Global/Dynamic
;;   
;;   Delay
;;   
;;   Mutable
;;   
;;   Blocking
;;   
;;   Once only
;;   
;;   Lazy
;;   
;;   Future
;;   
;;   Mutable
;;   
;;   Blocking
;;   
;;   Once only
;;   
;;   Parallel
;;   
;;   Promise
;;   
;;   Mutable
;;   
;;   Blocking
;;   
;;   Once only
;;   
;;   Atom
;;   
;;   Mutable
;;   
;;   Nonblocking
;;   
;;   Linearizable
;;   
;;   Ref
;;   
;;   Mutable
;;   
;;   Nonblocking
;;   
;;   Serializable
;;   
;;   State is undoubtedly the hardest part of programming, and this chapter probably felt overwhelming! On the other hand, we’re now equipped to solve serious problems. We’ll take a break to apply what we’ve learned through practical examples, in Chapter Seven: [Logistics](http://aphyr.com/posts/311-clojure-from-the-ground-up-logistics).
;;   
;;   [Exercises](#exercises)
;;   -----------------------
;;   
;;   Finding the sum of the first 10000000 numbers takes about 1 second on my machine:
;;   
;;       user=> (defn sum [start end] (reduce + (range start end)))
;;       user=> (time (sum 0 1e7))
;;       "Elapsed time: 1001.295323 msecs"
;;       49999995000000
;;       
;;   
;;   1.  Use `delay` to compute this sum lazily; show that it takes no time to return the delay, but roughly 1 second to `deref`.
;;       
;;   2.  We can do the computation in a new thread directly, using `(.start (Thread. (fn [] (sum 0 1e7)))`–but this simply runs the `(sum)` function and discards the results. Use a promise to hand the result back out of the thread. Use this technique to write your own version of the `future` macro.
;;       
;;   3.  If your computer has two cores, you can do this expensive computation twice as fast by splitting it into two parts: `(sum 0 (/ 1e7 2))`, and `(sum (/ 1e7 2) 1e7)`, then adding those parts together. Use `future` to do both parts at once, and show that this strategy gets the same answer as the single-threaded version, but takes roughly half the time.
;;       
;;   4.  Instead of using `reduce`, store the sum in an atom and use two futures to add each number from the lower and upper range to that atom. Wait for both futures to complete using `deref`, then check that the atom contains the right number. Is this technique faster or slower than `reduce`? Why do you think that might be?
;;       
;;   5.  Instead of using a lazy list, imagine two threads are removing tasks from a pile of work. Our work pile will be the list of all integers from 0 to 10000:
;;       
;;           user=> (def work (ref (apply list (range 1e5))))
;;           user=> (take 10 @work)
;;           (0 1 2 3 4 5 6 7 8 9)
;;           
;;       
;;       And the sum will be a ref as well:
;;       
;;           user=> (def sum (ref 0))
;;           
;;       
;;       Write a function which, in a `dosync` transaction, removes the first number in `work` and adds it to `sum`.  
;;       Then, in two futures, call that function over and over again until there’s no work left. Verify that `@sum` is `4999950000`. Experiment with different combinations of `alter` and `commute`–if both are correct, is one faster? Does using `deref` instead of `ensure` change the result?
;;       
;;   
;;   ![Edward Cho](https://www.gravatar.com/avatar/946aeb5ce5f353e02b51ac863ec0c1d4?r=pg&s=96&d=identicon "Edward Cho")
;;   
;;   Edward Cho on [2014-01-01](/posts/306-clojure-from-the-ground-up-state#comment-1798)
;;   
;;   Kudos to your effort, this is great work!
;;   
;;   I just have a minor correction in your dynamic vars example. The binding call should rebind _board_ instead of board. E.g.
;;   
;;   (cut)
;;   
;;   (binding _board_ :cedar)
;;   
;;   (cut)
;;   
;;   ![Edward Cho](https://www.gravatar.com/avatar/946aeb5ce5f353e02b51ac863ec0c1d4?r=pg&s=96&d=identicon "Edward Cho")
;;   
;;   Edward Cho on [2014-01-01](/posts/306-clojure-from-the-ground-up-state#comment-1799)
;;   
;;   Doh, Markdown? That should be:
;;   
;;   `(cut) (binding [*board* :cedar] (cut)) (cut)`
;;   
;;   ![Andy Dwelly](https://www.gravatar.com/avatar/722a6812714c20da3f477ec9fb32180a?r=pg&s=96&d=identicon "Andy Dwelly")
;;   
;;   Andy Dwelly on [2014-01-02](/posts/306-clojure-from-the-ground-up-state#comment-1800)
;;   
;;   This is great. I hope you’ll consider publishing this set of posts as a book when you are done (ideally a kindle one AFAIC). I’d buy it - it’s far more readable and to the point than other beginner guides.
;;   
;;   Aphyr on [2014-01-02](/posts/306-clojure-from-the-ground-up-state#comment-1801)
;;   
;;   Thanks for the correction, Edward. Fixed! :)
;;   
;;   ![Aphyr](https://www.gravatar.com/avatar/e145b50faf662e70c066b13c98921900?r=pg&s=96&d=identicon "Aphyr")
;;   
;;   ![Saad Mufti](https://www.gravatar.com/avatar/ec93f8f40f6bddd88ea08d90a4ef07bf?r=pg&s=96&d=identicon "Saad Mufti")
;;   
;;   Saad Mufti on [2014-01-03](/posts/306-clojure-from-the-ground-up-state#comment-1802)
;;   
;;   Great series, I’m learning by leaps and bounds, and eagerly anticipating each chapter :-) A small correction, in your exercises section, you have:
;;   
;;   “We can do the computation in a new thread directly, using (Thread. (fn )–but this simply runs the (sum-up) function and discards the results.”
;;   
;;   First a minor point, the name of the function you defined at the top of the Exercises section is actually “sum” and not “sump-up”. Second, I don’t think this actually even runs anything, it just creates the java.lang.Thread object and passes in the Clojure function as its runnable, because all Clojure functions at the Java level implement the java.lang.Runnable interface. I think you need the following to actually run it, though of course you still need the solution to your exercise using “promise” to actually get a reference to the result:
;;   
;;   (.start (Thread. (fn ))
;;   
;;   ![Saad Mufti](https://www.gravatar.com/avatar/ec93f8f40f6bddd88ea08d90a4ef07bf?r=pg&s=96&d=identicon "Saad Mufti")
;;   
;;   Saad Mufti on [2014-01-03](/posts/306-clojure-from-the-ground-up-state#comment-1803)
;;   
;;   Yikes, bitten by the markup gods, let me try that again:
;;   
;;   Great series, I’m learning by leaps and bounds, and eagerly anticipating each chapter :-) A small correction, in your exercises section, you have:
;;   
;;   “We can do the computation in a new thread directly, using (Thread. (fn )
;;   
;;   –but this simply runs the (sum-up) function and discards the results.”
;;   
;;   First a minor point, the name of the function you defined at the top of the Exercises section is actually “sum” and not “sump-up”. Second, I don’t think this actually even runs anything, it just creates the java.lang.Thread object and passes in the Clojure function as its runnable, because all Clojure functions at the Java level implement the java.lang.Runnable interface. I think you need the following to actually run it, though of course you still need the solution to your exercise using “promise” to actually get a reference to the result:
;;   
;;   `(.start (Thread. (fn [] (sum-up 0 1e7)))`
;;   
;;   Aphyr on [2014-01-06](/posts/306-clojure-from-the-ground-up-state#comment-1807)
;;   
;;   Correct on both counts, Saad. Fixed. :)
;;   
;;   ![Aphyr](https://www.gravatar.com/avatar/e145b50faf662e70c066b13c98921900?r=pg&s=96&d=identicon "Aphyr")
;;   
;;   ![Dan Haffey](https://www.gravatar.com/avatar/9012e1f859fd9ebe0650cd94dcf3249a?r=pg&s=96&d=identicon "Dan Haffey")
;;   
;;   Dan Haffey on [2014-01-10](/posts/306-clojure-from-the-ground-up-state#comment-1808)
;;   
;;   Your description of `ensure` doesn’t quite match my understanding of it as an optimized version of `(ref-set y @y)`. You wrote:
;;   
;;   > if you want to read a value from one ref and use it to update another, use ensure instead of deref to perform a strongly consistent read
;;   
;;   Could you elaborate on why `deref` doesn’t suffice in `(alter x + (ensure y))`? Sure, `deref` means another transaction might modify `y` before we commit, but wouldn’t both `deref` and `ensure` return the value of `y` from our snapshot regardless? I thought it would take some additional semantic constraint relating `x` and `y` to necessitate `ensure` here. Just defensive programming, or am I missing something?
;;   
;;   ![Andreas Olsson](https://www.gravatar.com/avatar/294de3557d9d00b3d2d8a1e6aab028cf?r=pg&s=96&d=identicon "Andreas Olsson")
;;   
;;   Andreas Olsson on [2014-01-14](/posts/306-clojure-from-the-ground-up-state#comment-1810)
;;   
;;   Hello there.
;;   
;;   I´m trying to learn some and I like your tutorial. I have tryed changing som of youre code and got into problems.
;;   
;;   this code works with #{} but not with \[\]… why?
;;   
;;   > (def sx(atom \[\]))
;;   
;;   (dotimes i 10))
;;   
;;   If you dont try you never learn… =)
;;   
;;   ![Andreas Olsson](https://www.gravatar.com/avatar/242eb6bfde0f58b0943cd8c1ae068e00?r=pg&s=96&d=identicon "Andreas Olsson")
;;   
;;   Andreas Olsson on [2014-01-14](/posts/306-clojure-from-the-ground-up-state#comment-1812)
;;   
;;   The Code dissapers. Sorry. (def sx(atom \[1 2\])) (dotimes i 10)).
;;   
;;   ![pseudoanonymous](https://www.gravatar.com/avatar/294de3557d9d00b3d2d8a1e6aab028cf?r=pg&s=96&d=identicon "pseudoanonymous")
;;   
;;   pseudoanonymous on [2014-02-16](/posts/306-clojure-from-the-ground-up-state#comment-1820)
;;   
;;   Thanks for writing this, it is really accessible.
;;   
;;   Will you have a concurrency chapter? I’m assuming that’s when you’ll talk about agents.
;;   
;;   I’m really looking forward to reading the next installment.
;;   
;;   ![tiensonqin](https://www.gravatar.com/avatar/ee8c87b63359c999698970acc89148ea?r=pg&s=96&d=identicon "tiensonqin")
;;   
;;   tiensonqin on [2014-03-13](/posts/306-clojure-from-the-ground-up-state#comment-1843)
;;   
;;   This series is so helpful, very great work!
;;   
;;   ![Luke Worth](https://www.gravatar.com/avatar/4c46bf42871b485af2f44f03023d99f3?r=pg&s=96&d=identicon "Luke Worth")
;;   
;;   Luke Worth on [2014-10-09](/posts/306-clojure-from-the-ground-up-state#comment-1962)
;;   
;;   I’m trying to figure out exercise 3. Currently I have:
;;   
;;   `(time (let [a (future (sum 0 (/ 1e8 2))) b (future (sum (/ 1e8 2) 1e8))] (+ @a @b)))`
;;   
;;   but this has roughly the same runtime as just
;;   
;;   `(time (let [a (sum 0 (/ 1e8 2)) b (sum (/ 1e8 2) 1e8)] (+ a b)))`
;;   
;;   What have I done wrong?
;;   
;;   ![Luke Worth](https://www.gravatar.com/avatar/4c46bf42871b485af2f44f03023d99f3?r=pg&s=96&d=identicon "Luke Worth")
;;   
;;   Luke Worth on [2014-10-10](/posts/306-clojure-from-the-ground-up-state#comment-1963)
;;   
;;   In reply to myself, it seems that upgrading from Java 6 (pre-installed on my mac) to Java 8 (from Oracle) has improved the speed of the multi-threaded version.
;;   
;;   ![Kenneth R. Beesley](https://www.gravatar.com/avatar/acce31a5852d552b2dba38c4cda7b811?r=pg&s=96&d=identicon "Kenneth R. Beesley")
;;   
;;   Kenneth R. Beesley on [2015-08-11](/posts/306-clojure-from-the-ground-up-state#comment-2480)
;;   
;;   Let me first repeat that you explain Clojure with remarkable skill and readability. You should definitely write a book. On one little point, the following example might be a bit misleading:
;;   
;;   (def xs (atom #{}))
;;   
;;   (dotimes i 10))
;;   
;;   The output is shown as: #{0 1 2 3 4 5 6 7 8 9}, which might imply that order is maintained. Earlier in the presentation, it is stated that “All updates with swap! complete in what appears to be a single consecutive order.” However, sets being sets, the order of elements is insignificant, and the output may appear different when printed.
;;   
;;   ![Kenneth R. Beesley](https://www.gravatar.com/avatar/acce31a5852d552b2dba38c4cda7b811?r=pg&s=96&d=identicon "Kenneth R. Beesley")
;;   
;;   Kenneth R. Beesley on [2015-08-11](/posts/306-clojure-from-the-ground-up-state#comment-2481)
;;   
;;   Oops. Code disappears. Let me try again with a marked up Clojure code block. First try:
;;   
;;   `(def xs (atom #{})) (dotimes [i 5] (future (swap! xs conj i))) @xs`
;;   
;;   The output is shown as
;;   
;;   `#{0 1 2 3 4 5 6 7 8 9}`
;;   
;;   which might imply that the order is preserved.
;;   
;;   ![Kenneth R. Beesley](https://www.gravatar.com/avatar/acce31a5852d552b2dba38c4cda7b811?r=pg&s=96&d=identicon "Kenneth R. Beesley")
;;   
;;   Kenneth R. Beesley on [2015-08-11](/posts/306-clojure-from-the-ground-up-state#comment-2482)
;;   
;;   On the issue of swap!, when the indicated function’s operation is not commutative, it might be useful to point out that the old value of the atom is used as the _first_ argument to the function. E.g. in the example
;;   
;;   `(def x (atom 1)) (swap! x + 1 2)`
;;   
;;   it doesn’t matter if the old value of x is used as the first or last argument to + because + is commutative. But in
;;   
;;   `(def x (atom 4)) (swap! x - 1 2)`
;;   
;;   using the - function/operation, the old value of x is definitely used as the _first_ argument to 1, and the resulting value is of x is 1, e.g.
;;   
;;   `(swap! x - <oldvalueofx> 1 2)`
;;   
;;   ![Kenneth R. Beesley](https://www.gravatar.com/avatar/acce31a5852d552b2dba38c4cda7b811?r=pg&s=96&d=identicon "Kenneth R. Beesley")
;;   
;;   Kenneth R. Beesley on [2015-08-12](/posts/306-clojure-from-the-ground-up-state#comment-2483)
;;   
;;   Trying to sort out delay, future and promise, I came up with the following summary. Please correct as necessary.
;;   
;;   delay The delay definition specifies code to set/bind the identity, but it doesn’t execute that code. When the delay is dereferenced, only then is the code is executed, and the dereference will effectively need to wait/hang until that code is finished. The result of the dereference is cashed, so if the identity is dereferenced again, the previously computed result is simply looked up and returned.
;;   
;;   future The future definition specifies code to set/bind the identity, and it creates a thread and immediately starts executing the code in that thread. When the identity is dereferenced, the execution will have to wait/hang if and only if the code launched in the thread has not yet terminated.
;;   
;;   promise The promise definition does NOT specify code to set/bind the identity; it simply creates an “empty” promise, with the assumption that some other code somewhere/somehow will ‘deliver’ a value to that identity. The deference of the promise identity will have to wait/hang if and only if a value has not yet been ‘deliver’-ed to the promise.
;;   
;;   ![new clojurian](https://www.gravatar.com/avatar/294de3557d9d00b3d2d8a1e6aab028cf?r=pg&s=96&d=identicon "new clojurian")
;;   
;;   new clojurian on [2017-12-08](/posts/306-clojure-from-the-ground-up-state#comment-2920)
;;   
;;   My answers for the questions 2, 3 and 4 :
;;   
;;   1.  `(defmacro my-future [& args] ( let [p# promise] ((.start (Thread. (fn [] (do ~args)))) p#)))`
;;   
;;   3\. `(time (let [a (future (sum 0 (/ 1e7 2))) b (sum (/ 1e7 2) 1e7)] (+ @a b)))`
;;   
;;   4\. `(defn my-sum [start end] ( let [a (atom 0)] (let [b (future (doseq [i (range start (/ end 2))] (swap! a + i))) c (future (doseq [i (range (/ end 2) end)] (swap! a + i)))] @b @c) @a))`\`
;;   
;;   How can i improve them ?
;;   
;;   Thx.
;;   
;;   ![Mariano Mollo](https://www.gravatar.com/avatar/69d5391257453ddaeb2ca6557ad9439d?r=pg&s=96&d=identicon "Mariano Mollo")
;;   
;;   [Mariano Mollo](https://marianomollo.me) on [2020-08-21](/posts/306-clojure-from-the-ground-up-state#comment-3190)
;;   
;;   Hi new clojurian, it seems that inside the let, you should call the promise doing `(promise)`, otherwise p# is linked to the function named promise itself and is not actually a promise.
;;   
;;   Also, p# returns an empty promise because the thread didn’t deliver the obtained value to the promise. I tried my best and came up with this:
;;   
;;   ``(defmacro my-future [expr] `(let [p# (promise)] (.start (Thread. (fn [] (deliver p# ~expr)))) p#))``
;;   
;;   I played around a lot with macroexpand to get the desired result.
;;   
;;   ![Elias Vakkuri](https://www.gravatar.com/avatar/4e05cdd796d05953e23ff15a2e1a85b7?r=pg&s=96&d=identicon "Elias Vakkuri")
;;   
;;   Elias Vakkuri on [2021-07-28](/posts/306-clojure-from-the-ground-up-state#comment-3502)
;;   
;;   I’d like to join in and thank you for this great content!
;;   
;;   A question on exercise 5: how should I be using ensure? I have a sum function that checks whether the work queue has any items in it and pops the first one in a `when-let` block. However, when I try to run this, I get an error: `Execution error (IllegalStateException) ... No transaction running`. I guess then the ensure should be in an `alter` expression, but how should I then check if the queue has any items in it?
;;   
;;   Thanks for any help!
;;   
;;   Code:
;;   
;;       (defn ref-sum
;;         [work-ref sum-ref]
;;         (when-let [n (first (ensure work-ref))]
;;           (dosync
;;            (alter sum-ref + n)
;;            (alter work-ref rest))
;;           (recur work-ref sum-ref)))
;;       
;;       (defn ref-sum-multi
;;         []
;;         (let [work (ref (apply list (range 1e5)))
;;               sum-ref (ref 0)
;;               a (future (ref-sum work sum-ref))
;;               b (future (ref-sum work sum-ref))]
;;           (deref a)
;;           (deref b)
;;           @sum-ref))
;;       
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
;;     5

)