  Clojure from the ground up: logistics     window.dataLayer = window.dataLayer || \[\]; function gtag(){dataLayer.push(arguments);} gtag('js', new Date()); gtag('config', 'G-MXDP37S6QL');

    

*   [Aphyr](/)
*   [About](/about)
*   [Blog](/posts)
*   [Photos](/photos)
*   [Code](http://github.com/aphyr)

[Clojure from the ground up: logistics](/posts/311-clojure-from-the-ground-up-logistics)
========================================================================================

[Software](/tags/software) [Clojure](/tags/clojure) [Clojure from the ground up](/tags/clojure-from-the-ground-up)

2014-02-15

_Previously, we covered [state and mutability](http://aphyr.com/posts/306-clojure-from-the-ground-up-state)._

Up until now, we’ve been programming primarily at the REPL. However, the REPL is a limited tool. While it lets us explore a problem interactively, that interactivity comes at a cost: changing an expression requires retyping the entire thing, editing multi-line expressions is awkward, and our work vanishes when we restart the REPL–so we can’t share our programs with others, or run them again later. Moreover, programs in the REPL are hard to organize. To solve large problems, we need a way of writing programs _durably_–so they can be read and evaluated later.

In addition to the code itself, we often want to store _ancillary_ information. _Tests_ verify the correctness of the program. _Resources_ like precomputed databases, lookup tables, images, and text files provide other data the program needs to run. There may be _documentation_: instructions for how to use and understand the software. A program may also depend on code from _other_ programs, which we call _libraries_, _packages_, or _dependencies_. In Clojure, we have a standardized way to bind together all these parts into a single directory, called a _project_.

[Project structure](#project-structure)
---------------------------------------

We created a project at the start of this book by using Leiningen, the Clojure project tool.

    $ lein new scratch
    

`scratch` is the name of the project, and also the name of the directory where the project’s files live. Inside the project are a few files.

    $ cd scratch; ls
    doc  project.clj  README.md  resources  src  target  test
    

`project.clj` defines the project: its name, its version, dependencies, and so on. Notice the name of the project (`scratch`) comes first, followed by the version (`0.1.0-SNAPSHOT`). `-SNAPSHOT` versions are for development; you can change them at any time, and any projects which depend on the snapshot will pick up the most recent changes. A version which does _not_ end in `-SNAPSHOT` is fixed: once published, it always points to the same version of the project. This allows projects to specify precisely which projects they depend on. For example, scratch’s `project.clj` says scratch depends on `org.clojure/clojure` version `1.5.1`.

    (defproject scratch "0.1.0-SNAPSHOT"
      :description "FIXME: write description"
      :url "http://example.com/FIXME"
      :license {:name "Eclipse Public License"
                :url "http://www.eclipse.org/legal/epl-v10.html"}
      :dependencies [[org.clojure/clojure "1.5.1"] ])
    

`README.md` is the first file most people open when they look at a new project, and Lein generates a generic readme for you to fill in later. We call this kind of scaffolding or example a “stub”; it’s just there to remind you what sort of things to write yourself. You’ll notice the readme includes the name of the project, some notes on what it does and how to use it, a copyright notice where your name should go, and a license, which sets the legal terms for the use of the project. By default, Leiningen suggests the Eclipse Public License, which allows everyone to use and modify the software, so long as they preserve the license information.

The `doc` directory is for documentation; sometimes hand-written, sometimes automatically generated from the source code. `resources` is for additional files, like images. `src` is where Clojure code lives, and `test` contains the corresponding tests. Finally, `target` is where Leiningen stores compiled code, built packages, and so on.

[Namespaces](#namespaces)
-------------------------

Every lein project starts out with a stub namespace containing a simple function. Let’s take a look at that namespace now–it lives in `src/scratch/core.clj`:

    (ns scratch.core)
    
    (defn foo
      "I don't do a whole lot."
      [x]
      (println x "Hello, World!"))
    

The first part of this file defines the _namespace_ we’ll be working in. The `ns` macro lets the Clojure compiler know that all following code belongs in the `scratch.core` namespace. Remember, `scratch` is the name of our project. `scratch.core` is for the core functions and definitions of the scratch project. As projects expand, we might add new namespaces to _separate_ our work into smaller, more understandable pieces. For instance, Clojure’s primary functions live in `clojure.core`, but there are auxiliary functions for string processing in `clojure.string`, functions for interoperating with Java’s input-output system in `clojure.java.io`, for printing values in `clojure.pprint`, and so on.

`def`, `defn`, and peers always work in the scope of a particular _namespace_. The function `foo` in `scratch.core` is _different_ from the function `foo` in `scratch.pad`.

    scratch.foo=> (ns scratch.core)
    nil
    scratch.core=> (def foo "I'm in core")
    #'scratch.core/foo
    scratch.core=> (ns scratch.pad)
    nil
    scratch.pad=> (def foo "I'm in pad!")
    #'scratch.pad/foo
    

Notice the full names of these vars are different: `scratch.core/foo` vs `scratch.pad/foo`. You can always refer to a var by its fully qualified name: the namespace, followed by a slash `/`, followed by the short name.

Inside a namespace, symbols resolve to variables which are defined in that namespace. So in `scratch.pad`, `foo` refers to `scratch.pad/foo`.

    scratch.pad=> foo
    "I'm in pad!"
    

Namespaces automatically include `clojure.core` by default; which is where all the standard functions, macros, and special forms come from. `let`, `defn`, `filter`, `vector`, etc: all live in `clojure.core`, but are automatically _included_ in new namespaces so we can refer to them by their short names.

Notice that the values for `foo` we defined in `scratch.pad` and `scratch.core` aren’t available in other namespaces, like `user`.

    scratch.pad=> (ns user)
    nil
    user=> foo
    
    CompilerException java.lang.RuntimeException: Unable to resolve symbol: foo in this context, compiling:(NO_SOURCE_PATH:1:602)
    

To access things from other namespaces, we have to _require_ them in the namespace definition.

    user=> (ns user (:require [scratch.core]))
    nil
    user=> scratch.core/foo
    "I'm in core"
    

The `:require` part of the `ns` declaration told the compiler that the `user` namespace needed access to `scratch.core`. We could then refer to the fully qualified name `scratch.core/foo`.

Often, writing out the full namespace is cumbersome–so you can give a short alias for a namespace like so:

    user=> (ns user (:require [scratch.core :as c]))
    nil
    user=> c/foo
    "I'm in core"
    

The `:as` directive indicates that anywhere we write `c/something`, the compiler should expand that to `scratch.core/something`. If you plan on using a var from another namespace often, you can _refer_ it to the local namespace–which means you may omit the namespace qualifier entirely.

    user=> (ns user (:require [scratch.core :refer [foo]]))
    nil
    user=> foo
    "I'm in core"
    

You can refer functions into the current namespace by listing them: `[foo bar ...]`. Alternatively, you can suck in _every_ function from another namespace by saying `:refer :all`:

    user=> (ns user (:require [scratch.core :refer :all]))
    nil
    user=> foo
    "I'm in core"
    

Namespaces _control complexity_ by isolating code into more understandable, related pieces. They make it easier to read code by keeping similar things together, and unrelated things apart. By making dependencies between namespaces explicit, they make it clear how groups of functions relate to one another.

If you’ve worked with Erlang, Modula-2, Haskell, Perl, or ML, you’ll find namespaces analogous to _modules_ or _packages_. Namespaces are often large, encompassing hundreds of functions; and most projects use only a handful of namespaces.

By contrast, object-oriented programming languages like Java, Scala, Ruby, and Objective C organize code in _classes_, which combine _names_ and _state_ in a single construct. Because all functions in a class operate on the same state, object-oriented languages tend to have _many_ classes with _fewer_ functions in each. It’s not uncommon for a typical Java project to define hundreds or thousands of classes containing only one or two functions each. If you come from an object-oriented language, it can feel a bit unusual to combine so many functions in a single scope–but because functional programs isolate state differently, this is _normal_. If, on the other hand, you move _to_ an object-oriented language after Clojure, remember that OO languages compose differently. Objects with hundreds of functions are usually considered unwieldy and should be split into smaller pieces.

[Code and tests](#code-and-tests)
---------------------------------

It’s perfectly fine to test small programs in the REPL. We’ve written and refined hundreds of functions that way: by calling the function and seeing what happens. However, as programs grow in scope and complexity, testing them by hand becomes harder and harder. If you change the behavior of a function which ten other functions rely on, you may have to re-test _all ten_ by hand. In real programs, a small change can alter thousands of distinct behaviors, all of which should be verified.

Wherever possible, we want to _automate_ software tests–making the test itself _another program_. If the test suite runs in a matter of seconds, we can make changes freely–re-running the tests continuously to verify that everything still works.

As a simple example, let’s write and test a single function in `src/scratch/core.clj`. How about exponentiation–raising a number to the given power?

    (ns scratch.core)
    
    (defn pow
      "Raises base to the given power. For instance, (pow 3 2) returns three squared, or nine."
      [base power]
      (apply * (repeat base power)))
    

So we _repeat_ the base _power_ times, then call `*` with that sequence of bases to multiply them all together. Seems straightforward enough. Now we need to test it.

By default, all lein projects come with a simple test stub. Let’s see it in action by running `lein test`.

    aphyr@waterhouse:~/scratch$ lein test
    
    lein test scratch.core-test
    
    lein test :only scratch.core-test/a-test
    
    FAIL in (a-test) (core_test.clj:7)
    FIXME, I fail.
    expected: (= 0 1)
      actual: (not (= 0 1))
    
    Ran 1 tests containing 1 assertions.
    1 failures, 0 errors.
    Tests failed.
    

A _failure_ is when a test returns the wrong value. An _error_ is when a test throws an exception. In this case, the test named `a-test`, in the file `core_test.clj`, on line 7, failed. That test expected zero to be equal to one–but found that 0 and 1 were (in point of fact) not equal. Let’s take a look at that test, in `test/scratch/core_test.clj`.

    (ns scratch.core-test
      (:require [clojure.test :refer :all]
                [scratch.core :refer :all]))
    
    (deftest a-test
      (testing "FIXME, I fail."
        (is (= 0 1))))
    

These tests live in a namespace too! Notice that namespaces and file names match up: `scratch.core` lives in `src/scratch/core.clj`, and `scratch.core-test` lives in `test/scratch/core_test.clj`. Dashes (`-`) in namespaces correspond to underscores (`_`) in filenames, and dots (`.`) correspond to directory separators (`/`).

The `scratch.core-test` namespace is responsible for testing things in `scratch.core`. Notice that it requires two namespaces: `clojure.test`, which provides testing functions and macros, and `scratch.core`, which is the namespace we want to test.

Then we define a test using `deftest`. `deftest` takes a symbol as a test name, and then any number of expressions to evaluate. We can use `testing` to split up tests into smaller pieces. If a test fails, `lein test` will print out the enclosing `deftest` and `testing` names, to make it easier to figure out what went wrong.

Let’s change this test so that it passes. 0 should equal 0.

    (deftest a-test
      (testing "Numbers are equal to themselves, right?"
        (is (= 0 0))))
    

    aphyr@waterhouse:~/scratch$ lein test
    
    lein test scratch.core-test
    
    Ran 1 tests containing 1 assertions.
    0 failures, 0 errors.
    

Wonderful! Now let’s test the `pow` function. I like to start with a really basic case and work my way up to more complicated ones. 1^1 is 1, so:

    (deftest pow-test
      (testing "unity"
        (is (= 1 (pow 1 1)))))
    

    aphyr@waterhouse:~/scratch$ lein test
    
    lein test scratch.core-test
    
    Ran 1 tests containing 1 assertions.
    0 failures, 0 errors.
    

Excellent. How about something harder?

    (deftest pow-test
      (testing "unity"
        (is (= 1 (pow 1 1))))
      
      (testing "square integers"
        (is (= 9 (pow 3 2)))))
    

    aphyr@waterhouse:~/scratch$ lein test
    
    lein test scratch.core-test
    
    lein test :only scratch.core-test/pow-test
    
    FAIL in (pow-test) (core_test.clj:10)
    square integers
    expected: (= 9 (pow 3 2))
      actual: (not (= 9 8))
    
    Ran 1 tests containing 2 assertions.
    1 failures, 0 errors.
    Tests failed.
    

That’s odd. 3^2 should be 9, not 8. Let’s double-check our code in the REPL. `base` was 3, and `power` was 2, so…

    user=> (repeat 3 2)
    (2 2 2)
    user=> (* 2 2 2)
    8
    

Ah, there’s the problem. We’re mis-using `repeat`. Instead of repeating 3 twice, we repeated 2 thrice.

    user=> (doc repeat)
    -------------------------
    clojure.core/repeat
    ([x] [n x])
      Returns a lazy (infinite!, or length n if supplied) sequence of xs.
    

Let’s redefine `pow` with the correct arguments to `repeat`:

    (defn pow
      "Raises base to the given power. For instance, (pow 3 2) returns three
      squared, or nine."
      [base power]
      (apply * (repeat power base)))
    

How about 0^0? By convention, mathematicians define 0^0 as 1.

    (deftest pow-test
      (testing "unity"
        (is (= 1 (pow 1 1))))
    
      (testing "square integers"
        (is (= 9 (pow 3 2))))
      
      (testing "0^0"
        (is (= 1 (pow 0 0)))))
    

    aphyr@waterhouse:~/scratch$ lein test
    
    lein test scratch.core-test
    
    Ran 1 tests containing 3 assertions.
    0 failures, 0 errors.
    

Hey, what do you know? It works! But _why_?

    user=> (repeat 0 0)
    ()
    

What happens when we call `*` with an _empty_ list of arguments?

    user=> (*)
    1
    

Remember when we talked about how the zero-argument forms of `+`, and `*` made some definitions simpler? This is one of those times. We didn’t have to define a special exception for zero powers because `(*)` returns the multiplicative identity 1, by convention.

[Exploring data](#exploring-data)
---------------------------------

The last bit of logistics we need to talk about is _working with other people’s code_. Clojure projects, like most modern programming environments, are built to work together. We can use libraries to parse data, solve mathematical problems, render graphics, perform simulations, talk to robots, or predict the weather. As a quick example, I’d like to imagine that you and I are public-health researchers trying to identify the best location for an ad campaign to reduce drunk driving.

The FBI’s [Uniform Crime Reporting](http://www.fbi.gov/about-us/cjis/ucr/ucr) database tracks the annual tally of different types of arrests, broken down by county–but the data files provided by the FBI are a mess to work with. Helpfully, [Matt Aliabadi](http://emdasheveryone.wordpress.com/) has organized the UCR’s somewhat complex format into nice, normalized files in a data format called JSON, and made them available [on Github](https://github.com/maliabadi/ucr-json). Let’s download the most recent year’s [normalized data](https://raw2.github.com/maliabadi/ucr-json/master/data/parsed/normalized/2008.json), and save it in the `scratch` directory.

What’s _in_ this file, anyway? Let’s take a look at the first few lines using `head`:

    aphyr@waterhouse:~/scratch$ head 2008.json
    [
      {
        "icpsr_study_number": null,
        "icpsr_edition_number": 1,
        "icpsr_part_number": 1,
        "icpsr_sequential_case_id_number": 1,
        "fips_state_code": "01",
        "fips_county_code": "001",
        "county_population": 52417,
        "number_of_agencies_in_county": 3,
    

This is a data format called [JSON](http://json.org/), and it looks a lot like Clojure’s data structures. That’s the start of a vector on the first line, and the second line starts a map. Then we’ve got string keys like `"icpsr_study_number"`, and values which look like `null` (`nil`), numbers, or strings. But in order to _work_ with this file, we’ll need to _parse_ it into Clojure data structures. For that, we can use a JSON parsing library, like [Cheshire](https://github.com/dakrone/cheshire).

Cheshire, like most Clojure libraries, is published on an internet repository called [Clojars](http://clojars.org). To include it in our scratch project, all we have to do is add open `project.clj` in a text editor, and add a line specifying that we want to use a particular version of Cheshire.

    (defproject scratch "0.1.0-SNAPSHOT"
      :description "Just playing around"
      :url "http://example.com/FIXME"
      :license {:name "Eclipse Public License"
                :url "http://www.eclipse.org/legal/epl-v10.html"}
      :dependencies [[org.clojure/clojure "1.5.1"]
                     [cheshire "5.3.1"]])
    

Now we’ll exit the REPL with Control+D (^D), and restart it with `lein repl`. Leiningen, the Clojure package manager, will automatically download Cheshire from Clojars and make it available in the new REPL session.

Now let’s figure out how to parse the JSON file. Looking at [Cheshire’s README](https://github.com/dakrone/cheshire) shows an example that looks helpful:

    ;; parse some json and get keywords back
    (parse-string "{\"foo\":\"bar\"}" true)
    ;; => {:foo "bar"}
    

So Cheshire includes a parse-string function which can take a string and return a data structure. How can we get a string out of a file? Using `slurp`:

    user=> (use 'cheshire.core)
    nil
    user=> (parse-string (slurp "2008.json"))
    ...
    

Woooow, that’s a lot of data! Let’s chop it down to something more manageable. How about the first entry?

    user=> (first (parse-string (slurp "2008.json")))
    {"syntheticdrug_salemanufacture" 1, "all_other_offenses_except_traffic" 900, "arson" 3, ...}
    user=> (-> "2008.json" slurp parse-string first)
    

It’d be nicer if this data used keywords instead of strings for its keys. Let’s use the second argument to Chesire’s `parse-string` to convert all the keys in maps to keywords.

    user=> (first (parse-string (slurp "2008.json") true))
    {:other_assaults 288, :gambling_all_other 0, :arson 3, ... :drunkenness 108}
    

Since we’re going to be working with this dataset over and over again, let’s bind it to a variable for easy re-use.

    user=> (def data (parse-string (slurp "2008.json") true))
    #'user/data
    

Now we’ve got a big long vector of counties, each represented by a map–but we’re just interested in the _DUIs_ of each one. What does that look like? Let’s _map_ each county to its `:driving_under_influence`.

    user=> (->> data (map :driving_under_influence))
    (198 1095 114 98 135 4 122 587 204 53 177 ...
    

What’s the most any county has ever reported?

    user=> (->> data (map :driving_under_influence) (apply max))
    45056
    

45056 counts in one year? Wow! What about the second-worst county? The easiest way to find the _top n_ counties is to _sort_ the list, then look at the final elements.

    user=> (->> data (map :driving_under_influence) sort (take-last 10))
    (8589 10432 10443 10814 11439 13983 17572 18562 26235 45056)
    

So the top 10 counties range from 8549 counts to 45056 counts. What’s the _most common_ count? Clojure comes with a built-in function called `frequencies` which takes a sequence of elements, and returns a map from each element to how many times it appeared in the sequence.

    user=> (->> data (map :driving_under_influence) frequencies)
    {0 227, 1024 1, 45056 1, 32 15, 2080 1, 64 12 ...
    

Now let’s take those \[drunk-driving, frequency\] pairs and sort them by key to produce a _histogram_. `sort-by` takes a function to apply to each element in the collection–in this case, a key-value pair–and returns something that can be sorted, like a number. We’ll choose the `key` function to extract the key from each key-value pair, effectively sorting the counties by number of reported incidents.

    user=> (->> data (map :driving_under_influence) frequencies (sort-by key) pprint)
    ([0 227]
     [1 24]
     [2 17]
     [3 20]
     [4 17]
     [5 24]
     [6 23]
     [7 23]
     [8 17]
     [9 19]
     [10 29]
     [11 20]
     [12 18]
     [13 21]
     [14 25]
     [15 13]
     [16 18]
     [17 16]
     [18 17]
     [19 11]
     [20 8]
     ...
    

So a ton of counties (227 out of 3172 total) report no drunk driving; a few hundred have one incident, a moderate number have 10-20, and it falls off from there. This is a common sort of shape in statistics; often a hallmark of an exponential distribution.

How about the 10 worst counties, all the way out on the end of the curve?

    user=> (->> data (map :driving_under_influence) frequencies (sort-by key) (take-last 10) pprint)
    ([8589 1]
     [10432 1]
     [10443 1]
     [10814 1]
     [11439 1]
     [13983 1]
     [17572 1]
     [18562 1]
     [26235 1]
     [45056 1])
    

So it looks like 45056 is high, but there are 8 other counties with tens of thousands of reports too. Let’s back up to the original dataset, and sort it by DUIs:

    user=> (->> data (sort-by :driving_under_influence) (take-last 10) pprint)
    ({:other_assaults 3096,
      :gambling_all_other 3,
      :arson 106,
      :have_stolen_property 698,
      :syntheticdrug_salemanufacture 0,
      :icpsr_sequential_case_id_number 220,
      :drug_abuse_salemanufacture 1761,
      ...
    

What we’re looking for is the county names, but it’s a little hard to read these enormous maps. Let’s take a look at just the keys that define each county, and see which ones might be useful. We’ll take this list of counties, map each one to a list of its keys, and concatenate those lists together into one big long list. `mapcat` maps and concatenates in a single step. We expect the same keys to show up over and over again, so we’ll remove duplicates by merging all those keys `into` a `sorted-set`.

    user=> (->> data (sort-by :driving_under_influence) (take-last 10) (mapcat keys) (into (sorted-set)) pprint)
    #{:aggravated_assaults :all_other_offenses_except_traffic :arson
      :auto_thefts :bookmaking_horsesport :burglary :county_population
      :coverage_indicator :curfew_loitering_laws :disorderly_conduct
      :driving_under_influence :drug_abuse_salemanufacture
      :drug_abuse_violationstotal :drug_possession_other
      :drug_possession_subtotal :drunkenness :embezzlement
      :fips_county_code :fips_state_code :forgerycounterfeiting :fraud
      :gambling_all_other :gambling_total :grand_total
      :have_stolen_property :icpsr_edition_number :icpsr_part_number
      :icpsr_sequential_case_id_number :icpsr_study_number :larceny
      :liquor_law_violations :marijuana_possession
      :marijuanasalemanufacture :multicounty_jurisdiction_flag :murder
      :number_of_agencies_in_county :numbers_lottery
      :offenses_against_family_child :opiumcocaine_possession
      :opiumcocainesalemanufacture :other_assaults :otherdang_nonnarcotics
      :part_1_total :property_crimes :prostitutioncomm_vice :rape :robbery
      :runaways :sex_offenses :suspicion :synthetic_narcoticspossession
      :syntheticdrug_salemanufacture :vagrancy :vandalism :violent_crimes
      :weapons_violations}
    

Ah, `:fips_county_code` and `:fips_state_code` look promising. Let’s compact the dataset to just drunk driving and those codes, using `select-keys`.

    user=> (->> data (sort-by :driving_under_influence) (take-last 10) (map #(select-keys % [:driving_under_influence :fips_county_code :fips_state_code])) pprint)
    ({:fips_state_code "06",
      :fips_county_code "067",
      :driving_under_influence 8589}
     {:fips_state_code "48",
      :fips_county_code "201",
      :driving_under_influence 10432}
     {:fips_state_code "32",
      :fips_county_code "003",
      :driving_under_influence 10443}
     {:fips_state_code "06",
      :fips_county_code "065",
      :driving_under_influence 10814}
     {:fips_state_code "53",
      :fips_county_code "033",
      :driving_under_influence 11439}
     {:fips_state_code "06",
      :fips_county_code "071",
      :driving_under_influence 13983}
     {:fips_state_code "06",
      :fips_county_code "059",
      :driving_under_influence 17572}
     {:fips_state_code "06",
      :fips_county_code "073",
      :driving_under_influence 18562}
     {:fips_state_code "04",
      :fips_county_code "013",
      :driving_under_influence 26235}
     {:fips_state_code "06",
      :fips_county_code "037",
      :driving_under_influence 45056})
    

Now we’re getting somewhere–but we need a way to interpret these state and county codes. Googling for “FIPS” led me to Wikipedia’s account of the [FIPS county code system](http://en.wikipedia.org/wiki/FIPS_county_code), and the NOAA’s ERDDAP service, which has a table [mapping FIPS codes to state and county names](http://coastwatch.pfeg.noaa.gov/erddap/convert/fipscounty.html). Let’s save that file as [fips.json](http://coastwatch.pfeg.noaa.gov/erddap/convert/fipscounty.json).

Now we’ll slurp that file into the REPL and parse it, just like we did with the crime dataset.

    user=> (def fips (parse-string (slurp "fips.json") true))
    

Let’s take a quick look at the structure of this data:

    user=> (keys fips)
    (:table)
    user=> (keys (:table fips))
    (:columnNames :columnTypes :rows)
    user=> (->> fips :table :columnNames)
    ["FIPS" "Name"]
    

Great, so we expect the rows to be a list of FIPS code and Name.

    user=> (->> fips :table :rows (take 3) pprint)
    (["02000" "AK"]
     ["02013" "AK, Aleutians East"]
     ["02016" "AK, Aleutians West"])
    

Perfect. Now that’s we’ve done some exploratory work in the REPL, let’s shift back to an editor. Create a new file in `src/scratch/crime.clj`:

    (ns scratch.crime
      (:require [cheshire.core :as json]))
    
    (def fips
      "A map of FIPS codes to their county names."
      (->> (json/parse-string (slurp "fips.json") true)
           :table
           :rows
           (into {})))
    

We’re just taking a snippet we wrote in the REPL–parsing the FIPS dataset–and writing it down for use as a part of a bigger program. We use `(into {})` to convert the sequence of `[fips, name]` pairs into a map, just like we used `into (sorted-set)` to merge a list of keywords into a set earlier. `into` works just like `conj`, repeated over and over again, and is an incredibly useful tool for building up collections of things.

Back in the REPL, let’s check if that worked:

    user=> (use 'scratch.crime :reload)
    nil
    user=> (fips "10001")
    "DE, Kent"
    

Remember, maps act like functions in Clojure, so we can use the `fips` map to look up the names of counties efficiently.

We also have to load the UCR crime file–so let’s split that load-and-parse code into its own function:

    (defn load-json
      "Given a filename, reads a JSON file and returns it, parsed, with keywords."
      [file]
      (json/parse-string (slurp file) true))
    
    (def fips
      "A map of FIPS codes to their county names."
      (->> "fips.json"
           load-json
           :table
           :rows
           (into {})))
    

Now we can re-use `load-json` to load the UCR crime file.

    (defn most-duis
      "Given a JSON filename of UCR crime data for a particular year, finds the
      counties with the most DUIs."
      [file]
      (->> file
           load-json
           (sort-by :driving_under_influence)
           (take-last 10)
           (map #(select-keys % [:driving_under_influence
                                 :fips_county_code
                                 :fips_state_code]))))
    

    user=> (use 'scratch.crime :reload) (pprint (most-duis "2008.json"))
    nil
    ({:fips_state_code "06",
      :fips_county_code "067",
      :driving_under_influence 8589}
     {:fips_state_code "48",
      :fips_county_code "201",
      :driving_under_influence 10432}
     {:fips_state_code "32",
      :fips_county_code "003",
      :driving_under_influence 10443}
     {:fips_state_code "06",
      :fips_county_code "065",
      :driving_under_influence 10814}
     {:fips_state_code "53",
      :fips_county_code "033",
      :driving_under_influence 11439}
     {:fips_state_code "06",
      :fips_county_code "071",
      :driving_under_influence 13983}
     {:fips_state_code "06",
      :fips_county_code "059",
      :driving_under_influence 17572}
     {:fips_state_code "06",
      :fips_county_code "073",
      :driving_under_influence 18562}
     {:fips_state_code "04",
      :fips_county_code "013",
      :driving_under_influence 26235}
     {:fips_state_code "06",
      :fips_county_code "037",
      :driving_under_influence 45056})
    

Almost there. We need to join together the state and county FIPS codes into a single string, because that’s how `fips` represents the county code:

    (defn fips-code
      "Given a county (a map with :fips_state_code and :fips_county_code keys),
       returns the five-digit FIPS code for the county, as a string."
      [county]
      (str (:fips_state_code county) (:fips_county_code county)))
    

Let’s write a quick test in `test/scratch/crime_test.clj` to verify that function works correctly:

    (ns scratch.crime-test
      (:require [clojure.test :refer :all]
                [scratch.crime :refer :all]))
    
    (deftest fips-code-test
      (is (= "12345" (fips-code {:fips_state_code "12" :fips_county_code "345"}))))
    

    aphyr@waterhouse:~/scratch$ lein test scratch.crime-test
    
    lein test scratch.crime-test
    
    Ran 1 tests containing 1 assertions.
    0 failures, 0 errors.
    

Great. Note that `lein test some-namespace` runs only the tests in that particular namespace. For the last step, let’s take the `most-duis` function and, using `fips` and `fips-code`, construct a map of county names to DUI reports.

    (defn most-duis
      "Given a JSON filename of UCR crime data for a particular year, finds the
      counties with the most DUIs."
      [file]
      (->> file
           load-json
           (sort-by :driving_under_influence)
           (take-last 10)
           (map (fn [county]
                  [(fips (fips-code county))
                   (:driving_under_influence county)]))
           (into {})))
    

    user=> (use 'scratch.crime :reload) (pprint (most-duis "2008.json"))
    nil
    {"CA, Orange" 17572,
     "CA, San Bernardino" 13983,
     "CA, Los Angeles" 45056,
     "CA, Riverside" 10814,
     "NV, Clark" 10443,
     "WA, King" 11439,
     "AZ, Maricopa" 26235,
     "CA, San Diego" 18562,
     "TX, Harris" 10432,
     "CA, Sacramento" 8589}
    

Our question is, at least in part, answered: Los Angeles and Maricopa counties, in California and Arizona, have the most reports of drunk driving out of any counties in the 2008 Uniform Crime Reporting database. These might be good counties for a PSA campaign. California has either lots of drunk drivers, or aggressive enforcement, or both! Remember, this only tells us about _reports_ of crimes; not the crimes themselves. Numbers vary based on how the state enforces the laws!

    (ns scratch.crime
      (:require [cheshire.core :as json]))
    
    (defn load-json
      "Given a filename, reads a JSON file and returns it, parsed, with keywords."
      [file]
      (json/parse-string (slurp file) true))
    
    (def fips
      "A map of FIPS codes to their county names."
      (->> "fips.json"
           load-json
           :table
           :rows
           (into {})))
    
    (defn fips-code
      "Given a county (a map with :fips_state_code and :fips_county_code keys),
      returns the five-digit FIPS code for the county, as a string."
      [county]
      (str (:fips_state_code county) (:fips_county_code county)))
    
    (defn most-duis
      "Given a JSON filename of UCR crime data for a particular year, finds the
      counties with the most DUIs."
      [file]
      (->> file
           load-json
           (sort-by :driving_under_influence)
           (take-last 10)
           (map (fn [county]
                  [(fips (fips-code county))
                   (:driving_under_influence county)]))
           (into {})))
    

[Recap](#recap)
---------------

In this chapter, we expanded beyond transient programs written in the REPL. We learned how _projects_ combine static resources, code, and tests into a single package, and how projects can relate to one another through _dependencies_. We learned the basics of Clojure’s namespace system, which isolates distinct chunks of code from one another, and how to include definitions from one namespace in another via `require` and `use`. We learned how to write and run _tests_ to verify our code’s correctness, and how to move seamlessly between the repl and code in `.clj` files. We made use of Cheshire, a Clojure library published on Clojars, to parse JSON–a common data format. Finally, we brought together our knowledge of Clojure’s basic grammar, immutable data structures, core functions, sequences, threading macros, and vars to explore a real-world problem.

[Exercises](#exercises)
-----------------------

1.  `most-duis` tells us about the raw number of reports, but doesn’t account for differences in county population. One would naturally expect counties with more people to have more crime! Divide the `:driving_under_influence` of each county by its `:county_population` to find a _prevalence_ of DUIs, and take the top ten counties based on prevalence. How should you handle counties with a population of zero?
    
2.  How do the prevalence counties compare to the original counties? Expand most-duis to return vectors of `[county-name, prevalence, report-count, population]` What are the populations of the high-prevalence counties? Why do you suppose the data looks this way? If you were leading a public-health campaign to reduce drunk driving, would you target your intervention based on _report count_ or _prevalence_? Why?
    
3.  We can _generalize_ the `most-duis` function to handle _any_ type of crime. Write a function `most-prevalent` which takes a file and a field name, like `:arson`, and finds the counties where that field is most often reported, per capita.
    
4.  Write a test to verify that `most-prevalent` is correct.
    

Next up: [modeling](https://aphyr.com/posts/312-clojure-from-the-ground-up-modeling).

![Jeremy](https://www.gravatar.com/avatar/a198879c03effd0d1e0cd73b5b38b751?r=pg&s=96&d=identicon "Jeremy")

Jeremy on [2014-02-24](/posts/311-clojure-from-the-ground-up-logistics#comment-1824)

I think I can sort of clean an answer from how you used it here, but do you have a general guideline for when to use the ->> macro? I think that, coming from Ruby, I’d be tempted to lean on it too much.

Aphyr on [2014-02-24](/posts/311-clojure-from-the-ground-up-logistics#comment-1825)

I shift to `->>` whenever I’m doing a linear sequence of computations, and I lean on it more heavily in the REPL, where matching parens is a little more cumbersome. It’s usually shorter for anything over 3 function calls. :)

![Aphyr](https://www.gravatar.com/avatar/e145b50faf662e70c066b13c98921900?r=pg&s=96&d=identicon "Aphyr")

Aphyr on [2014-02-24](/posts/311-clojure-from-the-ground-up-logistics#comment-1826)

Think I finally convinced myself to fix the comment formatting code, haha. :)

![Aphyr](https://www.gravatar.com/avatar/e145b50faf662e70c066b13c98921900?r=pg&s=96&d=identicon "Aphyr")

![Jeremy](https://www.gravatar.com/avatar/a198879c03effd0d1e0cd73b5b38b751?r=pg&s=96&d=identicon "Jeremy")

Jeremy on [2014-02-24](/posts/311-clojure-from-the-ground-up-logistics#comment-1827)

Cool, thanks. Really enjoying this series!

Aphyr on [2014-02-24](/posts/311-clojure-from-the-ground-up-logistics#comment-1828)

Thank you! :)

![Aphyr](https://www.gravatar.com/avatar/e145b50faf662e70c066b13c98921900?r=pg&s=96&d=identicon "Aphyr")

![Tom](https://www.gravatar.com/avatar/dbf7e6364a56f5b98607b86dcc18db7d?r=pg&s=96&d=identicon "Tom")

Tom on [2014-05-10](/posts/311-clojure-from-the-ground-up-logistics#comment-1862)

This is brilliantly written. Thank you very much for your effort in producing it.

![Samridh](https://www.gravatar.com/avatar/a462b227d14ac376a3eb1d0576a813e7?r=pg&s=96&d=identicon "Samridh")

Samridh on [2014-05-22](/posts/311-clojure-from-the-ground-up-logistics#comment-1880)

Hi! Really love the series of posts. Just a tiny nit: (repeat n x): repeats x, n times so pow should be defined as: (defn pow “Raises base to the given power. For instance, (pow 3 2) returns three squared, or nine.” base power)) Right?

![Samridh](https://www.gravatar.com/avatar/a462b227d14ac376a3eb1d0576a813e7?r=pg&s=96&d=identicon "Samridh")

Samridh on [2014-05-22](/posts/311-clojure-from-the-ground-up-logistics#comment-1881)

Well, now I feel stupid :) Just read the whole thing. Ignore my comment.

![Bruno](https://www.gravatar.com/avatar/a1ea60b344309f25069867fa629aaf1d?r=pg&s=96&d=identicon "Bruno")

Bruno on [2014-12-07](/posts/311-clojure-from-the-ground-up-logistics#comment-1991)

This tutorial is bookmarked in my browser for a long time now. Please, just… keep it up!

Cheers!

![R0B](https://www.gravatar.com/avatar/818072198759cdf40d3c3888f733dae5?r=pg&s=96&d=identicon "R0B")

[R0B](https://mdf0.blogpspot.com) on [2014-12-19](/posts/311-clojure-from-the-ground-up-logistics#comment-1996)

Although I have not read any post yet I finished saving them to my drive. I say thanks for your open intro and support for the ladies. Great job man and continue the good fight.

![Peter](https://www.gravatar.com/avatar/b6b8952ea62c016fb9598eb888a819ef?r=pg&s=96&d=identicon "Peter")

Peter on [2014-12-26](/posts/311-clojure-from-the-ground-up-logistics#comment-2018)

There is no link here to the next chapter!

![Dinesh](https://www.gravatar.com/avatar/66652fb6340ab3747d9b58292ebeb687?r=pg&s=96&d=identicon "Dinesh")

Dinesh on [2015-06-07](/posts/311-clojure-from-the-ground-up-logistics#comment-2423)

`(defn pow "Raises base to the given power. For instance, (pow 3 2) returns three squared, or nine." [base power] (apply * (repeat base power)))`

_This should be_ `(repeat power base)`

(repeat 2 3) (3 3)

![Tedwed](https://www.gravatar.com/avatar/ff5399a04f9f34bd69aaaf460dbac21f?r=pg&s=96&d=identicon "Tedwed")

[Tedwed](https://tedwed.com/product/jurassic-world-2015-chris-pratt-vest.html) on [2015-06-17](/posts/311-clojure-from-the-ground-up-logistics#comment-2428)

Educational post! The involved analysis is very knowledgeable and a lot to know about this. Keep sharing here!

![miles](https://www.gravatar.com/avatar/9b94ecf0d927c6786fd1f9da2cb7dddb?r=pg&s=96&d=identicon "miles")

miles on [2016-06-09](/posts/311-clojure-from-the-ground-up-logistics#comment-2658)

Hey, just wanted to let you know that link to UCR data (in “Exploring data” section: “Let’s download the most recent year’s _normalized data_ (…)”) is 404 and should probably be ‘[https://raw.githubusercontent.com/maliabadi/ucr-json/master/data/parsed/normalized/2008.json](https://raw.githubusercontent.com/maliabadi/ucr-json/master/data/parsed/normalized/2008.json)’

![billwang](https://www.gravatar.com/avatar/4d0707c0df8f05939b6c058d2420c8fd?r=pg&s=96&d=identicon "billwang")

billwang on [2019-05-22](/posts/311-clojure-from-the-ground-up-logistics#comment-3018)

awsome

Post a Comment
==============

Comments are moderated. Links have `nofollow`. Seriously, spammers, give it a rest.

Please avoid writing anything here unless you're a computer. Captcha  This is also a trap: Comment

Name 

E-Mail (for [Gravatar](https://gravatar.com), not published) 

Personal URL 

Comment

Supports [Github-flavored Markdown](https://guides.github.com/features/mastering-markdown/), including `[links](http://foo.com/)`, `*emphasis*`, `_underline_`, `` `code` ``, and `> blockquotes`. Use ` ```clj ` on its own line to start an (e.g.) Clojure code block, and ` ``` ` to end the block.    

Copyright © 2023 Kyle Kingsbury.  
Also on: [Mastodon](https://woof.group/@aphyr) and [Github](https://github.com/aphyr).

var \_gaq = \_gaq || \[\]; \_gaq.push(\['\_setAccount', 'UA-9527251-1'\]); \_gaq.push(\['\_trackPageview'\]); (function() { var ga = document.createElement('script'); ga.type = 'text/javascript'; ga.async = true; ga.src = ('https:' == document.location.protocol ? 'https://ssl' : 'http://www') + '.google-analytics.com/ga.js'; var s = document.getElementsByTagName('script')\[0\]; s.parentNode.insertBefore(ga, s); })();