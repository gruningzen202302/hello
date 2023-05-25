(ns ch001book)

(comment
  (* 5 8)
  (print "Clojure from the ground up  basic types")

;; window.dataLayer = window.dataLayer || \[\]; function gtag(){dataLayer.push(arguments);} gtag('js', new Date()); gtag('config', 'G-MXDP37S6QL');
;;   
;;       
;;   
;;   *   [Aphyr](/)
;;   *   [About](/about)
;;   *   [Blog](/posts)
;;   *   [Photos](/photos)
;;   *   [Code](http://github.com/aphyr)
;;   
;;   [Clojure from the ground up: basic types](/posts/302-clojure-from-the-ground-up-basic-types)
;;   ============================================================================================
;;   
;;   [Software](/tags/software) [Clojure](/tags/clojure) [Clojure from the ground up](/tags/clojure-from-the-ground-up)
;;   
;;   2013-10-26
;;   
;;   We’ve learned [the basics of Clojure’s syntax and evaluation model](http://aphyr.com/posts/301-clojure-from-the-ground-up-first-principles). Now we’ll take a tour of the basic nouns in the language.
;;   
;;   [Types](#types)
;;   ---------------
;;   
;;   We’ve seen a few different values already–for instance, `nil`, `true`, `false`, `1`, `2.34`, and `"meow"`. Clearly all these things are _different_ values, but some of them seem more alike than others.
;;   
;;   For instance, `1` and `2` are _very_ similar numbers; both can be added, divided, multiplied, and subtracted. `2.34` is also a number, and acts very much like 1 and 2, but it’s not quite the same. It’s got _decimal_ points. It’s not an _integer_. And clearly `true` is _not_ very much like a number. What is true plus one? Or false divided by 5.3? These questions are poorly defined.
;;   
;;   We say that a _type_ is a group of values which work in the same way. It’s a _property_ that some values share, which allows us to organize the world into sets of similar things. 1 + 1 and 1 + 2 use _the same addition_, which adds together integers. Types also help us _verify_ that a program makes sense: that you can only add together numbers, instead of adding numbers to porcupines.
;;   
;;   Types can overlap and intersect each other. Cats are animals, and cats are fuzzy too. You could say that a cat is a _member_ (or sometimes “instance”), of the fuzzy and animal types. But there are fuzzy things like moss which _aren’t_ animals, and animals like alligators that aren’t fuzzy in the slightest.
;;   
;;   Other types completely subsume one another. All tabbies are housecats, and all housecats are felidae, and all felidae are animals. Everything which is true of an animal is automatically true of a housecat. Hierarchical types make it easier to write programs which don’t need to know all the specifics of every value; and conversely, to create new types in terms of others. But they can also get in the way of the programmer, because not every useful classification (like “fuzziness”) is purely hierarchical. Expressing overlapping types in a hierarchy can be tricky.
;;   
;;   Every language has a _type system_; a particular way of organizing nouns into types, figuring out which verbs make sense on which types, and relating types to one another. Some languages are strict, and others more relaxed. Some emphasize hierarchy, and others a more ad-hoc view of the world. We call Clojure’s type system _strong_ in that operations on improper types are simply not allowed: the program will explode if asked to subtract a dandelion. We also say that Clojure’s types are _dynamic_ because they are enforced when the program is run, instead of when the program is first read by the computer.
;;   
;;   We’ll learn more about the formal relationships between types later, but for now, keep this in the back of your head. It’ll start to hook in to other concepts later.
;;   
;;   [Integers](#integers)
;;   ---------------------
;;   
;;   Let’s find the type of the number 3:
;;   
  (type 3)
;;       java.lang.Long
;;       
;;   
;;   So 3 is a `java.lang.Long`, or a “Long”, for short. Because Clojure is built on top of Java, many of its types are plain old Java types.
;;   
;;   Longs, internally, are represented as a group of sixty-four binary digits (ones and zeroes), written down in a particular pattern called [signed two’s complement representation](http://en.wikipedia.org/wiki/Two's_complement). You don’t need to worry about the specifics–there are only two things to remember about longs. First, longs use one bit to store the sign: whether the number is positive or negative. Second, the other 63 bits represent the _size_ of the number. That means the biggest number you can represent with a long is 263 - 1 (the minus one is because of the number 0), and the smallest long is -263.
;;   
;;   How big is 263 - 1?
;;   
  (+ (- java.lang.Long/MAX_VALUE 1) 1)
;;       9223372036854775807
;;       
;;   
;;   That’s a reasonably big number. Most of the time, you won’t need anything bigger, but… what if you did? What happens if you add one to the biggest Long?
;;   
;;       user=> 
  (inc Long/MAX_VALUE)
;;       
;;       ArithmeticException integer overflow  clojure.lang.Numbers.throwIntOverflow (Numbers.java:1388)
;;       
;;   
;;   An error occurs! This is Clojure telling us that something went wrong. The type of error was an `ArithmeticException`, and its message was “integer overflow”, meaning “this type of number can’t hold a number that big”. The error came from a specific _place_ in the source code of the program: `Numbers.java`, on line 1388. That’s a part of the Clojure source code. Later, we’ll learn more about how to unravel error messages and find out what went wrong.
;;   
;;   The important thing is that Clojure’s type system _protected_ us from doing something dangerous; instead of returning a corrupt value, it aborted evaluation and returned an error.
;;   
;;   If you _do_ need to talk about really big numbers, you can use a BigInt: an arbitrary-precision integer. Let’s convert the biggest Long into a BigInt, then increment it:
;;   
;;       user=> 
  (inc (bigint Long/MAX_VALUE))
;;       9223372036854775808N
;;       
;;   
;;   Notice the N at the end? That’s how Clojure writes arbitrary-precision integers.
;;   
;;       user=> 
  (type 5N)
;;       clojure.lang.BigInt
;;       
;;   
;;   There are also smaller numbers.
;;   
;;       user=> 
  (type (int 0))
;;       java.lang.Integer
;;       user=> 
  (type (short 0))
;;       java.lang.Short
;;       user=> 
  (type (byte 0))
;;       java.lang.Byte
;;       
;;   
;;   Integers are half the size of Longs; they store values in 32 bits. Shorts are 16 bits, and Bytes are 8. That means their biggest values are 231\-1, 215\-1, and 27\-1, respectively.
;;   
;;       user=> Integer/MAX_VALUE
;;       2147483647
;;       user=> Short/MAX_VALUE
;;       32767
;;       user=> 
;;       Byte/MAX_VALUE
;;       127
  (byte 127)
  (inc (- Byte/MAX_VALUE 1))
  (byte 129)
  (byte (inc Byte/MAX_VALUE))
  (- (inc Byte/MAX_VALUE) 1)
;;      
;;   
;;   [Fractional numbers](#fractional-numbers)
;;   -----------------------------------------
;;   
;;   To represent numbers _between_ integers, we often use floating-point numbers, which can represent small numbers with fine precision, and large numbers with coarse precision. Floats use 32 bits, and Doubles use 64. Doubles are the default in Clojure.
;;   
;;       user=> 
  (type 1.23)
;;       java.lang.Double
;;       user=> 
  (type (float 1.23))
;;       java.lang.Float
;;       
;;   
;;   Floating point math is [complicated](http://en.wikipedia.org/wiki/Floating_point), and we won’t get bogged down in the details just yet. The important thing to know is floats and doubles are _approximations_. There are limits to their correctness:
;;   
;;       user=> 
  (inc 0.99999999999999999)
;;       2.0
;;       
;;   
;;   To represent fractions exactly, we can use the _ratio_ type:
;;   
;;       user=> 
  (type 1/3)
;;       clojure.lang.Ratio
;;       
;;   
;;   [Mathematical operations](#mathematical-operations)
;;   ---------------------------------------------------
;;   
;;   The exact behavior of mathematical operations in Clojure depends on their types. In general, though, Clojure aims to _preserve_ information. Adding two longs returns a long; adding a double and a long returns a double.
;;   
;;       user=> 
  (+ 1 2)
;;       3
;;       user=> 

  (+ 1 2.0)
;;       3.0
;;       
;;   
;;   `3` and `3.0` are _not_ the same number; one is a long, and the other a double. But for most purposes, they’re equivalent, and Clojure will tell you so:
;;   
;;       user=> 
  (= 3 3.0)
;;       false
;;       user=> 

  (== 3 3.0)
;;       true
;;       
;;   
;;   `=` asks whether all the things that follow are equal. Since floats are approximations, `=` considers them different from integers. `==` also compares things, but a little more loosely: it considers integers equivalent to their floating-point representations.
;;   
;;   We can also subtract with `-`, multiply with `*`, and divide with `/`.
;;   
;;       user=> 
  (- 3 1)
;;       2
;;       user=> 
  (* 1.5 3)
;;       4.5
;;       user=> 
  (/ 1 2)
;;        1/2 
  (type (/ 1 2))
  (float (/ 1 2))
;;       
;;   
;;   Putting the verb _first_ in each list allows us to add or multiply more than one number in the same step:
;;   
;;       user=> (+ 1 2 3)
;;       6
;;       user=> (* 2 3 1/5)
;;       6/5
;;       
;;   
;;   Subtraction with more than 2 numbers subtracts all later numbers from the first. Division divides the first number by all the rest.
;;   
;;       user=> 
  (- 5 1 1 1)
;;      2 
  (- 5 1 1 -1)

  (- 5 1 -1 -1)

  (- 5 1)

  (- 5)

;;       user=> (/ 24 2 3)
;;       4
;;       
;;   
;;   By extension, we can define useful interpretations for numeric operations with just a _single_ number:
;;   
;;       user=> (+ 2)
;;       2
;;       user=> (- 2)
;;       -2
;;       user=> (* 4)
;;       4
;;       user=> (/ 4)
;;       1/4
;;       
;;   
;;   We can also add or multiply a list of no numbers at all, obtaining the additive and multiplicative identities, respectively. This might seem odd, especially coming from other languages, but we’ll see later that these generalizations make it easier to reason about higher-level numeric operations.
;;   
;;       user=> (+)
;;       0
;;       user=> (*)
;;       1
;;       
;;   
;;   Often, we want to ask which number is bigger, or if one number falls between two others. `<=` means “less than or equal to”, and asserts that all following values are in order from smallest to biggest.
;;   
;;       user=> (<= 1 2 3)
;;       true
;;       user=> (<= 1 3 2)
;;       false
;;       
;;   
;;   `<` means “strictly less than”, and works just like `<=`, except that no two values may be equal.
;;   
;;       user=> 
  (<= 1 1 2)
;;       true
;;       user=> 
  (< 1 1 2)
;;       false
;;       
;;   
;;   Their friends `>` and `>=` mean “greater than” and “greater than or equal to”, respectively, and assert that numbers are in descending order.
;;   
;;       user=> 
  (> 3 2 1)
;;       true
  (> 3 2 2 1)
;;       user=> (> 1 2 3)
;;       false
;;       
;;   
;;   Also commonly used are `inc` and `dec`, which add and subtract one to a number, respectively:
;;   
;;       user=> (inc 5)
;;       6
;;       user=> 
  (dec 5)
;;       4

  (< (dec Byte/MAX_VALUE) Byte/MAX_VALUE)
  (> (dec Byte/MAX_VALUE) (- Byte/MAX_VALUE 2))
;;       
;;   
;;   One final note: equality tests can take more than 2 numbers as well.
;;   
;;       user=> (= 2 2 2)
;;       true
;;       user=> (= 2 2 3)
;;       false
;;       
;;   
;;   [Strings](#strings)
;;   -------------------
;;   
;;   We saw that strings are text, surrounded by double quotes, like `"foo"`. Strings in Clojure are, like Longs, Doubles, and company, backed by a Java type:
;;   
;;       user=> (type "cat")
;;       java.lang.String
;;       
;;   
;;   We can make almost _anything_ into a string with `str`. Strings, symbols, numbers, booleans; every value in Clojure has a string representation. Note that `nil`’s string representation is `""`; an empty string.
;;   
;;       user=> (str "cat")
;;       "cat"
;;       user=> (str 'cat)
;;       "cat"
;;       user=> (str 1)
;;       "1"
;;       user=> (str true)
;;       "true"
;;       user=> 
  (str '(1 2 3))
;;       "(1 2 3)"
;;       user=> 
  (str nil)
;;       ""
;;       
;;   
;;   `str` can also _combine_ things together into a single string, which we call “concatenation”.
;;   
;;       user=> 
  (str "meow " 3 " times")
;;       "meow 3 times"
;;       
;;   
;;   To look for patterns in text, we can use a [regular expression](http://www.regular-expressions.info/tutorial.html), which is a tiny language for describing particular arrangements of text. `re-find` and `re-matches` look for occurrences of a regular expression in a string. To find a cat:
;;   
;;       user=> 
  (re-find #"cat" "mystic cat mouse")
;;       "cat"


  (re-matches #"cat" "mystic cat mouse")
  (re-matches #"cat" "cat")

;;       user=> 
  (re-find #"cat" "only dogs here")
;;       nil
;;       
;;   
;;   That `#"..."` is Clojure’s way of writing a regular expression.
;;   
;;   With `re-matches`, you can extract particular parts of a string which match an expression. Here we find two strings, separated by a `:`. The parentheses mean that the regular expression should _capture_ that part of the match. We get back a list containing the part of the string that matched the first parentheses, followed by the part that matched the second parentheses.
;;   
;;       user=> 
  (rest (re-matches #"(.+):(.+)" "mouse:treat"))
;;       ("mouse" "treat")
;;       
  (re-matches #"(.+):(.+)" "mouse:treat")
  (rest (re-matches #"(.+):(.+)" "mouse:treat"))
  (rest (re-matches #"(.+):(.+)" "mouse:treat"))
;;   
;;   Regular expressions are a powerful tool for searching and matching text, especially when working with data files. Since regexes work the same in most languages, you can use any guide online to learn more. It’s not something you have to master right away; just learn specific tricks as you find you need them. For a deeper guide, try Fitzgerald’s [Introducing Regular Expressions](http://shop.oreilly.com/product/0636920012337.do).
;;   
;;   [Booleans and logic](#booleans-and-logic)
;;   -----------------------------------------
;;   
;;   Everything in Clojure has a sort of charge, a truth value, sometimes called “truthiness”. `true` is positive and `false` is negative. `nil` is negative, too.
;;   
;;       user=> 
  (boolean true)

;;       true
;;       false
;;       user=>
  (boolean nil)
;;       false
  (boolean false)
;;   
;;   Every other value in Clojure is positive.
;;   
;;       user=> (boolean 0)
;;       true
;;       user=> (boolean 1)
;;       true
;;       user=> (boolean "hi there")
;;       true
;;       user=> 
  (boolean str)
;;       true
;;       
;;   
;;   If you’re coming from a C-inspired language, where 0 is considered false, this might be a bit surprising. Likewise, in much of POSIX, 0 is considered success and nonzero values are failures. Lisp allows no such confusion: the only negative values are `false` and `nil`.
;;   
;;   We can reason about truth values using `and`, `or`, and `not`. `and` returns the first negative value, or the last value if all are truthy.
;;   
;;       user=> (and true false true)
;;       false
;;       user=> (and true true true)
;;       true
;;       user=> 
  (and 1 2 3)
;;       3
;;       
  (and 1 2 0 3)
  (and 1 2 nil 3)
  (and 1 false nil 3)
;;   
;;   Similarly, `or` returns the first positive value.
;;   
;;       user=> 
  (or false 2 3)
;;       2
;;       user=> 
  (or false nil)
  (or  nil false)
;;       
;;   
;;   And `not` inverts the logical sense of a value:
;;   
;;       user=> 
  (not 2)
;;       false
;;       user=> 
  (not nil)
;;       true
;;       
;;   
;;   We’ll learn more about Boolean logic when we start talking about _control flow_; the way we alter evaluation of a program and express ideas like “if I’m a cat, then meow incessantly”.
;;   
;;   [Symbols](#symbols)
;;   -------------------
;;   
;;   We saw symbols in the previous chapter; they’re bare strings of characters, like `foo` or `+`.
;;   
;;       user=> 
  (class 'str)
;;       clojure.lang.Symbol
;;       
  (class 'abc)
;;   
;;   Symbols can have either short or full names. The short name is used to refer to things locally. The _fully qualified_ name is used to refer unambiguously to a symbol from anywhere. If I were a symbol, my name would be “Kyle”, and my full name “Kyle Kingsbury.”
;;   
;;   Symbol names are separated with a `/`. For instance, the symbol `str` is also present in a family called `clojure.core`; the corresponding full name is `clojure.core/str`.
;;   
;;       user=> 
  (= str clojure.core/str)
;;       true
;;       user=> 
  (name 'clojure.core/str)
;;       "str"
;;       
;;   
;;   When we talked about the maximum size of an integer, that was a fully-qualified symbol, too.
;;   
;;       (type 'Integer/MAX_VALUE)
;;       clojure.lang.Symbol
;;       
;;   
;;   The job of symbols is to _refer_ to things, to _point_ to other values. When evaluating a program, symbols are looked up and replaced by their corresponding values. That’s not the only use of symbols, but it’s the most common.
;;   
;;   [Keywords](#keywords)
;;   ---------------------
;;   
;;   Closely related to symbols and strings are _keywords_, which begin with a `:`. Keywords are like strings in that they’re made up of text, but are specifically intended for use as _labels_ or _identifiers_. These _aren’t_ labels in the sense of symbols: keywords aren’t replaced by any other value. They’re just names, by themselves.
;;   
;;       user=> 
  (type :cat)

;;       clojure.lang.Keyword
;;       user=> 
  (str :cat)
;;       ":cat"
;;       user=> 
  (name :cat)
;;       "cat"
;;       
;;   
;;   As labels, keywords are most useful when paired with other values in a collection, like a _map_. Keywords can also be used as verbs to _look up specific values_ in other data types. We’ll learn more about keywords shortly.
;;   
;;   [Lists](#lists)
;;   ---------------
;;   
;;   A collection is a group of values. It’s a _container_ which provides some structure, some framework, for the things that it holds. We say that a collection contains _elements_, or _members_. We saw one kind of collection–a list–in the previous chapter.
;;   
;;       user=> 
  '(1 2 3)
;;       (1 2 3)
;;       user=> 
  (type '(1 2 3))
;;       clojure.lang.PersistentList
;;       
;;   
;;   Remember, we _quote_ lists with a `'` to prevent them from being evaluated. You can also construct a list using `list`:
;;   
;;       user=> 
  (list 1 2 3)
;;       (1 2 3)
;;       
;;   
;;   Lists are comparable just like every other value:
;;   
;;       user=> 
  (= (list 1 2) (list 1 2))
;;       true
;;       
  (= '(1 2) (list 1 2))
;;   
;;  (= (1 2) (list 1 2)) => Excecution error

;;   You can modify a list by `conj`oining an element onto it:
;;   
;;       user=> 
  '(1 2 3)
  (conj '(1 2 3) 4)

;;       (4 1 2 3)
;;       
;;   
;;   We added 4 to the list–but it appeared at the _front_. Why? Internally, lists are stored as a _chain_ of values: each link in the chain is a tiny box which holds the value and a connection to the next link. This data structure, called a linked list, offers immediate access to the first element.
;;   
;;       user=> 
  (first (list 1 2 3))
;;       1
;;       
  (first (list nil 2 3))
;;   
  (first (list 'abc 2 3))

  (first (list))

  (not (first (list)))

;;   But getting to the second element requires an extra hop down the chain
;;   
;;       user=> (second (list 1 2 3))
;;       2
;;       
;;   
;;   and the third element a hop after that, and so on.
;;   
;;       user=> 
  (nth (list 1 2 3) 0)
  (nth (list 1 2 3) 2)
;;       3
;;       
;;   
;;   `nth` gets the element of an ordered collection at a particular _index_. The first element is index 0, the second is index 1, and so on.
;;   
;;   This means that lists are well-suited for small collections, or collections which are read in linear order, but are slow when you want to get arbitrary elements from later in the list. For fast access to every element, we use a _vector_.
;;   
;;   [Vectors](#vectors)
;;   -------------------
;;   
;;   Vectors are surrounded by square brackets, just like lists are surrounded by parentheses. Because vectors _aren’t_ evaluated like lists are, there’s no need to quote them:
;;   
;;       user=> 
  [1 2 3]
;;       [1 2 3]
;;       user=> 
  (type [1 2 3])
;;       clojure.lang.PersistentVector
;;       
;;   
;;   You can also create vectors with `vector`, or change other structures into vectors with `vec`:
;;   
;;       user=> 
  (vector 1 2 3)
;;       [1 2 3]
;;       user=> 
  (vec (list 1 2 3))
;;       [1 2 3]
;;       
;;   
;;   `conj` on a vector adds to the _end_, not the _start_:
;;   
;;       user=> 
  (conj [1 2 3] 4)
;;       [1 2 3 4]
;;       
;;   
;;   Our friends `first`, `second`, and `nth` work here too; but unlike lists, `nth` is _fast_ on vectors. That’s because internally, vectors are represented as a very broad tree of elements, where each part of the tree branches into 32 smaller trees. Even very large vectors are only a few layers deep, which means getting to elements only takes a few hops.
;;   
;;   In addition to `first`, you’ll often want to get the _remaining_ elements in a collection. There are two ways to do this:
;;   
;;       user=> 
  (rest [1 2 3])
;;       (2 3)
;;       user=> 
  (next [1 2 3])
;;       (2 3)
;;       
;;   
;;   `rest` and `next` both return “everything but the first element”. They differ only by what happens when there are no remaining elements:
;;   
;;       user=> 
  (rest [1])
;;       ()
;;       user=> 
  (next [1])
;;       nil
;;       
;;   
;;   `rest` returns logical true, `next` returns logical false. Each has their uses, but in almost every case they’re equivalent–I interchange them freely.
;;   
;;   We can get the final element of any collection with `last`:
;;   
;;       user=> (last [1 2 3])
;;       3
;;       
;;   
;;   And figure out how big the vector is with `count`:
;;   
;;       user=> (count [1 2 3])
;;       3
;;       
;;   
;;   Because vectors are intended for looking up elements by index, we can also use them directly as _verbs_:
;;   
;;       user=> ([:a :b :c] 1)
;;       :b
;;       
;;   
;;   So we took the vector containing three keywords, and asked “What’s the element at index 1?” Lisp, like most (but not all!) modern languages, counts up from _zero_, not one. Index 0 is the first element, index 1 is the second element, and so on. In this vector, finding the element at index 1 evaluates to `:b`.
;;   
;;   Finally, note that vectors and lists containing the same elements are considered equal in Clojure:
;;   
;;       user=> (= '(1 2 3) [1 2 3])
;;       true
;;       
;;   
;;   In almost all contexts, you can consider vectors, lists, and other sequences as interchangeable. They only differ in their performance characteristics, and in a few data-structure-specific operations.
;;   
;;   [Sets](#sets)
;;   -------------
;;   
;;   Sometimes you want an unordered collection of values; especially when you plan to ask questions like “does the collection have the number 3 in it?” Clojure, like most languages, calls these collections _sets_.
;;   
;;       user=> #{:a :b :c}
;;       #{:a :c :b}
;;       
;;   
;;   Sets are surrounded by `#{...}`. Notice that though we gave the elements `:a`, `:b`, and `:c`, they came out in a different order. In general, the order of sets can shift at any time. If you want a particular order, you can ask for it as a list or vector:
;;   
;;       user=> (vec #{:a :b :c})
;;       [:a :c :b]
;;       
;;   
;;   Or ask for the elements in sorted order:
;;   
;;       (sort #{:a :b :c})
;;       (:a :b :c)
;;       
;;   
;;   `conj` on a set adds an element:
;;   
;;       user=> (conj #{:a :b :c} :d)
;;       #{:a :c :b :d}
;;       user=> (conj #{:a :b :c} :a)
;;       #{:a :c :b}
;;       
;;   
;;   Sets never contain an element more than once, so `conj`ing an element which is already present does nothing. Conversely, one removes elements with `disj`:
;;   
;;       user=> (disj #{"hornet" "hummingbird"} "hummingbird")
;;       #{"hornet"}
;;       
;;   
;;   The most common operation with a set is to check whether something is inside it. For this we use `contains?`.
;;   
;;       user=> (contains? #{1 2 3} 3)
;;       true
;;       user=> (contains? #{1 2 3} 5)
;;       false
;;       
;;   
;;   Like vectors, you can use the set _itself_ as a verb. Unlike `contains?`, this expression returns the element itself (if it was present), or `nil`.
;;   
;;       user=> (#{1 2 3} 3)
;;       3
;;       user=> (#{1 2 3} 4)
;;       nil
;;       
;;   
;;   You can make a set out of any other collection with `set`.
;;   
;;       user=> (set [:a :b :c])
;;       #{:a :c :b}
;;       
;;   
;;   [Maps](#maps)
;;   -------------
;;   
;;   The last collection on our tour is the _map_: a data structure which associates _keys_ with _values_. In a dictionary, the keys are words and the definitions are the values. In a library, keys are call signs, and the books are values. Maps are indexes for looking things up, and for representing different pieces of named information together. Here’s a cat:
;;   
;;       user=> {:name "mittens" :weight 9 :color "black"}
;;       {:weight 9, :name "mittens", :color "black"}
;;       
;;   
;;   Maps are surrounded by braces `{...}`, filled by alternating keys and values. In this map, the three keys are `:name`, `:color`, and `:weight`, and their values are `"mittens"`, `"black"`, and 9, respectively. We can look up the corresponding value for a key with `get`:
;;   
;;       user=> (get {"cat" "meow" "dog" "woof"} "cat")
;;       "meow"
;;       user=> (get {:a 1 :b 2} :c)
;;       nil
;;       
;;   
;;   `get` can also take a _default_ value to return instead of nil, if the key doesn’t exist in that map.
;;   
;;       user=> (get {:glinda :good} :wicked :not-here)
;;       :not-here
;;       
;;   
;;   Since lookups are so important for maps, we can use a map as a verb directly:
;;   
;;       user=> ({"amlodipine" 12 "ibuprofen" 50} "ibuprofen")
;;       50
;;       
;;   
;;   And conversely, keywords can _also_ be used as verbs, which look themselves up in maps:
;;   
;;       user=> (:raccoon {:weasel "queen" :raccoon "king"})
;;       "king"
;;       
;;   
;;   You can add a value for a given key to a map with `assoc`.
;;   
;;       user=> (assoc {:bolts 1088} :camshafts 3)
;;       {:camshafts 3 :bolts 1088}
;;       user=> (assoc {:camshafts 3} :camshafts 2)
;;       {:camshafts 2}
;;       
;;   
;;   Assoc adds keys if they aren’t present, and _replaces_ values if they’re already there. If you associate a value onto `nil`, it creates a new map.
;;   
;;       user=> (assoc nil 5 2)
;;       {5 2}
;;       
;;   
;;   You can combine maps together using `merge`, which yields a map containing all the elements of _all_ given maps, preferring the values from later ones.
;;   
;;       user=> (merge {:a 1 :b 2} {:b 3 :c 4})
;;       {:c 4, :a 1, :b 3}
;;       
;;   
;;   Finally, to remove a value, use `dissoc`.
;;   
;;       user=> (dissoc {:potatoes 5 :mushrooms 2} :mushrooms)
;;       {:potatoes 5}
;;       
;;   
;;   [Putting it all together](#putting-it-all-together)
;;   ---------------------------------------------------
;;   
;;   All these collections and types can be combined freely. As software engineers, we model the world by creating a particular _representation_ of the problem in the program. Having a rich set of values at our disposal allows us to talk about complex problems. We might describe a person:
;;   
;;       {:name "Amelia Earhart"
;;        :birth 1897
;;        :death 1939
;;        :awards {"US"    #{"Distinguished Flying Cross" "National Women's Hall of Fame"}
;;                 "World" #{"Altitude record for Autogyro" "First to cross Atlantic twice"}}}
;;       
;;   
;;   Or a recipe:
;;   
;;       {:title "Chocolate chip cookies"
;;        :ingredients {"flour"           [(+ 2 1/4) :cup]
;;                      "baking soda"     [1   :teaspoon]
;;                      "salt"            [1   :teaspoon]
;;                      "butter"          [1   :cup]
;;                      "sugar"           [3/4 :cup]
;;                      "brown sugar"     [3/4 :cup]
;;                      "vanilla"         [1   :teaspoon]
;;                      "eggs"            2
;;                      "chocolate chips" [12  :ounce]}}
;;       
;;   
;;   Or the Gini coefficients of nations, as measured over time:
;;   
;;       {"Afghanistan" {2008 27.8}
;;        "Indonesia"   {2008 34.1 2010 35.6 2011 38.1}
;;        "Uruguay"     {2008 46.3 2009 46.3 2010 45.3}}
;;       
;;   
;;   In Clojure, we _compose_ data structures to form more complex values; to talk about bigger ideas. We use operations like `first`, `nth`, `get`, and `contains?` to extract specific information from these structures, and modify them using `conj`, `disj`, `assoc`, `dissoc`, and so on.
;;   
;;   We started this chapter with a discussion of _types_: groups of similar objects which obey the same rules. We learned that bigints, longs, ints, shorts, and bytes are all integers, that doubles and floats are approximations to decimal numbers, and that ratios represent fractions exactly. We learned the differences between strings for text, symbols as references, and keywords as short labels. Finally, we learned how to compose, alter, and inspect collections of elements. Armed with the basic nouns of Clojure, we’re ready to write a broad array of programs.
;;   
;;   I’d like to conclude this tour with one last type of value. We’ve inspected dozens of types so far–but what happens when you turn the camera on itself?
;;   
;;       user=> (type type)
;;       clojure.core$type
;;       
;;   
;;   What _is_ this `type` thing, exactly? What _are_ these verbs we’ve been learning, and where do they come from? This is the central question of [chapter three: functions](http://aphyr.com/posts/303-clojure-from-the-ground-up-functions).
;;   
;;   ![Kevin Whitefoot](https://www.gravatar.com/avatar/0562351d3bdfca86c83600f5c7401a67?r=pg&s=96&d=identicon "Kevin Whitefoot")
;;   
;;   Kevin Whitefoot on [2013-10-27](/posts/302-clojure-from-the-ground-up-basic-types#comment-1688)
;;   
;;   This is a nice gentle introduction. I would like to take issue with one thing, something that annoys me often: -263 is not the smallest possible Long, it is the most negative. The comparison operators do not test for size but for order.
;;   
;;   It might sound like nit-picking but I encountered a bug quite recently that hinged on this distinction where a function was rounding a number and the comments said that the result would always be greater than or equal to the input when what the code actually did was to ensure that the result was never more negative than the input. This was in VB.Net but the principle is the same for all the languages with which I am familiar. Documentation for all the languages I know use greater and less than in this jargon way which misleads people when they apply code to the real world (I write simulation software).
;;   
;;   ![Tim Condit](https://www.gravatar.com/avatar/738f76468b85820f2c3e84f4bc26d076?r=pg&s=96&d=identicon "Tim Condit")
;;   
;;   Tim Condit on [2013-10-27](/posts/302-clojure-from-the-ground-up-basic-types#comment-1690)
;;   
;;   I don’t understand the statement and result that ‘or’ returns the first positive value.
;;   
;;   user=> (or false nil) nil
;;   
;;   Earlier you showed that the only negative values are false and nil.
;;   
;;   Great stuff so far! I’m looking forward to more.
;;   
;;   ![Tim Condit](https://www.gravatar.com/avatar/738f76468b85820f2c3e84f4bc26d076?r=pg&s=96&d=identicon "Tim Condit")
;;   
;;   Tim Condit on [2013-10-27](/posts/302-clojure-from-the-ground-up-basic-types#comment-1692)
;;   
;;   Ah. The result is nil, meaning there were no positive values in the list being evaluated. Never mind. :)
;;   
;;   ![Branden Rolston](https://www.gravatar.com/avatar/49d8f54f43fa381bfa383748dc152f4c?r=pg&s=96&d=identicon "Branden Rolston")
;;   
;;   [Branden Rolston](https://branden.info) on [2013-10-27](/posts/302-clojure-from-the-ground-up-basic-types#comment-1694)
;;   
;;   ^ TIm:
;;   
;;   “or” will return the last value in the list if no preceding values are logically true.
;;   
;;   > (or false false nil) nil (or false false false) false (or false false true) true (or false false 0) 0
;;   
;;   [http://clojuredocs.org/clojure\_core/clojure.core/or](http://clojuredocs.org/clojure_core/clojure.core/or)
;;   
;;   ![Johnny Chang](https://www.gravatar.com/avatar/2d22b5145dc063d32010f0905e2b0078?r=pg&s=96&d=identicon "Johnny Chang")
;;   
;;   [Johnny Chang](http://johnnychang.com) on [2013-10-27](/posts/302-clojure-from-the-ground-up-basic-types#comment-1696)
;;   
;;   @Tim Condit, ‘or’ returns the last negative value if there are no positive values, I believe.
;;   
;;   I love this series. Written engagingly, fun to read, and organized really well. Looking forward to the next one!
;;   
;;   ![Charles](https://www.gravatar.com/avatar/73c82e246b10ecf36ccfbcf512d0eeea?r=pg&s=96&d=identicon "Charles")
;;   
;;   Charles on [2013-10-28](/posts/302-clojure-from-the-ground-up-basic-types#comment-1704)
;;   
;;   Hey I’m just getting started with Clojure right now and this is awesome!
;;   
;;   Ibuprofen is spelled with an “e”, though :)
;;   
;;   ![](https://www.gravatar.com/avatar/294de3557d9d00b3d2d8a1e6aab028cf?r=pg&s=96&d=identicon)
;;   
;;   anonymous on [2013-10-29](/posts/302-clojure-from-the-ground-up-basic-types#comment-1705)
;;   
;;   really nice introduction, thx!
;;   
;;   ![Phil](https://www.gravatar.com/avatar/22788ec68b2aee512f8f4c5d8ae819ae?r=pg&s=96&d=identicon "Phil")
;;   
;;   [Phil](http://technomancy.us) on [2013-10-30](/posts/302-clojure-from-the-ground-up-basic-types#comment-1707)
;;   
;;   Nice article; does a good job of surveying the lay of the land. One correction though:
;;   
;;   > Every symbol actually has two names: one, a short name, is used to refer to things locally. Another is the fully qualified name, which is used to refer unambiguously to a symbol from anywhere.
;;   
;;   Actually some symbols are qualified and some aren’t. Unqualified symbols are typically resolved to vars which do have namespaces, but that connection comes from the ns-map of the namespace under which evaluation is performed and isn’t a property of the symbol.
;;   
;;   Aphyr on [2013-11-04](/posts/302-clojure-from-the-ground-up-basic-types#comment-1708)
;;   
;;   Thanks Phil. Was playing a little fast and loose there with qualified names, but I think you’re right, it does make sense to introduce the dual symbol resolution here instead. Vars come in Chapter 3.
;;   
;;   ![Aphyr](https://www.gravatar.com/avatar/e145b50faf662e70c066b13c98921900?r=pg&s=96&d=identicon "Aphyr")
;;   
;;   ![Alex Moore](https://www.gravatar.com/avatar/a98830a754660303ef35d0e8f4bd3cd0?r=pg&s=96&d=identicon "Alex Moore")
;;   
;;   [Alex Moore](https://alexmoore.io) on [2013-11-15](/posts/302-clojure-from-the-ground-up-basic-types#comment-1741)
;;   
;;   You’re missing a `}` on the end of your recipe.
;;   
;;   Thanks for the tutorial
;;   
;;   ![Adit](https://www.gravatar.com/avatar/31f27c74af13e18ea378df4e91359596?r=pg&s=96&d=identicon "Adit")
;;   
;;   Adit on [2013-12-19](/posts/302-clojure-from-the-ground-up-basic-types#comment-1784)
;;   
;;   You use `type` everywhere but `class` in `(class 'str)`. Is there a reason for this?
;;   
;;   ![Fabian](https://www.gravatar.com/avatar/0af94f58b3e30ecebb9de664dfd5c810?r=pg&s=96&d=identicon "Fabian")
;;   
;;   Fabian on [2014-02-05](/posts/302-clojure-from-the-ground-up-basic-types#comment-1819)
;;   
;;   Very nice guide! So far it helped with my understanding of clojure!
;;   
;;   I have a similar question as Adit. What is the difference between class and type? So far i haven’t found anything where they have a different return value.
;;   
;;   From the documentation i got: “Returns the :type metadata of x, or its Class if none” When has something a :type metadata?
;;   
;;   ![eGerula](https://www.gravatar.com/avatar/c417ac4be11346e7249a5968f47f8ded?r=pg&s=96&d=identicon "eGerula")
;;   
;;   [eGerula](https://none) on [2014-03-05](/posts/302-clojure-from-the-ground-up-basic-types#comment-1836)
;;   
;;   class - “Returns the Class of x” type - “Returns the :type metadata of x, or its Class if none” Therefore, type does what class does when metadata of the x doesn’t contain a :type. Also, (source type) shows you the body of the type as: (or (get (meta x) :type) (class x)))
;;   
;;   ![eGerula](https://www.gravatar.com/avatar/c417ac4be11346e7249a5968f47f8ded?r=pg&s=96&d=identicon "eGerula")
;;   
;;   eGerula on [2014-03-05](/posts/302-clojure-from-the-ground-up-basic-types#comment-1837)
;;   
;;   Back again to (source class) when you can see included something like: {:added “1.0” :static true}. This is metadata and looks like a map. In this example it has 2 keys, :added and :static. In some functions declarations you can find the :type key also in metadata. You can find an example here: [http://clojuredocs.org/clojure\_core/clojure.core/type](http://clojuredocs.org/clojure_core/clojure.core/type)
;;   
;;   ![Brendan](https://www.gravatar.com/avatar/b80b0b3f9bfd5804ab03f8ca365bb7f1?r=pg&s=96&d=identicon "Brendan")
;;   
;;   [Brendan](http://www.kuripai.com) on [2014-11-09](/posts/302-clojure-from-the-ground-up-basic-types#comment-1981)
;;   
;;   These tutorials are amazing. Thank you for all the time you’re clearly putting into them.
;;   
;;   ![Zhangjing](https://www.gravatar.com/avatar/25ca4a9012e81ffc56321b295eb060b8?r=pg&s=96&d=identicon "Zhangjing")
;;   
;;   Zhangjing on [2014-12-08](/posts/302-clojure-from-the-ground-up-basic-types#comment-1992)
;;   
;;   Really wonderful tutorial !!!
;;   
;;   ![Martijn](https://www.gravatar.com/avatar/a4cae679f8986fad8e3883c60b1f27ad?r=pg&s=96&d=identicon "Martijn")
;;   
;;   Martijn on [2015-07-30](/posts/302-clojure-from-the-ground-up-basic-types#comment-2472)
;;   
;;   One of the best reads on Clojure I’ve seen. Hope you’ll finish the book some day.. would definitely buy it.
;;   
;;   ![AgasthyaH](https://www.gravatar.com/avatar/915d7b8e588ea5f3a38f6873d89b8df7?r=pg&s=96&d=identicon "AgasthyaH")
;;   
;;   AgasthyaH on [2015-08-09](/posts/302-clojure-from-the-ground-up-basic-types#comment-2477)
;;   
;;   Thank you for the excellent and invaluable series. Just wanted to point put a philosophical irony ( unrelated to your series ). That is that most sources which teach about functional programming paradigm use a procedural paradigm :)
;;   
;;   ![Michael](https://www.gravatar.com/avatar/2d7e8083d5537ebddbecd71d02f724c6?r=pg&s=96&d=identicon "Michael")
;;   
;;   Michael on [2016-01-25](/posts/302-clojure-from-the-ground-up-basic-types#comment-2604)
;;   
;;   In the tutorial you state, “Similarly, or returns the first positive value.”
;;   
;;   Is it more accurate to say, “or returns the first true value” because
;;   
;;   user=> (or -1 1) -1
;;   
;;   ![Makro](https://www.gravatar.com/avatar/294de3557d9d00b3d2d8a1e6aab028cf?r=pg&s=96&d=identicon "Makro")
;;   
;;   Makro on [2020-08-25](/posts/302-clojure-from-the-ground-up-basic-types#comment-3193)
;;   
;;   ‘what what’ -> ‘what’
;;   
;;   Thanks for the article.
;;   
;;   ![Claire Jordan](https://www.gravatar.com/avatar/e9054856ba2d25b114780f869cda9c59?r=pg&s=96&d=identicon "Claire Jordan")
;;   
;;   [Claire Jordan](https://www.whitehound.co.uk) on [2020-11-01](/posts/302-clojure-from-the-ground-up-basic-types#comment-3253)
;;   
;;   “Tabby” is a bad example to choose, because it’s flatly untrue that all tabbies are housecats. Many small wild species of cat are tabby, and so are king cheetahs. Steer clear of Siamese and Himalayan as well because mice and rats come in these colours. You could say “All Japanese bobtails are housecats” as afaik no other animal is called a Japanese bobtail.
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
;;   var \_gaq = \_gaq || \[\]; \_gaq.push(\['\_setAccount', 'UA-9527251-1'\]); \_gaq.push(\['\_trackPageview'\]); (function() { var ga = document.createElement('script'); ga.type = 'text/javascript'; ga.async = true; ga.src = ('https:' == document.locationotocol ? 'https://ssl' : 'http://www') + '.google-analytics.com/ga.js'; var s = document.getElementsByTagName('script')\[0\]; s.parentNode.insertBefore(ga, s); })();
;;   
;;  

  )

(comment
  ;; = re-matches - Example 1 = 
  
  ;; The distinction is that re-find tries to find _any part_ of the string
  ;; that matches the pattern, but re-matches only matches if the _entire_
  ;; string matches the pattern.
  (re-matches #"hello" "hello, world")
 ;; nil
  
  (re-matches #"hello.*" "hello, world")
  ;;"hello, world"
  
  (re-matches #"hello, (.*)" "hello, world")
  ;;["hello, world" "world"]
  
  ;; See also:
  clojure.core/re-find
  clojure.core/subs
  clojure.core/re-groups
  clojure.core/re-pattern
  :rcf)

