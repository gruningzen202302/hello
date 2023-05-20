(ns welcome-to-clojure
  (:require [clojure.repl :refer [source apropos dir pst doc find-doc]]
            [clojure.string :as string]
            [clojure.test :refer [is are]])) 


(comment

;;
;;
;;

  Clojure from the ground up: welcome     window.dataLayer = window.dataLayer || \[\]; function gtag(){dataLayer.push(arguments);} gtag('js', new Date()); gtag('config', 'G-MXDP37S6QL');

    

*   [Aphyr](/)
*   [About](/about)
*   [Blog](/posts)
*   [Photos](/photos)
*   [Code](http://github.com/aphyr)

[Clojure from the ground up: welcome](/posts/301-clojure-from-the-ground-up-welcome)
====================================================================================

[Software](/tags/software) [Clojure](/tags/clojure) [Clojure from the ground up](/tags/clojure-from-the-ground-up)

2013-10-26

This guide aims to introduce newcomers and experienced programmers alike to the beauty of functional programming, starting with the simplest building blocks of software. You’ll need a computer, basic proficiency in the command line, a text editor, and an internet connection. By the end of this series, you’ll have a thorough command of the Clojure programming language.

[Who is this guide for?](#who-is-this-guide-for)
------------------------------------------------

Science, technology, engineering, and mathematics are deeply rewarding fields, yet few women enter STEM as a career path. Still more are discouraged by a culture which repeatedly asserts that women lack the analytic aptitude for writing software, that they are not driven enough to be successful scientists, that it’s not cool to pursue a passion for structural engineering. Those few with the talent, encouragement, and persistence to break in to science and tech are discouraged by persistent sexism in practice: the old boy’s club of tenure, being passed over for promotions, isolation from peers, and flat-out assault. This landscape sucks. I want to help change it.

[Women Who Code](https://twitter.com/WomenWhoCode), [PyLadies](http://www.pyladies.com/), [Black Girls Code](http://www.blackgirlscode.com/), [RailsBridge](http://railsbridge.org/), [Girls Who Code](http://www.girlswhocode.com/about-us/), [Girl Develop It](http://www.girldevelopit.com/), and [Lambda Ladies](http://www.lambdaladies.com/) are just a few of the fantastic groups helping women enter and thrive in software. I wholeheartedly support these efforts.

In addition, I want to help in my little corner of the technical community–functional programming and distributed systems–by making high-quality educational resources available for free. The [Jepsen](/tags/jepsen) series has been, in part, an effort to share my enthusiasm for distributed systems with beginners of all stripes–but especially for [women, LGBT folks, and people of color](http://aphyr.com/posts/275-meritocracy-is-short-sighted).

As technical authors, we often assume that our readers are white, that our readers are straight, that our readers are traditionally male. This is the invisible default in US culture, and it’s especially true in tech. People continue to assume on the basis of my software and writing that I’m straight, because well hey, it’s a statistically reasonable assumption.

But I’m _not_ straight. I get called faggot, cocksucker, and sinner. People say they’ll pray for me. When I walk hand-in-hand with my boyfriend, people roll down their car windows and stare. They threaten to beat me up or kill me. Every day I’m aware that I’m the only gay person some people know, and that I can show that not all gay people are effeminate, or hypermasculine, or ditzy, or obsessed with image. That you can be a manicurist or a mathematician or both. Being different, being a stranger in your culture, [comes with all kinds of challenges](http://aphyr.com/posts/274-identity-and-state). I can’t speak to everyone’s experience, but I can take a pretty good guess.

At the same time, in the technical community I’ve found overwhelming warmth and support, from people of _all_ stripes. My peers stand up for me every day, and I’m so thankful–especially you straight dudes–for understanding a bit of what it’s like to be different. I want to extend that same understanding, that same empathy, to people unlike myself. Moreover, I want to reassure everyone that though they may feel different, they _do_ have a place in this community.

So before we begin, I want to reinforce that you _can_ program, that you _can_ do math, that you _can_ design car suspensions and fire suppression systems and spacecraft control software and distributed databases, regardless of what your classmates and media and even fellow engineers think. You don’t have to be white, you don’t have to be straight, you don’t have to be a man. You can grow up never having touched a computer and still become a skilled programmer. Yeah, it’s harder–and yeah, people will give you shit, but that’s not your fault and has nothing to do with your _ability_ or your _right_ to do what you love. All it takes to be a good engineer, scientist, or mathematician is your curiosity, your passion, the right teaching material, and putting in the hours.

There’s nothing in this guide that’s just for lesbian grandmas or just for mixed-race kids; bros, you’re welcome here too. There’s nothing dumbed down. We’re gonna go as deep into the ideas of programming as I know how to go, and we’re gonna do it with everyone on board.

No matter who you are or who people _think_ you are, this guide is for you.

[Why Clojure?](#why-clojure)
----------------------------

This book is about how to program. We’ll be learning in Clojure, which is a modern dialect of a very old family of computer languages, called Lisp. You’ll find that many of this book’s ideas will translate readily to other languages; though they may be [expressed in different ways](http://aphyr.com/posts/266-core-language-concepts).

We’re going to explore the nature of syntax, metalanguages, values, references, mutation, control flow, and concurrency. Many languages leave these ideas implicit in the language construction, or don’t have a concept of metalanguages or concurrency at all. Clojure makes these ideas explicit, first-class language constructs.

At the same time, we’re going to defer or omit any serious discussion of static type analysis, hardware, and performance. This is not to say that these ideas aren’t _important_; just that they don’t fit well within this particular narrative arc. For a deep exploration of type theory I recommend a study in Haskell, and for a better understanding of underlying hardware, learning C and an assembly language will undoubtedly help.

In more general terms, Clojure is a well-rounded language. It offers broad library support and runs on multiple operating systems. Clojure performance is not terrific, but is orders of magnitude faster than Ruby, Python, or Javascript. Unlike some faster languages, Clojure emphasizes _safety_ in its type system and approach to parallelism, making it easier to write correct multithreaded programs. Clojure is _concise_, requiring very little code to express complex operations. It offers a REPL and dynamic type system: ideal for beginners to experiment with, and well-suited for manipulating complex data structures. A consistently designed standard library and full-featured set of core datatypes rounds out the Clojure toolbox.

Finally, there are some drawbacks. As a compiled language, Clojure is much slower to start than a scripting language; this makes it unsuitable for writing small scripts for interactive use. Clojure is also _not_ well-suited for high-performance numeric operations. Though it is possible, you have to jump through hoops to achieve performance comparable with Java. I’ll do my best to call out these constraints and shortcomings as we proceed through the text.

With that context out of the way, let’s get started by installing Clojure!

[Getting set up](#getting-set-up)
---------------------------------

First, you’ll need a Java Virtual Machine, or JVM, and its associated development tools, called the JDK. This is the software which _runs_ a Clojure program. If you’re on Windows, install [Oracle JDK 1.7](http://www.oracle.com/technetwork/java/javase/downloads/index.html). If you’re on OS X or Linux, you may already have a JDK installed. In a terminal, try:

    which javac
    

If you see something like

    /usr/bin/javac
    

Then you’re good to go. If you don’t see any output from that command, install the appropriate [Oracle JDK 1.7](http://www.oracle.com/technetwork/java/javase/downloads/index.html) for your operating system, or whatever JDK your package manager has available.

When you have a JDK, you’ll need [Leiningen](http://leiningen.org), the Clojure build tool. If you’re on a Linux or OS X computer, the instructions below should get you going right away. If you’re on Windows, see the Leiningen page for an installer. If you get stuck, you might want to start with a [primer on command line basics](http://blog.teamtreehouse.com/command-line-basics).

    mkdir -p ~/bin
    cd ~/bin
    curl -O https://raw.githubusercontent.com/technomancy/leiningen/stable/bin/lein
    chmod a+x lein
    

Leiningen automatically handles installing Clojure, finding libraries from the internet, and building and running your programs. We’ll create a new Leiningen project to play around in:

    cd
    lein new scratch
    

This creates a new directory in your homedir, called `scratch`. If you see `command not found` instead, it means the directory `~/bin` isn’t registered with your terminal as a place to search for programs. To fix this, add the line

    export PATH="$PATH":~/bin
    

to the file `.bash_profile` in your home directory, then run `source ~/.bash_profile`. Re-running `lein new scratch` should work.

Let’s enter that directory, and start using Clojure itself:

    cd scratch
    lein repl
    

[The structure of programs](#the-structure-of-programs)
-------------------------------------------------------

When you type `lein repl` at the terminal, you’ll see something like this:

    aphyr@waterhouse:~/scratch$ lein repl
    nREPL server started on port 45413
    REPL-y 0.2.0
    Clojure 1.5.1
        Docs: (doc function-name-here)
              (find-doc "part-of-name-here")
      Source: (source function-name-here)
     Javadoc: (javadoc java-object-or-class-here)
        Exit: Control+D or (exit) or (quit)
    
    user=>
    

This is an interactive Clojure environment called a REPL, for “Read, Evaluate, Print Loop”. It’s going to _read_ a program we enter, run that program, and print the results. REPLs give you quick feedback, so they’re a great way to explore a program interactively, run tests, and prototype new ideas.

Let’s write a simple program. The simplest, in fact. Type “nil”, and hit enter.

    user=> nil
    nil
    

`nil` is the most basic value in Clojure. It represents emptiness, nothing-doing, not-a-thing. The absence of information.

    user=> true
    true
    user=> false
    false
    

`true` and `false` are a pair of special values called _Booleans_. They mean exactly what you think: whether a statement is true or false. `true`, `false`, and `nil` form the three poles of the Lisp logical system.

    user=> 0
    0
    

This is the number zero. Its numeric friends are `1`, `-47`, `1.2e-4`, `1/3`, and so on. We might also talk about _strings_, which are chunks of text surrounded by double quotes:

    user=> "hi there!"
    "hi there!"
    

`nil`, `true`, `0`, and `"hi there!"` are all different types of _values_; the nouns of programming. Just as one could say “House.” in English, we can write a program like `"hello, world"` and it evaluates to itself: the string `"hello world"`. But most sentences aren’t just about stating the existence of a thing; they involve _action_. We need _verbs_.

    user=> inc
    #<core$inc clojure.core$inc@6f7ef41c>
    

This is a verb called `inc`–short for “increment”. Specifically, `inc` is a _symbol_ which _points_ to a verb: `#<core$inc clojure.core$inc@6f7ef41c>`– just like the word “run” is a _name_ for the _concept_ of running.

There’s a key distinction here–that a signifier, a reference, a label, is not the same as the signified, the referent, the concept itself. If you write the word “run” on paper, the ink means nothing by itself. It’s just a symbol. But in the mind of a reader, that symbol takes on _meaning_; the idea of running.

Unlike the number 0, or the string “hi”, symbols are references to other values. when Clojure evaluates a symbol, it looks up that symbol’s meaning. Look up `inc`, and you get `#<core$inc clojure.core$inc@6f7ef41c>`.

Can we refer to the symbol itself, _without_ looking up its meaning?

    user=> 'inc
    inc
    

Yes. The single quote `'` _escapes_ a sentence. In programming languages, we call sentences `expressions` or `statements`. A quote says “Rather than _evaluating_ this expression’s text, simply return the text itself, unchanged.” Quote a symbol, get a symbol. Quote a number, get a number. Quote anything, and get it back exactly as it came in.

    user=> '123
    123
    user=> '"foo"
    "foo"
    user=> '(1 2 3)
    (1 2 3)
    

A new kind of value, surrounded by parentheses: the _list_. LISP originally stood for LISt Processing, and lists are still at the core of the language. In fact, they form the most basic way to compose expressions, or sentences. A list is a single expression which has _multiple parts_. For instance, this list contains three elements: the numbers 1, 2, and 3. Lists can contain anything: numbers, strings, even other lists:

    user=> '(nil "hi")
    (nil "hi")
    

A list containing two elements: the number 1, and a second list. That list contains two elements: the number 2, and another list. _That_ list contains two elements: 3, and an empty list.

    user=> '(1 (2 (3 ())))
    (1 (2 (3 ())))
    

You could think of this structure as a tree–which is a provocative idea, because _languages_ are like trees too: sentences are comprised of clauses, which can be nested, and each clause may have subjects modified by adjectives, and verbs modified by adverbs, and so on. “Lindsay, my best friend, took the dog which we found together at the pound on fourth street, for a walk with her mother Michelle.”

    Took
      Lindsay
        my best friend
      the dog
        which we found together
          at the pound
            on fourth street
        for a walk
          with her mother
            Michelle
    

But let’s try something simpler. Something we know how to talk about. “Increment the number zero.” As a tree:

    Increment
      the number zero
    

We have a symbol for incrementing, and we know how to write the number zero. Let’s combine them in a list:

    clj=> '(inc 0)
    (inc 0)
    

A basic sentence. Remember, since it’s quoted, we’re talking about the tree, the text, the expression, by itself. Absent interpretation. If we remove the single-quote, Clojure will _interpret_ the expression:

    user=> (inc 0)
    1
    

Incrementing zero yields one. And if we wanted to increment _that_ value?

    Increment
      increment
        the number zero
    

    user=> (inc (inc 0))
    2
    

A sentence in Lisp is a list. It starts with a verb, and is followed by zero or more objects for that verb to act on. Each part of the list can _itself_ be another list, in which case that nested list is evaluated first, just like a nested clause in a sentence. When we type

    (inc (inc 0))
    

Clojure first looks up the meanings for the symbols in the code:

    (#<core$inc clojure.core$inc@6f7ef41c>
      (#<core$inc clojure.core$inc@6f7ef41c>
        0))
    

Then evaluates the innermost list `(inc 0)`, which becomes the number 1:

    (#<core$inc clojure.core$inc@6f7ef41c>
     1)
    

Finally, it evaluates the outer list, incrementing the number 1:

    2
    

Every list starts with a verb. Parts of a list are evaluated from left to right. Innermost lists are evaluated before outer lists.

    (+ 1 (- 5 2) (+ 3 4))
    (+ 1 3       (+ 3 4))
    (+ 1 3       7)
    11
    

That’s it.

The entire grammar of Lisp: the structure for every expression in the language. We transform expressions by _substituting_ meanings for symbols, and obtain some result. This is the core of the [Lambda Calculus](http://en.wikipedia.org/wiki/Lambda_calculus), and it is the theoretical basis for almost all computer languages. Ruby, Javascript, C, Haskell; all languages express the text of their programs in different ways, but internally all construct a _tree_ of expressions. Lisp simply makes it explicit.

[Review](#review)
-----------------

We started by learning a few basic nouns: numbers like `5`, strings like `"cat"`, and symbols like `inc` and `+`. We saw how quoting makes the difference between an _expression_ itself and the thing it _evaluates_ to. We discovered symbols as _names_ for other values, just like how words represent concepts in any other language. Finally, we combined lists to make trees, and used those trees to represent a program.

With these basic elements of syntax in place, it’s time to expand our vocabulary with new verbs and nouns; learning to [represent more complex values and transform them in different ways](http://aphyr.com/posts/302-clojure-from-the-ground-up-basic-types).

![Vasudev Ram](https://www.gravatar.com/avatar/05e4e83c87a5700958fcb3efa8951a06?r=pg&s=96&d=identicon "Vasudev Ram")

[Vasudev Ram](https://www.dancingbison.com) on [2013-10-26](/posts/301-clojure-from-the-ground-up-welcome#comment-1677)

Nice idea, Kyle.

![svankie](https://www.gravatar.com/avatar/98c5b03ee6a2786356748b9a99e0826f?r=pg&s=96&d=identicon "svankie")

svankie on [2013-10-26](/posts/301-clojure-from-the-ground-up-welcome#comment-1678)

Loving this.

![david karapetyan](https://www.gravatar.com/avatar/87c5b458dca4ebe9646478d1efc48b3c?r=pg&s=96&d=identicon "david karapetyan")

[david karapetyan](https://scriptcrafty.com) on [2013-10-26](/posts/301-clojure-from-the-ground-up-welcome#comment-1679)

Looking forward to what’s next.

![BR](https://www.gravatar.com/avatar/0f324fb00cdf26f501450a11eb821d8f?r=pg&s=96&d=identicon "BR")

BR on [2013-10-26](/posts/301-clojure-from-the-ground-up-welcome#comment-1680)

Seriously? LISP? Haven’t we learned?

![Peter](https://www.gravatar.com/avatar/3ad74a0b72273db84c85d67253fe5982?r=pg&s=96&d=identicon "Peter")

Peter on [2013-10-26](/posts/301-clojure-from-the-ground-up-welcome#comment-1681)

> Seriously? LISP? Haven’t we learned?

Apparently you haven’t

![na](https://www.gravatar.com/avatar/16fc03db97c032b24e20f13500971d42?r=pg&s=96&d=identicon "na")

na on [2013-10-26](/posts/301-clojure-from-the-ground-up-welcome#comment-1682)

> > Seriously? LISP? Haven’t we learned?
> 
> Apparently you haven’t

Agreed.

![Johnny Chang](https://www.gravatar.com/avatar/2d22b5145dc063d32010f0905e2b0078?r=pg&s=96&d=identicon "Johnny Chang")

[Johnny Chang](http://johnnychang.com) on [2013-10-26](/posts/301-clojure-from-the-ground-up-welcome#comment-1683)

Awesome! Eagerly awaiting the next installment!

![alex](https://www.gravatar.com/avatar/016455fdff8501f9b0b322137a641ce4?r=pg&s=96&d=identicon "alex")

[alex](https://alexspencer.me) on [2013-10-27](/posts/301-clojure-from-the-ground-up-welcome#comment-1684)

I have to make a comment entirely unrelated to your post - I love your blog design and layout. It is great. The comments, the terminal CSS, etc. great job.

![JD](https://www.gravatar.com/avatar/a3e8874df6cfeae776dcf5259cd39a40?r=pg&s=96&d=identicon "JD")

JD on [2013-10-27](/posts/301-clojure-from-the-ground-up-welcome#comment-1685)

I’m definitely following these posts.

Thank you! :)

![ScientificCoder](https://www.gravatar.com/avatar/0d2399d5f7eb1cd46f0437b20a39efaa?r=pg&s=96&d=identicon "ScientificCoder")

ScientificCoder on [2013-10-27](/posts/301-clojure-from-the-ground-up-welcome#comment-1686)

Nice ! I’ll be sure to point newcomers to your site. Kuddos and keep them coming ! ☺

Cheers.

![Aahz](https://www.gravatar.com/avatar/7e4e7569d64e14de784aca9f9a8fffb4?r=pg&s=96&d=identicon "Aahz")

[Aahz](http://www.pythoncraft.com/) on [2013-10-27](/posts/301-clojure-from-the-ground-up-welcome#comment-1687)

It’s not really clear who this is aimed at. For example, I got here through a feed thingie (DuckDuckGo on Android) and had no idea what exactly Clojure was until I saw the magic word “LISP”. Might be a good idea to at least link to something.

![Rick James](https://www.gravatar.com/avatar/601277db52ff762d09b5f29fd2257679?r=pg&s=96&d=identicon "Rick James")

Rick James on [2013-10-27](/posts/301-clojure-from-the-ground-up-welcome#comment-1689)

The whole white knight paragraph was horrible. Nobody thinks you’re a great guy because you did that. You’re just an asshole.

![Kasim](https://www.gravatar.com/avatar/1f992f8432040eddc781a49b034def54?r=pg&s=96&d=identicon "Kasim")

[Kasim](http://staplesinnovationlab.com/) on [2013-10-27](/posts/301-clojure-from-the-ground-up-welcome#comment-1691)

Hi,

Great post and keep up the good work. I’d be happy to help with reviewing codes and such before you publish.

Just let you know we are hiring Clojure developers. Come check us out: [http://staplesinnovationlab.com/](http://staplesinnovationlab.com/)

![Jbeja](https://www.gravatar.com/avatar/26134de1e74c627a6df26e898229e1e0?r=pg&s=96&d=identicon "Jbeja")

Jbeja on [2013-10-27](/posts/301-clojure-from-the-ground-up-welcome#comment-1693)

Loving this and thanks for the encouraging intro ( i am gay), just reading the “Clojure data analisys cookbook” and this will help me to go trough the chapters.

![Mark Wotton](https://www.gravatar.com/avatar/a79143991b8b636aa1670a1b30ba55f8?r=pg&s=96&d=identicon "Mark Wotton")

[Mark Wotton](https://shimweasel.com) on [2013-10-27](/posts/301-clojure-from-the-ground-up-welcome#comment-1695)

Nice tute - but isn’t it a read-Eval-print-loop, not execute?

![epyf](https://www.gravatar.com/avatar/e9fbeb37bfd7d3c5b187e5d8e2f422a6?r=pg&s=96&d=identicon "epyf")

epyf on [2013-10-28](/posts/301-clojure-from-the-ground-up-welcome#comment-1697)

Well, women, LGBT and people of color can always take classes on Coursera. The quality there is high too, and they won’t get the impression of being “people with special teaching ”. Or flagships - want it or not.

I do understand that the tutorial is aimed at just anybody, but this is really the feeling I get when I read that paragraph (but I’m white and straight, that may be the reason)

![epyf](https://www.gravatar.com/avatar/e9fbeb37bfd7d3c5b187e5d8e2f422a6?r=pg&s=96&d=identicon "epyf")

epyf on [2013-10-28](/posts/301-clojure-from-the-ground-up-welcome#comment-1698)

Well, women, LGBT and people of color can always take classes on Coursera. The quality there is high too, and they won’t get the impression of being “people with special teaching ”. Or flagships - want it or not.

I do understand that the tutorial is aimed at just anybody, but this is really the feeling I get when I read that paragraph (but I’m white and straight, that may be the reason)

![M](https://www.gravatar.com/avatar/1c417abcc099af753a15420bff6c363a?r=pg&s=96&d=identicon "M")

M on [2013-10-28](/posts/301-clojure-from-the-ground-up-welcome#comment-1699)

user=> ‘(1 (2 (3 ())) (1 (2 (3)))

is this a mistake?

![Andreas](https://www.gravatar.com/avatar/2f4b6ce9f3de16a3e17bd548f70836e1?r=pg&s=96&d=identicon "Andreas")

Andreas on [2013-10-28](/posts/301-clojure-from-the-ground-up-welcome#comment-1700)

Hi Kyle,

the goals of this introduction you’re mentioning are just wonderful. I have nothing than deep respect for your openness, encouragement _and_ your profound technical skills! Please, just keep walking this path.

Best, Andreas

![jarppe](https://www.gravatar.com/avatar/b6a00102453a0fd6e3fc9c25ca83cb9c?r=pg&s=96&d=identicon "jarppe")

[jarppe](http://metosin.fi) on [2013-10-28](/posts/301-clojure-from-the-ground-up-welcome#comment-1701)

Hello Kyle,

I really liked you approach where you introduce quoting first, then using quoting introduce a list, and only then calling functions.

I have been giving Clojure introductions and I usually start from literals and simple collections. When I go from vectors to lists I have to introduce quoting and evaluation at the same time and this is usually hard to grasp.

Next I will try to introduce Clojure to my 18 years old daughter and I think I’ll use your blog as starting point. So please continue your excellent blog.

Br, Jarppe

Aphyr on [2013-10-28](/posts/301-clojure-from-the-ground-up-welcome#comment-1702)

Good catch, it is a mistake. Thanks M.

![Aphyr](https://www.gravatar.com/avatar/e145b50faf662e70c066b13c98921900?r=pg&s=96&d=identicon "Aphyr")

Aphyr on [2013-10-28](/posts/301-clojure-from-the-ground-up-welcome#comment-1703)

I agree, jarppe. Went back and forth several times on whether it made more sense to introduce the (fun & args) syntax initially–and introduce the concept of evaluation as substitution–or stick with more concrete semantics as in Chapter 2. I… _think_ it paid off; folks seem to be following this path without getting too lost, and it sets the stage for a deeper exploration of substitution later.

Definitely a more abstract path, though. I’m trying to explain the core structure as deeply as possible without getting bogged down, but without losing sight of inductive learners either. We’ll see how it goes. :)

![Aphyr](https://www.gravatar.com/avatar/e145b50faf662e70c066b13c98921900?r=pg&s=96&d=identicon "Aphyr")

![Sergey](https://www.gravatar.com/avatar/3343705ce45837959960b7e9119143bf?r=pg&s=96&d=identicon "Sergey")

Sergey on [2013-11-18](/posts/301-clojure-from-the-ground-up-welcome#comment-1743)

Hello, Kyle! You are a man! Thanks a bunch for this guide, looking forward to next posts :)

![Adrian](https://www.gravatar.com/avatar/0629ec4252f7aaf7988b5770e73de4f5?r=pg&s=96&d=identicon "Adrian")

Adrian on [2013-11-20](/posts/301-clojure-from-the-ground-up-welcome#comment-1748)

Thankyou for this article. I often find myself wallpapering over my sexuality in professional environments to avoid any “awkward looks”. It is important for queer coders to be visible to their peers and their lives celebrated equally. It put a smile on my face to read something that speaks to me both socially and technically :)

![LitoNico](https://www.gravatar.com/avatar/294de3557d9d00b3d2d8a1e6aab028cf?r=pg&s=96&d=identicon "LitoNico")

[LitoNico](https://lito-nico.tumblr.com) on [2013-12-02](/posts/301-clojure-from-the-ground-up-welcome#comment-1760)

Thanks for both the article and statement of purpose! This is a great reference for learning clojure– especially since I’m completely new to lisps.

![Juan Manuel](https://www.gravatar.com/avatar/a839a1dce92696539e7e9475604ef310?r=pg&s=96&d=identicon "Juan Manuel")

Juan Manuel on [2013-12-16](/posts/301-clojure-from-the-ground-up-welcome#comment-1780)

Why don’t you publish this as a book? The contents deserve it.

![Marko Bonaci](https://www.gravatar.com/avatar/08cff047d246096cee251b541594a52f?r=pg&s=96&d=identicon "Marko Bonaci")

[Marko Bonaci](https://github/mbonaci @markobonaci) on [2013-12-21](/posts/301-clojure-from-the-ground-up-welcome#comment-1785)

I love you dude. And I’m strait! :)

![Ahsen](https://www.gravatar.com/avatar/73e3492025a37b39053a539c9d102bd6?r=pg&s=96&d=identicon "Ahsen")

Ahsen on [2014-01-05](/posts/301-clojure-from-the-ground-up-welcome#comment-1804)

nice tutorial, eagerly waiting for the next chapters.

![m](https://www.gravatar.com/avatar/294de3557d9d00b3d2d8a1e6aab028cf?r=pg&s=96&d=identicon "m")

m on [2014-01-18](/posts/301-clojure-from-the-ground-up-welcome#comment-1814)

This is wonderful. Thank you.

![Mathew Bentley](https://www.gravatar.com/avatar/976f0ac6a82ba65ef6b8065262ef1172?r=pg&s=96&d=identicon "Mathew Bentley")

Mathew Bentley on [2014-02-04](/posts/301-clojure-from-the-ground-up-welcome#comment-1818)

So happy to read this guide. You are an absolute programming badass, thanks for sharing some of that with the rest of us!

Aphyr on [2014-03-02](/posts/301-clojure-from-the-ground-up-welcome#comment-1834)

Thanks for the kind words, all. Goal has always been to produce a full-length book; hopefully some time by the end of the year.

![Aphyr](https://www.gravatar.com/avatar/e145b50faf662e70c066b13c98921900?r=pg&s=96&d=identicon "Aphyr")

![Rob](https://www.gravatar.com/avatar/a8d4e540082d690af80ab70191239ca0?r=pg&s=96&d=identicon "Rob")

Rob on [2014-03-19](/posts/301-clojure-from-the-ground-up-welcome#comment-1847)

Just to counterbalance the opening slightly, and perhaps this is just an encouraging view of the way things are where I live, but certainly at my software company we’ve interviewed female applicants whose CVs weren’t good enough, but we wanted to hire more women. One colleague hired this way didn’t know the difference between megabytes and gigabytes, but the point is it certainly wasn’t an environment that discouraged female hires.

When I was at school and university white guys such as myself weren’t encouraged socially to keep doing tech/geeky studies; in fact one chief idea is that women don’t like guys who do that sort of thing. I think facing opposition is something we all do if we want to do something that isn’t socially normal, and it’s hard (but necessary) to try and not confuse that opposition that almost everyone faces with some genuine victimisation that one has felt in the past, or lump all opposition together as having the same cause. E.g. if you have two guys who don’t like you, one of them might not because of your gender or sexuality or whatever (i.e. unfair victimisation) and the other might not because he doesn’t feel you work hard enough. If that happens, don’t think that both guys don’t like your for the victimisation reason, easy though it is to do.

I’m a Brit working in South Africa, and Afrikaaners sometimes give me a hard time. I keep smiling and chatting nicely, and eventually they stop thinking of me as British and just think of me as a person. I haven’t yet written a technical blog post that starts by encouraging Brits to work in South Africa :)

![Amy Jenkins](https://www.gravatar.com/avatar/837b171c4d5158c78780802b441a14fd?r=pg&s=96&d=identicon "Amy Jenkins")

Amy Jenkins on [2014-03-25](/posts/301-clojure-from-the-ground-up-welcome#comment-1850)

One of the most useful things for a beginner to do is to muck around with the command line. Thanks for this!

![Lee Spector](https://www.gravatar.com/avatar/8c4b2d3fb7b965ffe7fe9da1ee27ad9a?r=pg&s=96&d=identicon "Lee Spector")

[Lee Spector](http://hampshire.edu/lspector) on [2014-06-09](/posts/301-clojure-from-the-ground-up-welcome#comment-1891)

I really like this series of posts and I’m considering recommending it to my students in a Clojure-based AI course in the fall. Is there a table of contents page that I’ve missed? Easy to create externally, of course, but it’d be nice to have one on the site if there isn’t one already. FWIW I created something that uses an approach that’s similar in some ways (clojinc, at github/lspector), but whereas I provided almost no text at all between code snippets, you’ve provided lots and lots of helpful text. Thanks!

![](https://www.gravatar.com/avatar/294de3557d9d00b3d2d8a1e6aab028cf?r=pg&s=96&d=identicon)

anonymous on [2014-07-30](/posts/301-clojure-from-the-ground-up-welcome#comment-1942)

> Is there a table of contents page that I’ve missed?

Right up top, at the tags list: [http://aphyr.com/tags/Clojure-from-the-ground-up](http://aphyr.com/tags/Clojure-from-the-ground-up). I’ll be reformatting all this work for the book, eventually, where there’ll be actual chapter headings, etc. Had a bunch of other stuff on my plate recently but starting to get back to writing on CFTGU this month. :)

![Mike](https://www.gravatar.com/avatar/f2b0c578d79b3b269a1d8327843732c1?r=pg&s=96&d=identicon "Mike")

Mike on [2014-09-08](/posts/301-clojure-from-the-ground-up-welcome#comment-1952)

Thanks for making this series! I’m a novice trying to learn distributed systems and clojure, this blog is awesome. Two quick suggestions for the getting started section:

Ii’m on Ubuntu 14.04 and this works for me `which javacc`

`curl -O [https://raw.githubusercontent.com/technomancy/leiningen/stable/bin/lein](https://raw.githubusercontent.com/technomancy/leiningen/stable/bin/lein)`

![Sam](https://www.gravatar.com/avatar/a28ec2794dc4a6533d78ceecb3575854?r=pg&s=96&d=identicon "Sam")

Sam on [2014-10-21](/posts/301-clojure-from-the-ground-up-welcome#comment-1970)

Hi,

I’m new to Clojure. I was considering implementing data structures like BST etc in this and thought having a namespace clojure.tree might be a good idea. But others felt it was a horrible idea as there were other data structures that does all this work. Could you explain more about those?

Thanks!

![Peter](https://www.gravatar.com/avatar/6fcd34da244bbe310730ca5c625e94e7?r=pg&s=96&d=identicon "Peter")

Peter on [2014-11-11](/posts/301-clojure-from-the-ground-up-welcome#comment-1982)

Was going to use this resource until I saw the psychotic rants of the author. No way I would support someone with such hatred for other people. get over it - most coders are male, most people are straight. No reason to become a hate-monger.

![Kevin](https://www.gravatar.com/avatar/294de3557d9d00b3d2d8a1e6aab028cf?r=pg&s=96&d=identicon "Kevin")

Kevin on [2014-12-23](/posts/301-clojure-from-the-ground-up-welcome#comment-2012)

I agree with Rob. The beginning of this blog post is a bit too much and needs some counterbalancing. Honestly, I think your post would be much better without it. Perhaps you could just add a link to your rant? And put a disclaimer saying, “This is for everyone and here is why .” In my opinion rants like this are why people think of others so differently. As a child I never really noticed/thought about someone else being different. I always just saw them as a person without paying any attention to these differences I had yet to learn about. As I grew older I read rants like this one, and listened in history classes, etc. Eventually you are taught to think people are different. It is not that teachers or people around you meant to teach you to think that way, but just the mere discussion of differences implants those thoughts at least subconsciously into the brain. If I had never had anyone tell me the word racism or try to explain differences, I would not know what those concepts were in the first place. I am not saying that history is unimportant, but that if conversations like this were less… Perhaps children would not repeat the same mistakes. Perhaps children would not see these differences that were taught to me. Perhaps children will be able to read the actual content of a blog post instead of a long rant about equality followed by the content they visited the blog post for in the first place.

![Sam](https://www.gravatar.com/avatar/307ed40831c91ecef54ea2b565425078?r=pg&s=96&d=identicon "Sam")

[Sam](http://darkfunction.com) on [2014-12-28](/posts/301-clojure-from-the-ground-up-welcome#comment-2019)

Thanks for this. You write well and the technical pace is perfect and easy to follow.

![kiquetal](https://www.gravatar.com/avatar/4488d62a8a69ac5cf6910ac0f57114f1?r=pg&s=96&d=identicon "kiquetal")

kiquetal on [2015-01-14](/posts/301-clojure-from-the-ground-up-welcome#comment-2021)

When will the book be available on github?. I love learning xD

![Eugene Goostman](https://www.gravatar.com/avatar/294de3557d9d00b3d2d8a1e6aab028cf?r=pg&s=96&d=identicon "Eugene Goostman")

[Eugene Goostman](http://www.princetonai.com/) on [2015-01-17](/posts/301-clojure-from-the-ground-up-welcome#comment-2044)

Very sad to see that you do not include very clever AI chatterbots as well. Alas, I must search elsewhere to learn Clojure

![Dave Cottlehuber](https://www.gravatar.com/avatar/5cca6682e26539ee49633032eb126fc3?r=pg&s=96&d=identicon "Dave Cottlehuber")

Dave Cottlehuber on [2015-01-26](/posts/301-clojure-from-the-ground-up-welcome#comment-2052)

Thanks for one of the clearest introductions resources for Clojure, and the Jepsen series are an excellent gateway drug to the diabolic and fascinating world of distributed systems.

![Weslley](https://www.gravatar.com/avatar/294de3557d9d00b3d2d8a1e6aab028cf?r=pg&s=96&d=identicon "Weslley")

Weslley on [2015-03-05](/posts/301-clojure-from-the-ground-up-welcome#comment-2146)

Hi, this introduction saved my life in starting to learn Clojure. Congratulations for your effort! Thanks

![Max G. Faraday](https://www.gravatar.com/avatar/8dc1fe8960712ba2df7c3db90c4984dc?r=pg&s=96&d=identicon "Max G. Faraday")

Max G. Faraday on [2015-04-18](/posts/301-clojure-from-the-ground-up-welcome#comment-2301)

Hey, Love this… Great, Exceptional body of work you have here. I am sure I am not the only one with this sentiment: THANK YOU. I have been coding for 24 years now and in Clojure for the last few. Thanks, really good job.

![Sed](https://www.gravatar.com/avatar/294de3557d9d00b3d2d8a1e6aab028cf?r=pg&s=96&d=identicon "Sed")

Sed on [2016-01-03](/posts/301-clojure-from-the-ground-up-welcome#comment-2580)

“People of color” Seriously, dude. Suck my dick.

![Benjohn](https://www.gravatar.com/avatar/ffcb418e17cac2873d611c2b8d8d891c?r=pg&s=96&d=identicon "Benjohn")

Benjohn on [2016-01-05](/posts/301-clojure-from-the-ground-up-welcome#comment-2583)

Hi Aphyr, nice work.

I like you take on introducing quoted symbols before introducing executing them. I think it works too, and it’s definitely not something I’ve seen before. I’m learning Clojure myself (as a long term programmer), but I think this walk through would also work for my son (8), and when they’re a bit older, his siblings. I like how it starts them at the command line, talks them through checking their tool versions, installing, etc. Nice.

I also like your intro. There are a many Clojure guides – but this one is yours. You introduce it how the hell you like! I don’t think you need me to tell you that :-) But other readers, maybe they do.

It’s stuff that still, sadly, needs saying. Not, I think, because people are necessarily bigoted, but because people just don’t appreciate how it is to live the life of others around them. It’s good to be reminded.

![Jack](https://www.gravatar.com/avatar/11a330aa3a8a116a6043b59b4d4ca20b?r=pg&s=96&d=identicon "Jack")

Jack on [2016-04-04](/posts/301-clojure-from-the-ground-up-welcome#comment-2633)

Hy can’t I read something these days without touthing the LGBT shit? IT industry is sick.

![Jack](https://www.gravatar.com/avatar/11a330aa3a8a116a6043b59b4d4ca20b?r=pg&s=96&d=identicon "Jack")

Jack on [2016-04-04](/posts/301-clojure-from-the-ground-up-welcome#comment-2634)

Despite the LGBT syndrome, the article series is simply the best.

![JSteve](https://www.gravatar.com/avatar/dc117c26f9120a8ff3282c8505a168ac?r=pg&s=96&d=identicon "JSteve")

JSteve on [2016-04-07](/posts/301-clojure-from-the-ground-up-welcome#comment-2635)

I wanted to read this article, but the several paragraphs of political BS at the beginning completely turned me off. Yes, I’m a white straight male. No, I don’t have a problem with you (or anyone else) being different. Why don’t you stop worrying about it so much and just focus on the tech? It really has no place in this type of article.

You’re white and male and you spent a lot of time representing females and minorities, which to me makes it sound like YOU consider them second class citizens who are unable to represent themselves. Get off the high horse and write something that doesn’t make me want to barf. I don’t hate you or people like you, but I do hate all this victimization crap.

![Shayne O](https://www.gravatar.com/avatar/efb8f50e6ffe6cfbdc31f308a25606fc?r=pg&s=96&d=identicon "Shayne O")

Shayne O on [2016-09-26](/posts/301-clojure-from-the-ground-up-welcome#comment-2699)

Good work on reaching out to women and other less represented groups. Having youngsters in my family interested in coding, a lot of the attitudes seems discouraging for the girls, so I think its great folks are making an effort to reach out.

Never mind the huffing and puffing troglodytes who weaze and moan everytime someone even suggests including folks often excluded in this industry, they are on the wrong side of history and they know it. Your doing the right thing Kyle. :)

![Terrence Brannon](https://www.gravatar.com/avatar/7618242c9d6ceb32124c9800904c8aa7?r=pg&s=96&d=identicon "Terrence Brannon")

[Terrence Brannon](http://www.metaperl.org) on [2016-12-10](/posts/301-clojure-from-the-ground-up-welcome#comment-2727)

Without this guide, where would one start with Clojure? I dont know and I dont want to entertain the thought.

That being said, you do not need Leinengen to start with clojure. I was behind a corporate firewall teach myself clojure and could not access things like lein through the proxy.

You can just download clojure. And optionally download clojure-mode and inf-clojure from github and away you go!

![vikram](https://www.gravatar.com/avatar/1a23d324a3f5385c3fc01786c5bdc725?r=pg&s=96&d=identicon "vikram")

[vikram](https://www.erptree.com/course/fusion-financials/) on [2017-01-11](/posts/301-clojure-from-the-ground-up-welcome#comment-2737)

It is a very good one can you publish this one as a book?

![Daniel Carpenter](https://www.gravatar.com/avatar/3c18c38162620c52de5b1c79adab8746?r=pg&s=96&d=identicon "Daniel Carpenter")

Daniel Carpenter on [2017-02-01](/posts/301-clojure-from-the-ground-up-welcome#comment-2739)

Thank you so much for this. What a wonderful resource.

![Patrick](https://www.gravatar.com/avatar/3a9da6b673e0b31da07fe059d8485f78?r=pg&s=96&d=identicon "Patrick")

Patrick on [2017-03-24](/posts/301-clojure-from-the-ground-up-welcome#comment-2801)

Thank you for writing this guide, and for your introduction. Both are awesome.

![rosazon](https://www.gravatar.com/avatar/0c0e6cb66c4bb6e39e520e25136c3ea3?r=pg&s=96&d=identicon "rosazon")

rosazon on [2017-03-26](/posts/301-clojure-from-the-ground-up-welcome#comment-2803)

“Who is this guide for?” seriously dude, this does not belong in a technical guide. It doesn’t matter if you’re not straight or whether you’re writing for women or “people of color”. I want to learn Clojure but if you’re going to put all your emphasis on minorities (like me, mind you), then I would rather learn from someone who actually cares about teaching. You should be ashamed.

![Maia](https://www.gravatar.com/avatar/294de3557d9d00b3d2d8a1e6aab028cf?r=pg&s=96&d=identicon "Maia")

Maia on [2017-09-11](/posts/301-clojure-from-the-ground-up-welcome#comment-2895)

Ooooph, some of the comments here. :-\\ I thought your intro was welcoming and personal, and I appreciated it. So far this guide is really helpful and clear, and the order is making more sense to me than other Clojure tutorials I’ve been looking at. Thank you for writing it!

![cig0](https://www.gravatar.com/avatar/22bedcf073a01f4776cfab9eaaa3cdb9?r=pg&s=96&d=identicon "cig0")

[cig0](https://www.linkedin.com/in/martincigorraga/) on [2017-11-12](/posts/301-clojure-from-the-ground-up-welcome#comment-2913)

If you like to avoid as much as possible to clutter your base system with cruft, there’s an official Clojure Docker container ready to rock with everything you might need: [https://hub.docker.com/\_/clojure/](https://hub.docker.com/_/clojure/)

![Pankaj Doharey](https://www.gravatar.com/avatar/788814e1cf0b714c87b845ee840565e1?r=pg&s=96&d=identicon "Pankaj Doharey")

Pankaj Doharey on [2018-07-29](/posts/301-clojure-from-the-ground-up-welcome#comment-2972)

Well Said “All it takes to be a good engineer, scientist, or mathematician is your curiosity, your passion, the right teaching material, and putting in the hours.”

I say the same thing to all the people i have encountered and tried to explain why they can program, but unfortunately most people dont believe themselves.

![Gary Rawlings](https://www.gravatar.com/avatar/30d695948e9cedcec3f7f79f62c71e95?r=pg&s=96&d=identicon "Gary Rawlings")

Gary Rawlings on [2018-09-26](/posts/301-clojure-from-the-ground-up-welcome#comment-2986)

I did a course on LISP at Uni 24+ years ago and haven’t touched the language since, but recently had the need to learn Clojure. What I remember from 24 years ago was that I found it very confusing and went on through my career to focus on OO languages instead.

I wish my lecturer explained it like you have as I thought this was a great intro and makes much more sense!

![Ken](https://www.gravatar.com/avatar/294de3557d9d00b3d2d8a1e6aab028cf?r=pg&s=96&d=identicon "Ken")

Ken on [2018-11-12](/posts/301-clojure-from-the-ground-up-welcome#comment-2995)

I gotta say, regardless of what may or may not be true in your introduction, technical teaching posts/articles/etc. and SJW-ism simply don’t mix.

WALLACE: How are we going to get rid of racism until …?

FREEMAN: Stop talking about it. I’m going to stop calling you a white man. And I’m going to ask you to stop calling me a black man. I know you as Mike Wallace. You know me as Morgan Freeman. You’re not going to say, “I know this white guy named Mike Wallace.” Hear what I’m saying?

![Tod](https://www.gravatar.com/avatar/f6a9ed4d0603452bd4266b1480526b01?r=pg&s=96&d=identicon "Tod")

Tod on [2018-11-28](/posts/301-clojure-from-the-ground-up-welcome#comment-2999)

Kyle, I don’t know. All the personal stuff at the beginning sort of cleared my mind. Then your essay was very clear. Have you read Frege?

I did find your model complex sentence amusing. Is the dog’s mother named ‘Michelle’? And ‘together’ is cruft, redundant. “Lindsay, my best friend, took the dog which we found together at the pound on fourth street, for a walk with her mother Michelle.” But I know what point you were making. Thanks for the write-up. Understand that there is more. Yeah! Going to read again. It was rather soothing.

![](https://www.gravatar.com/avatar/294de3557d9d00b3d2d8a1e6aab028cf?r=pg&s=96&d=identicon)

anonymous on [2021-07-12](/posts/301-clojure-from-the-ground-up-welcome#comment-3494)

真的非常cool

![](https://www.gravatar.com/avatar/294de3557d9d00b3d2d8a1e6aab028cf?r=pg&s=96&d=identicon)

anonymous on [2021-08-03](/posts/301-clojure-from-the-ground-up-welcome#comment-3507)

Nice

![Ahmad ](https://www.gravatar.com/avatar/294de3557d9d00b3d2d8a1e6aab028cf?r=pg&s=96&d=identicon "Ahmad ")

Ahmad on [2022-03-24](/posts/301-clojure-from-the-ground-up-welcome#comment-3711)

Thanks for this guide. Nevermind the people getting furious about a really kind introductory paragraph to make some people that might have a rough time feel included. They could have just skipped that part if they didn’t like it. I think it’s refreshing to see something personal, makes the rest of the guide feel more human. I read your story too and it was touching.

![Andy](https://www.gravatar.com/avatar/18a887613be1838f47055e2c04d639e8?r=pg&s=96&d=identicon "Andy")

Andy on [2023-02-22](/posts/301-clojure-from-the-ground-up-welcome#comment-4254)

An opening paragraph about people being different? You should hear what my C# / Java colleagues think about me being a functional programmer, let alone programming in (dare we say it)… LISP. ;)

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

)