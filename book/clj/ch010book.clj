(ns welcome-to-clojure
  (:require [clojure.repl :refer [source apropos dir pst doc find-doc]]
            [clojure.string :as string]
            [clojure.test :refer [is are]]))

(comment
;;    Clojure from the ground up: polymorphism     window.dataLayer = window.dataLayer || \[\]; function gtag(){dataLayer.push(arguments);} gtag('js', new Date()); gtag('config', 'G-MXDP37S6QL');
;;  
;;      
;;  
;;  *   [Aphyr](/)
;;  *   [About](/about)
;;  *   [Blog](/posts)
;;  *   [Photos](/photos)
;;  *   [Code](http://github.com/aphyr)
;;  
;;  [Clojure from the ground up: polymorphism](/posts/352-clojure-from-the-ground-up-polymorphism)
;;  ==============================================================================================
;;  
;;  [Writing](/tags/writing) [Software](/tags/software) [Clojure from the ground up](/tags/clojure-from-the-ground-up)
;;  
;;  2020-08-27
;;  
;;  Previously: [Debugging](/posts/319-clojure-from-the-ground-up-debugging).
;;  
;;  In this chapter, we’ll discuss some of Clojure’s mechanisms for _polymorphism_: writing programs that do different things depending on what kind of inputs they receive. We’ll show ways to write _open_ functions, which can be extended to new conditions later on, without changing their original definitions. Along the way, we’ll investigate Clojure’s type system in more detail–discussing _interfaces_, _protocols_, how to construct our own datatypes, and the relationships between types which let us write flexible programs.
;;  
;;  Thus far, our functions have taken one type of input. For example:
;;  
;;      (defn append
;;        "Adds an element x to the end of a vector v."
;;        [v x]
;;        (conj v x))
;;      
;;      scratch.polymorphism=> (append [1 2] 3)
;;      [1 2 3]
;;      
;;  
;;  But we might want to append to _more_ than vectors. What if we wanted to append something to the end of a list?
;;  
;;      scratch.polymorphism=> (append '(1 2) 3)
;;      (3 1 2)
;;      
;;  
;;  Since `conj` prepends to lists, our `append` function doesn’t work correctly here. We could redefine `append` in a way that works for both vectors and lists–for instance, using `concat`:
;;  
;;      (defn append-concat
;;        "Adds an element x to the end of a collection coll by concatenating a
;;        single-element list (x) to the end of coll."
;;        [coll x]
;;        (concat coll (list x)))
;;      
;;  
;;  But this is less than ideal: `concat` produces a wrapper object every time we call `append-concat`, which introduces unnecessary overhead when working with vectors. What we would like is a function which does different things to different types of inputs. This is the heart of _polymorphism_.
;;  
;;  [A Simple Approach](#a-simple-approach)
;;  ---------------------------------------
;;  
;;  We have a function `type` which returns the type of an object. What if append asked for the type of collection it was being asked to append to, and did different things based on that type? Let’s check the types of lists and vectors:
;;  
;;      (type [1 2])
;;      clojure.lang.PersistentVector
;;      (type '(1 2))
;;      clojure.lang.PersistentList
;;      
;;  
;;  Okay, so we could try checking whether the type of our collection is a PersistentVector, and if so, use `conj` to append an element efficiently!
;;  
;;      (defn append
;;        "Adds an element x to the end of a collection coll. Coll may be either a
;;        vector or a list."
;;        [coll x]
;;        (condp = (type coll)
;;          clojure.lang.PersistentVector
;;          (conj coll x)
;;      
;;          clojure.lang.PersistentList
;;          (concat coll (list x))))
;;      
;;  
;;  As an aside: we’re using `condp =` instead of `case`, even though `case` might seem like the obvious solution here. That’s because `case` uses optimizations which require that each case is a a compile-time constant, and classes like `clojure.lang.PersistentVector` aren’t actually constant in that sense. Don’t worry too much about this—it’s not important for understanding this chapter. The important question is: does this approach of checking the type at runtime _work_? Can we append to both vectors and lists?
;;  
;;      scratch.polymorphism=> (append [1 2] 3)
;;      [1 2 3]
;;      scratch.polymorphism=> (append '(1 2) 3)
;;      (1 2 3)
;;      
;;  
;;  It does! We’ve written a _polymorphic function_ which can take two different kinds of input, and does different things depending on what type of input was provided. Just to confirm, let’s try an empty list:
;;  
;;      scratch.polymorphism=> (append '() 3)
;;      IllegalArgumentException No matching clause: class clojure.lang.PersistentList$EmptyList  scratch.polymorphism/append (polymorphism.clj:7)
;;      
;;  
;;  Oh shoot. Are empty lists… a _different type_?
;;  
;;      scratch.polymorphism=> (type '())
;;      clojure.lang.PersistentList$EmptyList
;;      
;;  
;;  Indeed, they are. Empty lists have a special type in Clojure: `clojure.lang.PersistentList` is not the same type as `clojure.lang.PersistentList$EmptyList`. Why, then, are they mostly interchangeable? What is it that lets `()` and `(1 2 3)` behave as if they were both the same type of thing?
;;  
;;  [Subtypes](#subtypes)
;;  ---------------------
;;  
;;  Most languages have a notion of a _relationship_ between types. The exact nature of these relationships is complex and language-specific, but informally, most languages have a way to express that type `A` is a _subtype_ of type `B`, and conversely, `B` is a supertype of `A`. For instance, type `Cat` might be a subtype of type `Animal`. This allows us to write functions which depend only on properties of `Animal`, in such a way that they work automatically on `Cat`s, `Dog`s, `Fish`, and so on. This is another form of polymorphism!
;;  
;;  Some languages organize their types into a tree, such that each type is a subtype of exactly one other type ( except for a single “all-inclusive” type, often called `Top` or `Object`). We might say, for instance, that `Cat`s are `Animal`s, `AlarmClock`s are `Electronic`s, and both `Animal`s and `Electronic`s are `Object`s.
;;  
;;  This sounds straightforward enough, but types rarely fall into this kind of tree-like hierarchy neatly. For instance, both `Cat`s and `AlarmClock`s can yowl at you when you’d really prefer to be sleeping. Perhaps both should be subtypes of `Noisemaker`! But not all `Animal`s are `Noisemaker`s, nor are all `Noisemaker`s `Animal`s. Down this path lies madness! For this reason, most type systems allow a type to have _multiple_ supertypes: a `Cat` can be _both_ a `Noisemaker` and an `Animal`. In the JVM—the program which underlies Clojure—there are (and I speak very loosely here: we’re going to ignore [primitives](https://www.baeldung.com/java-primitives-vs-objects) and smooth over all kinds of internal details) two kinds of types, and both of these kinds of relationships are in play.
;;  
;;  The types of JVM values—things like `java.lang.Long`, `java.lang.String`, `clojure.lang.PersistentVector`, etc.—are called _classes_. If you have a value like `2` or `["foo" :bar]` in Clojure, that value’s type is a class. Each class is a subtype of exactly one other class, except for `Object`, the JVM’s Top class.
;;  
;;  The other kind of JVM type is called an _interface_ (or an [_abstract class_](https://pythonconquerstheuniverse.wordpress.com/2011/05/24/java-abc-vs-interface/)—we’ll use “interface” to refer to both throughout this chapter) and it defines the behavior for a type. In essence, an interface defines a collection of functions which take an instance of that interface as their first argument. Both classes and interfaces can be a subtype of any number of interfaces. Clojure uses interfaces to define the behavior of things like “a list” or “something you can look up values in”, and provides a variety of classes, each optimized for a different kind of work, which are _subtypes_ of those interfaces. These shared interfaces are why we can have two types of lists which work the same way.
;;  
;;  We can see these relationships between types in Clojure with the `supers` function, which returns the _supertypes_ of a given type:
;;  
;;      scratch.polymorphism=> (supers clojure.lang.PersistentList$EmptyList)
;;      #{clojure.lang.Obj clojure.lang.IPersistentCollection clojure.lang.IMeta clojure.lang.IObj clojure.lang.Sequential java.lang.Iterable java.io.Serializable clojure.lang.IPersistentStack java.lang.Object clojure.lang.IHashEq clojure.lang.IPersistentList clojure.lang.Seqable clojure.lang.ISeq clojure.lang.Counted java.util.List java.util.Collection}
;;      
;;      scratch.polymorphism=> (supers clojure.lang.PersistentList)
;;      #{clojure.lang.Obj clojure.lang.IPersistentCollection clojure.lang.IReduce clojure.lang.IMeta clojure.lang.IObj clojure.lang.Sequential java.lang.Iterable java.io.Serializable clojure.lang.IPersistentStack java.lang.Object clojure.lang.IHashEq clojure.lang.IPersistentList clojure.lang.Seqable clojure.lang.ISeq clojure.lang.ASeq clojure.lang.Counted java.util.List java.util.Collection clojure.lang.IReduceInit}
;;      
;;  
;;  A few of these types, like `java.lang.Object`, are actual classes. The rest are interfaces. Note that these sets are almost identical: empty and non-empty lists share almost all their supertypes. Both, for example, are subtypes of `clojure.lang.Counted`, which means that they keep track of how many elements they contain—the `count` function uses `Counted` to count collections efficiently. Both are `clojure.lang.Seqable`, which means they can be interpreted as a sequence of objects—that’s why we can call `map`, `filter`, and so on over lists. Most relevant for our purposes, both are kinds of `clojure.lang.IPersistentList`, which [defines](https://www.javadoc.io/doc/org.clojure/clojure/1.10.1/clojure/lang/IPersistentList.html) the core of how lists work: using `cons` to prepend elements. Let’s change our `append` function to use the `IPersistentList` type instead, and see if it lets us append to empty lists.
;;  
;;      (defn append
;;        "Adds an element x to the end of a collection coll. Coll may be either a
;;        vector or a list."
;;        [coll x]
;;        (condp = (type coll)
;;          clojure.lang.PersistentVector
;;          (conj coll x)
;;      
;;          clojure.lang.IPersistentList
;;          (concat coll (list x))))
;;      
;;      scratch.polymorphism=> (append '() 1)
;;      IllegalArgumentException No matching clause: class clojure.lang.PersistentList$EmptyList  scratch.polymorphism/append (polymorphism.clj:7)
;;      
;;  
;;  Ah, of course. We’re asking if the types of `coll` is _equal_ to `clojure.lang.IPersistentList`, but they’re not actually the same type. What we want to know is if the type of `coll` is a _subtype_ of `clojure.lang.IPersistentList`. Let’s check if any of `coll`’s _supertypes_ match as well:
;;  
;;      (defn append
;;        "Adds an element x to the end of a collection coll. Coll may be either a
;;        vector or a list."
;;        [coll x]
;;        (let [t     (type coll)
;;              types (conj (supers t) t)]
;;          (cond (types clojure.lang.PersistentVector)
;;                (conj coll x)
;;      
;;                (types clojure.lang.IPersistentList)
;;                (concat coll (list x))
;;      
;;                true (str "Sorry, I don't know how to append to a "
;;                      (type coll) ", which has supertypes " types))))
;;      
;;      scratch.polymorphism=> (append '() 1)
;;      (1)
;;      
;;  
;;  We’ve generalized our function from depending on _specific_ types to depending on a type _or its supertypes_. What about… a lazy sequence, like the ones returned by `map`?
;;  
;;      scratch.polymorphism=> (append (map inc [1 2 3]) 5)
;;      "Sorry, I don't know how to append to a class clojure.lang.LazySeq, which has supertypes #{java.util.List clojure.lang.IHashEq java.io.Serializable clojure.lang.IObj clojure.lang.IPersistentCollection clojure.lang.ISeq java.util.Collection java.lang.Iterable clojure.lang.Seqable clojure.lang.IPending clojure.lang.Sequential java.lang.Object clojure.lang.IMeta clojure.lang.Obj}"
;;      
;;  
;;  We could add another clause for `LazySeq` to our definition of `append`—but would it actually be any _different_ from how we append to lists? If we plan to `concat` for both, perhaps we should search for a type that sequences and lists have in common.
;;  
;;      (require '[clojure.set :as set])
;;      scratch.polymorphism=> (set/intersection (supers clojure.lang.IPersistentList) (supers clojure.lang.LazySeq))
;;      #{clojure.lang.IPersistentCollection clojure.lang.Seqable clojure.lang.Sequential}
;;      
;;  
;;  These types have three supertypes in common. One is `IPersistentCollection`, which defines how _any_ Clojure collection works, including sets, maps, etc. Another is `Seqable`, which means that the collection can be _interpreted_ as a sequence of values—this too applies to sets and maps. The final type in common is `Sequential`, which applies only to collections _with a well-defined order_: lists and vectors, but not sets and maps. If we think of `append` as operating only over _ordered_ collections, we should define it in terms of Sequential, rather than Seqable.
;;  
;;      (defn append
;;        "Adds an element x to the end of any sequential collection--faster for vectors."
;;        [coll x]
;;        (let [t     (type coll)
;;              types (conj (supers t) t)]
;;          (cond (types clojure.lang.PersistentVector)
;;                (conj coll x)
;;      
;;                (types clojure.lang.Seqable)
;;                (concat coll (list x))
;;      
;;                true (str "Sorry, I don't know how to append to a "
;;                      (type coll) ", which has supertypes " types))))
;;      
;;      scratch.polymorphism=> (append (map inc [1 2 3]) 5)
;;      (2 3 4 5)
;;      
;;  
;;  Now our function is even _more_ general: it can accept vectors, lists, and lazy sequences of all kinds, while being _smart_ about it: for vectors, it efficiently adds elements to the end using `conj`, and for other Sequential types, it falls back to using `concat`.
;;  
;;  This idea—checking a value’s type _and_ supertypes—is so useful that there’s a special function for it. We say that a value `v` is an _instance_ of type `T` if `v`’s type, or any of its supertypes, is `T`. We can use the `instance?` function to ask if this is so!
;;  
;;      scratch.polymorphism=> (instance? clojure.lang.PersistentVector [])
;;      true
;;      scratch.polymorphism=> (instance? clojure.lang.PersistentVector (list))
;;      false
;;      
;;  
;;  Thanks to the `instance?` function, we don’t need to compute the set of types and supertypes ourselves.
;;  
;;      (defn append
;;        "Adds an element x to the end of any sequential collection--faster for
;;        vectors."
;;        [coll x]
;;        (cond (instance? clojure.lang.PersistentVector coll)
;;              (conj coll x)
;;      
;;              (instance? clojure.lang.IPersistentList coll)
;;              (concat coll (list x))
;;      
;;              true (str "Sorry, I don't know how to append to a "
;;                        (type coll))))
;;      
;;  
;;  Wonderful! The supertype machinery disappears, and we’re left with something that asks succinctly about how a value might behave.
;;  
;;  This is a perfectly valid way to write a polymorphic function, but it has an important limitation. Whenever someone finds or creates a new type they’d like to append to, they have to edit the `append` function to add support for that type. This is one half of a classic dilemma in programming languages known as [the expression problem](https://wiki.c2.com/?ExpressionProblem). It would be nice if we could define functions piece by piece, so that we could add support for different types _without_ changing the original definition of the function. This is the motivation behind Clojure’s _multimethods_.
;;  
;;  [Multimethods](#multimethods)
;;  -----------------------------
;;  
;;  A _multimethod_ is a special kind of function. Instead of a function body, it has a _dispatch function_, which takes the arguments to the function and tells us not what to return, but how to find a particular _implementation_ of that function. We define the implementations (essentially, the function bodies) separately.
;;  
;;  To define a multimethod, use `defmulti`:
;;  
;;      (defmulti append
;;        "Appends an x to collection coll."
;;        (fn [coll x] (type coll)))
;;      
;;  
;;  Here, we’re defining an `append` function. This will overwrite our `append` function from earlier, so you can rename or delete the original to avoid the conflict, if you like. Like `defn`, we provide a docstring. Unlike `defn`, we follow that with a _dispatch function_, which takes two arguments (`coll` and `x`) and returns the type of `coll`. The return value of the dispatch function is how Clojure decides which implementation to use. All together, this `defmulti` says “the behavior of `append`, a function of two arguments, depends on the type of its first argument.”
;;  
;;  Next, we need to provide an _implementation_ of the `append` function. We do this with `defmethod`:
;;  
;;      (defmethod append clojure.lang.PersistentVector
;;        [coll x]
;;        (conj coll x))
;;      
;;  
;;  When `append`’s dispatch function returns `clojure.lang.PersistentVector`, we take the arguments `coll` and `x`, and use `conj` to append `x` to `coll`. This is the same implementation as our original polymorphic function for vectors, but we’ve decoupled the plumbing from the implementation: one function decides _which_ implementation to run, and the implementation does the work. This decoupling means we can add additional implementations (again using `defmethod`) without changing our existing implementation!
;;  
;;      (defmethod append clojure.lang.Sequential
;;        [coll x]
;;        (concat coll (list x)))
;;      
;;  
;;  This implementation of `append` takes a `clojure.lang.Sequential` as its first argument, and uses `concat` to add x to the end. Now our `append` function can take either a vector or any sequential object:
;;  
;;      scratch.polymorphism=> (append [1 2] 3)
;;      [1 2 3]
;;      scratch.polymorphism=> (append (map inc [1 2]) 4)
;;      (2 3 4)
;;      
;;  
;;  That’s odd! We dispatched using `(type coll)`, which, for `(map inc ...)`, would have been a `LazySeq`. But we didn’t define any method for `LazySeq`. Why… why did this work?
;;  
;;  The answer is that Clojure doesn’t compare multimethod dispatch values via `=`. It compares them using a function we haven’t seen before: `isa?`.
;;  
;;      scratch.polymorphism=> (doc isa?)
;;      -------------------------
;;      clojure.core/isa?
;;      ([child parent] [h child parent])
;;        Returns true if (= child parent), or child is directly or indirectly derived from
;;        parent, either via a Java type inheritance relationship or a
;;        relationship established via derive. h must be a hierarchy obtained
;;        from make-hierarchy, if not supplied defaults to the global
;;        hierarchy
;;      
;;  
;;  So `isa?` tells us whether two things are equal (using `=`), _or_ whether `child` is related to `parent` via Java types, _or_ via “a relationship established via derive”, whatever that is. The fact that `isa?` knows about Java type relationships means that we can use a supertype (e.g. `Sequential`) rather than listing every specific type (e.g. `PersistentList`, `LazySeq`, etc).
;;  
;;      scratch.polymorphism=> (isa? clojure.lang.PersistentList clojure.lang.Counted)
;;      true
;;      scratch.polymorphism=> (isa? clojure.lang.PersistentList clojure.lang.PersistentVector)
;;      false
;;      
;;  
;;  `isa?` has another trick up its sleeve–it says it can use relationships defined via `derive`. What does _that_ do?
;;  
;;      scratch.polymorphism=> (doc derive)
;;      -------------------------
;;      clojure.core/derive
;;      ([tag parent] [h tag parent])
;;        Establishes a parent/child relationship between parent and
;;        tag. Parent must be a namespace-qualified symbol or keyword and
;;        child can be either a namespace-qualified symbol or keyword or a
;;        class. h must be a hierarchy obtained from make-hierarchy, if not
;;        supplied defaults to, and modifies, the global hierarchy.
;;      
;;  
;;  Huh. So this lets us establish relationships between symbols or keywords. And classes, too—though classes can only be children. Let’s give that a shot.
;;  
;;      (derive ::milk  ::dairy)
;;      (derive ::dairy ::grocery)
;;      
;;      scratch.polymorphism=> (isa? ::milk ::milk)
;;      true
;;      scratch.polymorphism=> (isa? ::milk ::furniture)
;;      false
;;      scratch.polymorphism=> (isa? ::milk ::dairy)
;;      true
;;      scratch.polymorphism=> (isa? ::milk ::grocery)
;;      true
;;      
;;  
;;  With these `derive` statements, we’ve built a web of relationships between these keywords. Now `isa?` not only knows that milk is a kind of dairy, but also (because dairy is a kind of grocery) that milk is a kind of grocery. And we know that milk is _not_ furniture—I’m _pretty_ sure that’s true. Note that we’re using qualified keywords here (beginning with a `::`), which prevents us from accidentally changing the relationships in other namespaces.
;;  
;;  We’re not limited to defining 1:1 relationships. Milk can be a grocery _and_ refrigerated. Apples can _also_ be groceries.
;;  
;;      (derive ::milk ::refrigerated)
;;      (derive ::apples ::grocery)
;;      
;;      scratch.polymorphism=> (isa? ::milk ::grocery)
;;      true
;;      scratch.polymorphism=> (isa? ::milk ::refrigerated)
;;      true
;;      scratch.polymorphism=> (isa? ::apples ::grocery)
;;      true
;;      
;;  
;;  We can see the all the things that milk is by using the `parents` function. That’s kind of like supertypes, only these aren’t types: they’re just plain old keywords.
;;  
;;      scratch.polymorphism=> (parents ::milk)
;;      #{:scratch.polymorphism/refrigerated :scratch.polymorphism/dairy}
;;      
;;  
;;  And we can see all the things that are refrigerated using `descendents`. That’s kind of like subtypes:
;;  
;;      scratch.polymorphism=> (descendants ::grocery)
;;      #{:scratch.polymorphism/milk :scratch.polymorphism/apples :scratch.polymorphism/dairy}
;;      
;;  
;;  Now imagine we represented our groceries as maps. Something like `{:item-type ::milk, :size :gallon}`. When we get home from running errands, we’d like a function to put those grocery maps away—but _how_ they’re stored should depend on the `:item-type` of the grocery item. We could write:
;;  
;;      (defmulti put-away
;;        "Stores an item when we get home."
;;        :item-type)
;;      
;;  
;;  This takes advantage of the fact that keywords are functions: `:item-type` will look up the type of the item, and use that to choose an implementation.
;;  
;;  In general, we can put groceries in the pantry, and refrigerated items, we’ll put in the fridge.
;;  
;;      (defmethod put-away ::grocery
;;        [item]
;;        (println "Putting a" (name (:size item)) "of" (name (:item-type item))
;;                 "in the pantry"))
;;      
;;      (defmethod put-away ::refrigerated
;;        [item]
;;        (println "Storing a" (name (:size item)) "of" (name (:item-type item))
;;                 "in the fridge"))
;;      
;;  
;;  Now we can store some apples, and see them go into the pantry.
;;  
;;      scratch.polymorphism=> (put-away {:item-type ::apples, :size :large-bag})
;;      Putting a large-bag of apples in the pantry
;;      
;;  
;;  How about milk?
;;  
;;      scratch.polymorphism=> (put-away {:item-type ::milk, :size :gallon})
;;      IllegalArgumentException Multiple methods in multimethod 'put-away' match dispatch value: :scratch.polymorphism/milk -> :scratch.polymorphism/grocery and :scratch.polymorphism/refrigerated, and neither is preferred  clojure.lang.MultiFn.findAndCacheBestMethod (MultiFn.java:178)
;;      
;;  
;;  Ah, that’s interesting. Since milk is both a grocery _and_ refrigerated, _either_ of these implementations could apply to it. We can tell Clojure how to resolve the ambiguity using `prefer-method`:
;;  
;;      (prefer-method put-away ::refrigerated ::grocery)
;;      
;;      scratch.polymorphism=> (put-away {:item-type ::milk, :size :gallon})
;;      Storing a gallon of milk in the fridge
;;      
;;  
;;  Very good! We’ve established that the `::refrigerated` item type takes precedence over the `::grocery` item type. It’s important to prevent spoilage!
;;  
;;  You can use multimethods wherever you need to extend a function’s behavior later. This is especially useful when you intend your code to be used by other people—if someone else were to use our grocery-storage system, they could define new types of items, and be able to tell `put-away` exactly how to handle those new item types. We didn’t talk about garbage bags, pencils, or medication here, but because `put-away` is a multimethod, someone else could define something like `{:item-type ::medication}`, and extend `put-away` to store it correctly.
;;  
;;  Throughout this example, we’ve talked about “item types”, but… we used keywords, like `::apples`, to represent those types. These aren’t types in the sense of Clojure’s type system, but we could use them _like_ types. In a very real sense, what we’ve done here is define our own tiny language, with its own itty bitty type system, completely separate from Clojure’s. The core _ideas_ are the same: we use subtype relationships to write code which depends only on general things (e.g. “refrigerated things”) automatically cover more specific things (e.g. “milk”).
;;  
;;  Multimethods are powerful and general thanks to their dispatch functions. However, because those dispatch functions get involved in every call to a multimethod, they’re a bit slower than regular function calls. When performance matters, we turn to _interfaces_ and _protocols_.
;;  
;;  [Interfaces](#interfaces)
;;  -------------------------
;;  
;;  The idea of a polymorphic function which decides what to do based on the type of its arguments is so common, and so useful, that most languages provide special facilities for it. We call this “type dispatch”: the type of the value being passed chooses which particular code the language invokes. We wrote a version of type dispatch using multimethods and the `type` function. Many languages, such as Haskell and Java, build type dispatch into _every_ function—types are attached to each argument, and used to decide between alternative implementations.
;;  
;;  To support this feature in Java, the JVM has a fast, built-in mechanism for type dispatch using interfaces. We aren’t limited to using the interfaces given to us by Clojure and the JVM. We can define our own interfaces, and use them to get extra-speedy type dispatch, using the `definterface` macro.
;;  
;;      (definterface IAppend
;;        (append [x]))
;;      
;;  
;;  We’ve defined a new type: specifically, an interface. The name of our interface is `IAppend`. We’ve also stated that if a value `coll` is an instance of type IAppend, then there must be a _method_, named `append`. These methods are (and I know this is confusing) _not_ the multimethods we discussed earlier. These methods are _JVM_ methods: a sort of primitive function. Methods take arguments, evaluate code, and return results, like functions. Unlike Clojure’s functions, they aren’t values: you can’t ask them for docstrings, or pass them around to `map` or `filter`. We’ve provided only a single method here, but if we liked, we could define several in the same `definterface`.
;;  
;;  The `append` method we defined takes two arguments. Yes, _two_. Interfaces always take a first argument, which in this case must be an instance of `IAppend`. Since the first argument is mandatory, `definterface` doesn’t ask us to write it down. This is a bit weird, and contradicts how function definitions work everywhere else, but we’re stuck with this behavior for historical reasons. Long story short: `(append [x]` tells us that our first argument is an `IAppend`, and our second argument is some object called `x`. And that’s it! Like a multimethod, there’s no function body: we provide that later. Unlike a multimethod, there’s no dispatch function. The JVM will always dispatch based on the type of the first argument.
;;  
;;  “All right”, you might say. “It’s great that we have type to express that something is appendable, and an `append`… method, whatever that is, exists. But how do we make _an appendable thing_?”
;;  
;;  For this, we need new tools.
;;  
;;  [Making An Appendable Thing](#making-an-appendable-thing)
;;  ---------------------------------------------------------
;;  
;;  We have an interface, `IAppend`, and we’d like to make an _instance_ of that type. The quickest way to make an object of some type is to use a macro called `reify`: a fancy philosophical word that means “make a concrete thing out of an abstract concept.” In Clojure, `reify` takes interfaces, and definitions for how the methods in those interfaces should work, and returns an object which is an instance of those interfaces. For instance, perhaps we want an object to keep track of a grocery list:
;;  
;;      (defn grocery-list
;;        "Creates an appendable grocery list. Takes a vector of
;;        groceries to buy."
;;        [to-buy]
;;        (reify IAppend
;;          (append [this x]
;;            (grocery-list (conj to-buy x)))))
;;      
;;  
;;  There are two parts here: the first is a function, `grocery-list`, which we’re going to call when we want to make a new grocery list. The second is the `(reify IAppend ...)`, which constructs a value. That value will be an instance of type `IAppend`; at compile time, `reify` summons a new, anonymous class from the void, and makes sure that class is a subtype of `IAppend`. Each call to this `(reify ...)` constructs a new instance of that anonymous class.
;;  
;;  Inside the `reify`, we’ve provided definitions for _how_ to handle `IAppend`’s methods: when someone calls the `append` method with `this` (some value which this reify constructed) and `x`, we add `x` to the end of the `to-buy` vector using `conj`, and call `grocery-list` to make a new `GroceryList` out of it. That way we can keep appending more things later.
;;  
;;  An interesting thing to note: like `fn`, `reify` can use variables, like `to-buy`, from the surrounding code. When `grocery-list` returns, the object constructed by `reify` _remembers_ the value of `to-buy`, and can use it later. We say that `reify`, like `fn`, _closes over_ those variables: `reify` and `fn` are _closures_. That’s a fancy bit of programming jargon you can use to get strangers to stop talking to you at parties.
;;  
;;  Let’s try it out:
;;  
;;      scratch.polymorphism=> (grocery-list [:eggs])
;;      #object[scratch.polymorphism$grocery_list$reify__1950 0x70e02b5 "scratch.polymorphism$grocery_list$reify__1950@70e02b5"]
;;      
;;  
;;  This is not a particularly helpful representation of a grocery list. If you squint, you can see the namespace (`scratch.polymorphism`) and function (`grocery_list`) in there, and also `reify`, since we used `reify` to make this value. The `_1950` is a unique number that helps the computer tell this particular reify apart from others. In fact, this whole first part is the automatically generated class which `reify` defined for us. `0x70e02b5` is a number that identifies where this particular instance of that class lives in memory. Unhelpfully, _nothing_ here tells us about the to-buy list we provided (`[:eggs]`).
;;  
;;  One thing we _do_ know, though, is that this is something we can append to.
;;  
;;      scratch.polymorphism=> (supers (type (grocery-list [:eggs])))
;;      #{clojure.lang.IObj scratch.polymorphism.IAppend java.lang.Object clojure.lang.IMeta}
;;      
;;  
;;  Remember how many types were in `(supers clojure.lang.PersistentVector)`? Objects made with `reify` are far simpler. There’s `IAppend`: that’s the interface type we defined earlier. There’s `java.lang.Object`, of course. `clojure.lang.IObj` and `IMeta` mean that our reify object has metadata. Wait—what _is_ this thing’s metadata anyway?
;;  
;;      scratch.polymorphism=> (meta (grocery-list [:eggs]))
;;      {:line 12, :column 3}
;;      
;;  
;;  Huh! That’s the line and column number, of the `reify` expression which made this object. But what about appending? How do we use `append` with this thing?
;;  
;;      scratch.polymorphism=> (append (grocery-list [:eggs]) :tofu)
;;      "Sorry, I don't know how to append to a class scratch.polymorphism$grocery_list$reify__1491"
;;      
;;  
;;  Oh, wait, hang on—that’s our `append` function from before. We wanted to call the append _method_ we defined using `definterface`: methods and functions are different things, even if they have the same name. To make a method call, we put a `.` in front of the method name:
;;  
;;      scratch.polymorphism=> (.append (grocery-list [:eggs]) :tofu)
;;      #object[scratch.polymorphism$grocery_list$reify__1950 0x40eb00f0 "scratch.polymorphism$grocery_list$reify__1950@40eb00f0"]
;;      
;;  
;;  If we wanted a function for `append`, we could write one which calls the method. We might call this a _wrapper_ function, since it wraps the append method up in a nice functional package. This version of `append` we can use with `reduce` or `partial`, and so on.
;;  
;;      (defn append
;;        "Appends x to the end of coll."
;;        [coll x]
;;        (.append coll x))
;;      
;;  
;;  Moving on: we’ve called our `append` method, and it gave us… another unhelpful grocery-list. It’d be great if we had a more reasonable way to _print_ these lists to the console. In the JVM, the `Object` class defines a method called `toString`. That’s how `str` (typically) makes strings out of things. Let’s expand our `reify` to define a _different_ `toString` method:
;;  
;;      (defn grocery-list
;;        "Creates an appendable (via IAppend) grocery list. Takes a vector of
;;        groceries to buy."
;;        [to-buy]
;;        (reify
;;          IAppend
;;          (append [this x]
;;            (grocery-list (conj to-buy x)))
;;          
;;          Object
;;          (toString [this]
;;            (str "To buy: " to-buy))))
;;      
;;  
;;  In general, `reify` takes a type followed by method definitions for that particular type, then another type, and any number of methods for _that_ type, and so on. Our grocery lists were already `Object` before, and they were given simple, default definitions for all of `Object`’s methods–that’s how the REPL was able to show us `#object[scratch.polymorphism$grocery_list$reify__1950 ...]`. But now our call to `reify` states explicitly: when interpreted as an `Object`, here’s how the `toString` method works.
;;  
;;  Let’s see it in action!
;;  
;;      scratch.polymorphism=> (str (grocery-list [:eggs]))
;;      "To buy: [:eggs]"
;;      
;;  
;;  Hey, that’s more helpful! This is another kind of polymorphism at work: the `toString` method (and by extension, the `str` function) does different things depending on the type of object it’s given. And what’s neat is that _unlike_ our initial polymorphic function `append`—where we had a single function definition which had to know about _all_ the types we wanted to call… we didn’t have to change `toString` or `str`’s definitions. The plumbing—looking up what code to evaluate—is handled automatically. As with multimethods, we’re free to define behaviors for new types _without_ having to change the definitions for other types.
;;  
;;  Let’s try out our `append` method again and see if it works:
;;  
;;      scratch.polymorphism=> (str (.append (grocery-list [:eggs]) :tomatoes))
;;      "To buy: [:eggs :tomatoes]"
;;      
;;  
;;  Hey, that’s great! We can see the results of appending to our grocery list. What about appending to lists and vectors, though? Can we use `.append` with them?
;;  
;;      scratch.polymorphism=> (.append [1 2] 3)
;;      IllegalArgumentException No matching method found: append for class clojure.lang.PersistentVector  clojure.lang.Reflector.invokeMatchingMethod (Reflector.java:53)
;;      
;;  
;;  The _reflector_ is a part of Clojure which figures out what definition of a method to use for a given type. It _failed_ to find a matching method for `append`, given a `clojure.lang.PersistentVector`—which makes sense, because we haven’t made `clojure.lang.PersistentVector` a subtype of `IAppend`. Let’s do that next!
;;  
;;  I have terrible news: we _can’t_ do this. Interfaces are a one-way street: when we define a new type (as we did with `reify`), we can say how that type works with any number of interfaces. But when we define an interface, we _don’t_ get to say how it works with existing types. That’s just how the JVM’s type system works.
;;  
;;  “But this is awful!” You might exclaim. “The whole reason we defined an interface was so that we could write polymorphic functions like `append`, which could append to _many_ kinds of objects. Instead, we’re limited to polymorphism only over types which we ourselves define!”
;;  
;;  This is the other half of the [expression problem](https://wiki.c2.com/?ExpressionProblem) we mentioned earlier: existing (regular) functions can’t be extended to new types, and existing types can’t be extended to new interfaces. We solved the function-extension problem with multimethods and interfaces… but how do we solve the interface-extension problem?
;;  
;;  [Protocols](#protocols)
;;  -----------------------
;;  
;;  In Clojure, a _protocol_ is like an interface which can be extended to existing types. It defines a named type, together with functions whose first argument is an instance of that type. Where interfaces are built into the JVM, protocols are a Clojure-specific construct. To define a protocol, we use `defprotocol`:
;;  
;;      (defprotocol Append
;;        "This protocol lets us add things to the end of a collection."
;;        (append [coll x]
;;                "Appends x to the end of collection coll."))
;;      
;;  
;;  If you still have the `append` function we wrote earlier, this `append` function will replace it; you’ll see a message like `Warning: protocol #'scratch.polymorphism/Append is overwriting function append` at the REPL. You can delete or rename the original `append` function if you like.
;;  
;;  We’ve named our protocol `Append` (not to be confused with the interface `IAppend`), and given it a bit of documentation to remind us what it’s for. It has one function, named `append`, which takes two arguments: `coll` and `x`. We can give a docstring for the `append` function too. Like an interface, we _don’t_ define how the function works: we’re simply saying it exists. Unlike interfaces, these are real functions, not methods. Their first arguments are explicit, they have docstrings, we don’t need to use a `.` to call them, and they can be passed around to other functions.
;;  
;;  We can ask for our protocol’s documentation at the repl, just like we can for functions and namespaces:
;;  
;;      scratch.polymorphism=> (doc Append)
;;      -------------------------
;;      scratch.polymorphism/Append
;;        This protocol lets us add things to the end of a collection.
;;      
;;  
;;  And likewise, functions defined in `defprotocol` can be inspected, just like those made with `defn`.
;;  
;;      scratch.polymorphism=> (doc append)
;;      -------------------------
;;      scratch.polymorphism/append
;;      ([coll x])
;;        Appends x to the end of collection coll.
;;      
;;  
;;  If we try to use the `append` function with a grocery list, it’s going to fail: the grocery list `reify` is a subtype of the _interface_ `IAppend`, but we haven’t told it how the _protocol_ `Append` works yet:
;;  
;;      scratch.polymorphism=> (append (grocery-list [:eggs]) :tomatoes)
;;      IllegalArgumentException No implementation of method: :append of protocol: #'scratch.polymorphism/Append found for class: scratch.polymorphism$grocery_list$reify__1758  clojure.core/-cache-protocol-fn (core_deftype.clj:568)
;;      
;;  
;;  This error tells us that the `append` function doesn’t have an _implementation_ (a function body) for the type `scratch.polymorphism$grocery_list$reify__1758`. We can fix that by changing our `reify` to use the `Append` protocol, instead of the `IAppend` interface. This is a one-character change: protocol functions and interface methods are defined in `reify` in exactly the same way.
;;  
;;      (defn grocery-list
;;        "Creates an appendable (via IAppend) grocery list. Takes a vector of
;;        groceries to buy."
;;        [to-buy]
;;        (reify
;;          Append
;;          (append [this x]
;;            (grocery-list (conj to-buy x)))
;;      
;;          Object
;;          (toString [this]
;;            (str "To buy: " to-buy))))
;;      
;;  
;;  Now we can use our `append` function with grocery lists!
;;  
;;      scratch.polymorphism=> (str (append (grocery-list [:eggs]) :tomatoes))
;;      "To buy: [:eggs :tomatoes]"
;;      
;;  
;;  So far, we’ve done exactly what we did with interfaces. In fact, when we called `defprotocol`, it not only defined a protocol: it also defined an interface as well. But unlike interfaces, we can extend our protocol to cover _existing_ types. To do this, we use `extend-protocol`:
;;  
;;      (extend-protocol Append
;;        clojure.lang.IPersistentVector
;;        (append [v x]
;;          (conj v x)))
;;      
;;  
;;  This expresses that the `Append` protocol’s functions (i.e. `append`) can now be used on anything which is an `IPersistentVector`. When we call `(append v x)` with a vector `v`, we return the result of `(conj v x)`. Let’s try it out:
;;  
;;      scratch.polymorphism=> (append [1 2] 3)
;;      [1 2 3]
;;      
;;  
;;  Fantastic! What about other sequential collections?
;;  
;;      (extend-protocol Append
;;        clojure.lang.IPersistentVector
;;        (append [v x]
;;          (conj v x))
;;        
;;        clojure.lang.Sequential
;;        (append [v x]
;;          (concat v (list x))))
;;      
;;  
;;  `extend-protocol` can take several types, and the function definitions for each of them. Here, we’re extending `Append` over both `IPersistentVector` and `Sequential`—and providing definitions for how `append` works in each case. If you want to extend a single type to multiple protocols, use `extend-type`. Both `extend-protocol` and `extend-type` can be called as often as you like: all their definitions get merged together.
;;  
;;  We can even extend a protocol over `nil`! We could add this to the existing `extend-protocol`, or write it separately. This is another advantage of protocols over interfaces.
;;  
;;      (extend-protocol Append
;;        nil
;;        (append [v x]
;;          [x]))
;;      
;;      scratch.polymorphism=> (append nil 2)
;;      [2]
;;      
;;  
;;  [Named Datatypes](#named-datatypes)
;;  -----------------------------------
;;  
;;  We’ve used `reify` to make an object which satisfies some interfaces or protocols. Like an anonymous function `(fn [x] ...)`, `reify` creates an _anonymous type_. Because the `reify` type has no (predictable) name, we can’t extend protocols to it later. How do we make a type with a name–like `clojure.lang.PersistentVector`, or `clojure.lang.LazySeq`?
;;  
;;  There are two tools at our disposal here: `deftype` and `defrecord`. Both define new named types—classes, to be exact. The `deftype` macro produces a very basic datatype, whereas `defrecord` defines a type which behaves, in many respects, like a Clojure map. First, `deftype`:
;;  
;;      (deftype GroceryList [to-buy]
;;        Append
;;        (append [this x]
;;          (GroceryList. (conj to-buy x)))
;;        
;;        Object
;;        (toString [this]
;;          (str "To buy: " to-buy)))
;;      
;;  
;;  We’re defining a new type, named `GroceryList`. Objects of this type keep track of a single variable, called `to-buy`. Just as with `reify`, we provide a sequence of types we’d like GroceryLists to be a subtype of, and provide implementations for their functions or methods. The only difference is that in `append`, we construct a new grocery list using `(GroceryList. to-buy)`. We use the name of the class followed by a period `.` to make a new instance of Grocerylist.
;;  
;;  Let’s try creating one of these GroceryLists.
;;  
;;      scratch.polymorphism=> (GroceryList. [:eggs])
;;      #object[scratch.polymorphism.GroceryList 0x370dbd33 "To buy: [:eggs]"]
;;      
;;  
;;  Voilà! An instance of GroceryList. We’ve got the full name of the type: `GroceryList`, preceded by the namespace `scratch.polymorphism`. There’s a memory address, and then our string representation. Can we append to it?
;;  
;;      scratch.polymorphism=> (append (GroceryList. [:eggs]) :spinach)
;;      #object[scratch.polymorphism.GroceryList 0x3c612037 "To buy: [:eggs :spinach]"]
;;      
;;  
;;  Indeed we can. What else can we do with a GroceryList?
;;  
;;      scratch.polymorphism=> (supers GroceryList)
;;      #{clojure.lang.IType scratch.polymorphism.Append java.lang.Object}
;;      
;;  
;;  Not much. There’s `clojure.lang.IType`, which just means “this thing is a Clojure datatype”. There’s our `Append` protocol, and `java.lang.Object`, of course—almost _everything_ is a subtype of Object. As it turns out, `deftype` is pretty bare-bones.
;;  
;;  We _do_ get a few things for free with `deftype`. We can access the fields by using `.some-field-name`, like so:
;;  
;;      scratch.polymorphism=> (.to-buy (GroceryList. [:eggs]))
;;      [:eggs]
;;      
;;  
;;  And we also get a function that takes a `to-buy` list and builds a new `GroceryList`. These “constructor functions” take one argument for each field in the `deftype`.
;;  
;;      scratch.polymorphism=> (->GroceryList [:strawberries])
;;      #object[scratch.polymorphism.GroceryList 0x44cc69b3 "To buy: [:strawberries]"]
;;      
;;  
;;  This is a small wrapper around `(GroceryList. to-buy)`. It’s there because `GroceryList.`, like a method, isn’t a full-fledged Clojure function. Like methods, we can’t use `GroceryList.` with `map` or `apply`, or other things that expect functions. But we _can_ use `->GroceryList` in these contexts!
;;  
;;      scratch.polymorphism=> (map GroceryList. [[:twix] [:kale :bananas]])
;;      CompilerException java.lang.ClassNotFoundException: GroceryList., compiling:(/tmp/form-init2122621676255621718.clj:1:1) 
;;      
;;      scratch.polymorphism=> (map ->GroceryList [[:twix] [:kale :bananas]])
;;      (#object[scratch.polymorphism.GroceryList 0x552db723 "To buy: [:twix]"] #object[scratch.polymorphism.GroceryList 0x4d81eefd "To buy: [:kale :bananas]"])
;;      
;;  
;;  The types constructed by `deftype` are _so_ basic that they lack properties we’ve taken for granted so far—like equality:
;;  
;;      scratch.polymorphism=> (= (GroceryList. [:cheese]) (GroceryList. [:cheese]))
;;      false
;;      
;;  
;;  The _only_ thing a GroceryList is equal to is _itself_.
;;  
;;      scratch.polymorphism=> (let [gl (GroceryList. [:fish])] (= gl gl))
;;      true
;;      
;;  
;;  This is Clojure being conservative—it doesn’t know if, say, two GroceryLists with the same `to-buy` list can _really_ be considered equivalent. It’s up to us to define that by providing an implementation for the `equals` method—another part of `Object`.
;;  
;;      (deftype GroceryList [to-buy]
;;        Append
;;        (append [this x]
;;          (GroceryList. (conj to-buy x)))
;;        
;;        Object
;;        (toString [this]
;;          (str "To buy: " to-buy))
;;        
;;        (equals [this other]
;;          (and (= (type this) (type other))
;;               (= to-buy (.to-buy other)))))
;;      
;;      scratch.polymorphism=> (= (GroceryList. [:cheese]) (GroceryList. [:cheese]))
;;      true
;;      
;;  
;;  Want to make all grocery lists equal? Go wild!
;;  
;;      (deftype GroceryList [to-buy]
;;        Append
;;        (append [this x]
;;          (GroceryList. (conj to-buy x)))
;;        
;;        Object
;;        (toString [this]
;;          (str "To buy: " to-buy))
;;        
;;        (equals [this other]
;;          (= (type this) (type other))))
;;      
;;      scratch.polymorphism=> (= (GroceryList. [:ketchup]) (GroceryList. [:mayo]))
;;      true
;;      
;;  
;;  So, `deftype` gives us the power to construct our own, primitive types. But most of the time, we don’t _want_ this degree of control: defining exactly how to print our values, how to compare two values together, and so on. After all, plain old maps are a great way to model data. They’re easy to print and easy to manipulate. It’d be nice if we could create a type—to take advantage of protocols—but have it still work like a map. Clojure calls this kind of type a _record_.
;;  
;;      (defrecord GroceryList [to-buy]
;;        Append
;;        (append [this x]
;;          (GroceryList. (conj to-buy x))))
;;      
;;  
;;  The `defrecord` macro looks almost exactly like `deftype`: it takes the name of the type we’re defining, the names of the fields each instance will keep track of, and then a series of types with method implementations. As with `deftype`, we can construct instances of our `GroceryList` type using `GroceryList.` or the `->GroceryList` function.
;;  
;;      scratch.polymorphism=> (GroceryList. [:beans])
;;      #scratch.polymorphism.GroceryList{:to-buy [:beans]}
;;      
;;  
;;  _Unlike_ `deftype`, we get a nice, concise string representation for free. The first part shows the type name, and after that it looks just like a map, showing the fields of this `GroceryList` and their corresponding values.
;;  
;;  We don’t have to define our own equality either: two records are equal if they’re of the same type, and their fields are equal.
;;  
;;      scratch.polymorphism=> (= (GroceryList. [:beans]) (GroceryList. [:beans]))
;;      true
;;      scratch.polymorphism=> (= (GroceryList. [:beans]) {:to-buy [:beans]})
;;      false
;;      
;;  
;;  A GroceryList works _like_ a map, but it’s not the same type: records aren’t equal to maps, even if they have the same keys and values.
;;  
;;  Like `deftype`, we can access the fields of a record using `.to-buy`:
;;  
;;      scratch.polymorphism=> (.to-buy (GroceryList. [:bread]))
;;      [:bread]
;;      
;;  
;;  But since records work like maps, we can also access them using `get`, or by using keywords as functions:
;;  
;;      scratch.polymorphism=> (get (GroceryList. [:bread]) :to-buy)
;;      [:bread]
;;      scratch.polymorphism=> (:to-buy (GroceryList. [:bread]))
;;      [:bread]
;;      
;;  
;;  And we can alter those fields using `assoc` and `update`, just like maps. Let’s replace our shopping list with onions:
;;  
;;      scratch.polymorphism=> (-> (GroceryList. [:chicken])
;;                                 (assoc :to-buy [:onion]))
;;      #scratch.polymorphism.GroceryList{:to-buy [:onion]}
;;      
;;  
;;  … and add some beets:
;;  
;;      scratch.polymorphism=> (-> (GroceryList. [:chicken])
;;                                 (assoc :to-buy [:onion])
;;                                 (update :to-buy conj :beets))
;;      #scratch.polymorphism.GroceryList{:to-buy [:onion :beets]}
;;      
;;  
;;  Just as with maps, these updates are immutable: they don’t alter the original GroceryList. Instead, they create _copies_ with our requested changes. We aren’t limited to the fields we explicitly defined in the `defrecord`, either. Let’s tack on a `:note` to our grocery list:
;;  
;;      scratch.polymorphism=> (assoc (GroceryList. [:cherries]) :note "Tart cherries if possible!")
;;      #scratch.polymorphism.GroceryList{:to-buy [:cherries], :note "Tart cherries if possible!"}
;;      
;;  
;;  This is possible because records (unlike deftypes) always carry around an extra map—just in case they need to store additional fields we didn’t define up front. The `assoc` function tries to update a field if it can, and if there’s no field by that name, it stores it in the record’s extra map.
;;  
;;  Both `deftype` and `defrecord` produce named types, which means we can extend protocols over them after the fact. Let’s add a new protocol for printing out things nicely to the console—something we could use to print our grocery list and the items on it.
;;  
;;      (defprotocol Printable
;;        (print-out [x] "Print out the given object, nicely formatted."))
;;      
;;  
;;  Now we can define how to print a `GroceryList`. Let’s add a basic `print-out` function that works on any object, while we’re at it:
;;  
;;      (extend-protocol Printable
;;        GroceryList
;;        (print-out [gl]
;;          (println "GROCERIES")
;;          (println "---------")
;;          (doseq [item (:to-buy gl)]
;;            (print "[ ] ")
;;            (print-out item)
;;            (println)))
;;        
;;        Object
;;        (print-out [x]
;;          (print x)))
;;      
;;  
;;  Like we saw earlier, we can use `(:to-buy gl)` to get the items on the grocery list. We go through each one in turn using `doseq`, and call that particular item `item`. With that item, we print out a pair of brackets `"[ ] "`. Then we do something a bit strange: we call `print-out` _again_, but this time, with the `item` in question. The `Object` implementation takes over from there.
;;  
;;      scratch.polymorphism=> (print-out (GroceryList. [:cilantro :carrots :pork :baguette]))
;;      GROCERIES
;;      ---------
;;      [ ] :cilantro
;;      [ ] :carrots
;;      [ ] :pork
;;      [ ] :baguette
;;      
;;  
;;  Nice! This actually looks like a real grocery list. What if we wanted to keep track of how _many_ carrots to buy? We could introduce a _new_ type to keep track of things that come in a certain quantity:
;;  
;;      (defrecord CountedItem [thing quantity]
;;        Printable
;;        (print-out [this]
;;          (print-out thing)
;;          (print (str " (" quantity "x)"))))
;;      
;;  
;;  We’ve defined how to print out a counted item: first we print out the thing, then the quantity in parentheses. Let’s give that a shot:
;;  
;;      scratch.polymorphism=> (print-out (GroceryList. [:cilantro (CountedItem. :carrots 2) :pork :baguette]))
;;      GROCERIES
;;      ---------
;;      [ ] :cilantro
;;      [ ] :carrots (2x)
;;      [ ] :pork
;;      [ ] :baguette
;;      
;;  
;;  Neat! We didn’t have to change `GroceryList` at all to get this behavior. Because it used the polymorphic protocol function `print-out`, it _automatically_ knew how to work with our new `CountedItem` type.
;;  
;;  [When To Use Deftype and Defrecord](#when-to-use-deftype-and-defrecord)
;;  -----------------------------------------------------------------------
;;  
;;  If you’re coming from an object-oriented language (e.g. Ruby, Java), or a typed language with algebraic datatypes (e.g. Haskell, ML), you might see `defprotocol`, `deftype`, and `defrecord`, and think: “Ah, finally. Here are the tools I’ve been waiting for.” You might start by wanting to model a person, and immediately jump to `(defrecord Person [name pronouns age])`. While this is valid, you should take a step back, and ask: do I _need_ polymorphism here? Are there going to be functions that take people _and_ animals? Or do I simply want to keep track of some data?
;;  
;;  If you don’t need polymorphism, there’s a good chance your data can be modeled in Clojure as plain old maps, sets, vectors, and so on. Need to represent a person? How about:
;;  
;;      {:name     "Morgan"
;;       :pronouns [:they :them]
;;       :age      56}
;;      
;;  
;;  No `defrecord` required. Sticking to maps keeps your data in a shape that can be easily manipulated using standard Clojure functions. It’s easy to store this data on disk, or send it across the network. It’s easier to share this kind of data with other people. And it’s more concise to print at the console, which makes debugging your programs easier.
;;  
;;  Conversely, you’ll want to use `defrecord` and `deftype` when maps aren’t sufficient: when you need polymorphism, when you need to participate in existing protocols or interfaces, or when multimethod performance is too slow. Records are often faster and more memory-efficient than maps, so even if you don’t need the polymorphism, it can be worthwhile to define a record or so when map performance bogs you down. This is something you’ll want to find out by _measuring_ your code, though, rather than simply assuming.
;;  
;;  If you’re reaching for records for type safety: it’s not going to be as helpful as you’d like. Functions like `assoc` work equally well across _all_ kinds of records, and the compiler won’t warn you about using the wrong keyword. Sticking to methods eliminates _some_ of those risks, but it’s nothing like the type guardrails in Java or Haskell. Clojure programs generally rely more on tests and contracts to prevent these type errors. There are also static type systems like [core.typed](https://github.com/clojure/core.typed), which we’ll discuss later.
;;  
;;  [Review](#review)
;;  -----------------
;;  
;;  When a function’s behavior depends on the type of values it is provided, we call that function polymorphic. Many of Clojure’s core functions, like `conj` or `reduce`, are polymorphic: we can `conj` into maps, vectors, sets, and lists, and each does something different. Often, our own code is _implicitly_ polymorphic by virtue of using other polymorphic functions: `(defn add-bird [coll] (conj coll :bird))` can add birds to lots of different things.
;;  
;;  When we need a function whose behavior explicitly depends on its arguments, we can use ad-hoc approaches, like `if`, `cond`, or `case`. The `instance?`, `type`, and `supers` functions let us choose what to do based on the _type_ of the value.
;;  
;;  When we need an _open_ function—one whose behavior can be extended to new things _later_—we use a multimethod, an interface, or a protocol. Multimethods are the most general approach: they use a _dispatch function_, which receives the function’s arguments and decides which implementation to call. They’re not limited to dispatching by argument type: they can use arbitrary values and relationships between keywords, defined with `derive`. They also offer fine-grained control when that dispatch would be ambiguous. This flexibility comes with a performance penalty: Clojure has to evaluate the dispatch function every time the multimethod is called.
;;  
;;  When a function’s behavior depends on the type of the first argument, use protocols or interfaces. Interfaces can’t be extended to existing types; protocols can. Protocols have some ergonomic advantages: they define regular functions, rather than methods, and come with documentation—though there’s nothing stopping you from writing your own documented wrapper functions, or using [definterface+](https://github.com/aleph-io/potemkin#definterface), which does so automatically. Interfaces are slightly faster; prefer them when performance matters.
;;  
;;  To create instances of a new type, we have `reify`. Like `(fn [x] ...)`, `reify` generates an _anonymous_ type—it can have interfaces and protocols as supertypes, and provides implementations for those types, but has no (predictable, meaningful) name. When we want to name our types—perhaps so that other people can extend them later—we use `deftype` and `defrecord`. Most of the time, `defrecord` is most useful: they work like maps out of the box. However, `deftype` is available should we need to construct bare-bones types with unusual behaviors.
;;  
;;  We _haven’t_ talked about the details of classes or inheritance in this discussion. These are important for Java interop, but we don’t use these concepts often in Clojure. A topic for later discussion!
;;  
;;  [Problems](#problems)
;;  ---------------------
;;  
;;  *   Write a `sorted` function that uses `cond` and `instance?` to convert lists to sorted lists (using `(sort ...)`), and sets to sorted sets (using `(into (sorted-set) ...)`).
;;      
;;  *   Rewrite `sorted` as a multimethod. Using `defmethod`, extend `sorted` to handle maps.
;;      
;;  *   Add a `checked-off` field to the `GroceryList` type, and use it to store a set of items that are already in the cart. Write a `check-off` function that takes a grocery list and checks off an item on it, by adding that item to the `checked-off` set: `(check-off my-list eggs)`
;;      
;;  *   Write a `remaining` function which takes a `GroceryList` and returns the items that _haven’t_ been checked off yet.
;;      
;;  *   Change the definition of `print-out` for `GroceryList` to take the `checked-off` set into account, printing an `[x]` in front of checked-off items.
;;      
;;  *   Imagine Clojure had _no_ built-in sets. Make up a `Set` protocol with some basic operations, like `add-element`, `has-element?`, and `remove-element`.
;;      
;;  *   Using a vector or list to store your elements, write a basic implementation of your `Set` protocol. Experiment to make sure adding the same item twice doesn’t add two copies.
;;      
;;  *   Try making larger and larger sets–say, with ten, a thousand, and a hundred thousand elements. Use `(time (has-element? some-set 123))` to see how your set performance changes with size. Why is this?
;;      
;;  *   Write a different implementation of a `Set`, which uses a _map_ to store its elements. Compare its performance to your list-based set.
;;      
;;  *   The `deref` function uses an interface called `clojure.lang.IDeref` to return the _current value_ of a container type. Using `deftype`, define your own container type. Try `@(MyContainer. :hi)` to verify that you can get the value of your container (`:hi`) back.
;;      
;;  *   \[advanced\] So far, we’ve worked only with immutable types. `deftype` lets us define _mutable_ types by tagging a field with `^:unsynchronized-mutable`, like so: `(deftype DangerBox [^:unsynchronized-mutable value] ...)`. Design a `Mutable` protocol with a `(write! box value)` function to overwrite the value of a mutable container. Using `(set! field value)`, build your own mutable container type which supports both `Mutable` and `IDeref`. Confirm that you can change its value using `write!`, and read it back using `@`.
;;      
;;  *   \[advanced\] Use your mutable container as a counter by reading its current state and writing back a value one greater–e.g. via `(write! box (inc @box))`. Using `dotimes`, perform _many_ updates in a row, and verify that the final value of the counter is the same as the number you passed to `dotimes`.
;;      
;;  *   \[advanced\] Run this `dotimes` increment loop in two threads at once, using `future`. Is the final counter value what you expected? Why? How does this compare to using an `(atom)` with `swap!`?
;;      
;;  
;;  ![Rune](https://www.gravatar.com/avatar/294de3557d9d00b3d2d8a1e6aab028cf?r=pg&s=96&d=identicon "Rune")
;;  
;;  Rune on [2020-08-28](/posts/352-clojure-from-the-ground-up-polymorphism#comment-3194)
;;  
;;  FYI, the link to the previous blog entry is broken. It should have led to hxxps://aphyr.com/posts/319-clojure-from-the-ground-up-debugging
;;  
;;  ![Jochen](https://www.gravatar.com/avatar/3956eec13263ffb25480c0a31c74bf05?r=pg&s=96&d=identicon "Jochen")
;;  
;;  Jochen on [2020-08-28](/posts/352-clojure-from-the-ground-up-polymorphism#comment-3195)
;;  
;;  This was amazing! Thank you I also appreciated how you explained the often cryptic error messages coming from the clojure evaluator.
;;  
;;  ![Carlos](https://www.gravatar.com/avatar/8be1592fe29f2a7ac4e9e6ad683baaa1?r=pg&s=96&d=identicon "Carlos")
;;  
;;  Carlos on [2020-09-03](/posts/352-clojure-from-the-ground-up-polymorphism#comment-3197)
;;  
;;  This is probably the best article about polimorfism in clojure,well done!
;;  
;;  Thank you…
;;  
;;  ![Jiacai](https://www.gravatar.com/avatar/19b0b76c5ab16aa5cd11a46b9e25c080?r=pg&s=96&d=identicon "Jiacai")
;;  
;;  [Jiacai](https://twitter.com/liujiacai) on [2020-09-19](/posts/352-clojure-from-the-ground-up-polymorphism#comment-3213)
;;  
;;  `(defprotocol Mutable (write! [this x])) (deftype DangerBox [^:unsynchronized-mutable value] Mutable (write! [this x] (set! value x)) clojure.lang.IDeref (deref [this] value)) (let [box (->DangerBox 0) f1 (future (dotimes [_ 100] (write! box (inc @box)))) f2 (future (dotimes [_ 100] (write! box (inc @box))))] ;; join futures (println @f1 @f2) ;; due to race condition, the final result may well not be equals to 200 (println @box))`
;;  
;;  My solution to the advanced problems.
;;  
;;  ![Pyromancer](https://www.gravatar.com/avatar/d59ce8b760072b05fef23ef7ca4f5c67?r=pg&s=96&d=identicon "Pyromancer")
;;  
;;  Pyromancer on [2023-02-26](/posts/352-clojure-from-the-ground-up-polymorphism#comment-4264)
;;  
;;  I’ve just found your series. You manage to have a good balance, and I think it is great to promote depth over detail in understanding the language, specially one such has Clojure. On the same note, a repl tour of the language is also really good way to introduce Clojure without getting people entangled in tooling and editors/IDEs. And,very well written too!
;;  
;;  Thank you for your work and effort. I hope you decide to continue it!
;;  
;;  Post a Comment
;;  ==============
;;  
;;  Comments are moderated. Links have `nofollow`. Seriously, spammers, give it a rest.
;;  
;;  Please avoid writing anything here unless you're a computer. Captcha  This is also a trap: Comment
;;  
;;  Name 
;;  
;;  E-Mail (for [Gravatar](https://gravatar.com), not published) 
;;  
;;  Personal URL 
;;  
;;  Comment
;;  
;;  Supports [Github-flavored Markdown](https://guides.github.com/features/mastering-markdown/), including `[links](http://foo.com/)`, `*emphasis*`, `_underline_`, `` `code` ``, and `> blockquotes`. Use ` ```clj ` on its own line to start an (e.g.) Clojure code block, and ` ``` ` to end the block.    
;;  
;;  Copyright © 2023 Kyle Kingsbury.  
;;  Also on: [Mastodon](https://woof.group/@aphyr) and [Github](https://github.com/aphyr).
;;  
;;  var \_gaq = \_gaq || \[\]; \_gaq.push(\['\_setAccount', 'UA-9527251-1'\]); \_gaq.push(\['\_trackPageview'\]); (function() { var ga = document.createElement('script'); ga.type = 'text/javascript'; ga.async = true; ga.src = ('https:' == document.location.protocol ? 'https://ssl' : 'http://www') + '.google-analytics.com/ga.js'; var s = document.getElementsByTagName('script')\[0\]; s.parentNode.insertBefore(ga, s); })();
;;    10
)