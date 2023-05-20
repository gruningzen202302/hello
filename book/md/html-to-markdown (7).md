  Clojure from the ground up: modeling     window.dataLayer = window.dataLayer || \[\]; function gtag(){dataLayer.push(arguments);} gtag('js', new Date()); gtag('config', 'G-MXDP37S6QL');

    

*   [Aphyr](/)
*   [About](/about)
*   [Blog](/posts)
*   [Photos](/photos)
*   [Code](http://github.com/aphyr)

[Clojure from the ground up: modeling](/posts/312-clojure-from-the-ground-up-modeling)
======================================================================================

[Software](/tags/software) [Clojure](/tags/clojure) [Clojure from the ground up](/tags/clojure-from-the-ground-up)

2014-02-19

Previously: [Logistics](http://aphyr.com/posts/311-clojure-from-the-ground-up-logistics)

Until this point in the book, we’ve dealt primarily in specific details: what an expression is, how math works, which functions apply to different data structures, and where code lives. But programming, like speaking a language, painting landscapes, or designing turbines, is about more than the _nuts and bolts_ of the trade. It’s knowing how to _combine_ those parts into a cohesive whole–and this is a skill which is difficult to describe formally. In this part of the book, I’d like to work with you on an integrative tour of one particular problem: modeling a rocket in flight.

We’re going to reinforce our concrete knowledge of the standard library by using maps, sequences, and math functions together. At the same time, we’re going to practice how to represent a complex system; decomposing a problem into smaller parts, naming functions and variables, and writing tests.

[So you want to go to space](#so-you-want-to-go-to-space)
---------------------------------------------------------

First, we need a representation of a craft. The obvious properties for a rocket are its dry mass (how much it weighs without fuel), fuel mass, position, velocity, and time. We’ll create a new file in our scratch project–`src/scratch/rocket.clj`–to talk about spacecraft.

For starters, let’s pattern our craft after an [Atlas V](http://en.wikipedia.org/wiki/Atlas_V) launch vehicle. We’ll represent everything in SI units–kilograms, meters, newtons, etc. The Atlas V carries 627,105 lbs of LOX/RP-1 fuel, and a total mass of 334,500 kg gives only 50,050 kg of mass which _isn’t_ fuel. It develops 4152 kilonewtons of thrust and runs for 253 seconds, with a [specific impulse](http://en.wikipedia.org/wiki/Specific_impulse) (effectively, exhaust velocity) of 3.05 kilometers/sec. Real rockets develop varying amounts of thrust depending on the atmosphere, but we’ll pretend it’s constant in our simulation.

    (defn atlas-v
     []
      {:dry-mass  50050
       :fuel-mass 284450
       :time 0
       :isp 3050
       :max-fuel-rate (/ 284450 253)
       :max-thrust 4.152e6})
    

How heavy is the craft?

    (defn mass
      "The total mass of a craft."
      [craft]
      (+ (:dry-mass craft) (:fuel-mass craft)))
    

What about the position and velocity? We could represent them in Cartesian coordinates–x, y, and z–or we could choose spherical coordinates: a radius from the planet and angle from the pole and 0 degrees longitude. I’ve got a hunch that spherical coordinates will be easier for position, but accelerating the craft will be simplest in in x, y, and z terms. The center of the planet is a natural choice for the coordinate system’s origin (0, 0, 0). We’ll choose z along the north pole, and x and y in the plane of the equator.

Let’s define a space center where we launch from–let’s say it’s initially on the equator at y=0. To figure out the x coordinate, we’ll need to know how far the space center is from the center of the earth. The earth’s [equatorial radius](http://en.wikipedia.org/wiki/Earth_radius#Equatorial_radius) is ~6378 kilometers.

    (def earth-equatorial-radius
     "Radius of the earth, in meters"
      6378137)
    

How fast is the surface moving? Well the earth’s day is 86,400 seconds long,

    (def earth-day
      "Length of an earth day, in seconds."
      86400)
    

which means a given point on the equator has to go 2 \* pi \* equatorial radius meters in earth-day seconds:

    (def earth-equatorial-speed
      "How fast points on the equator move, relative to the center of the earth,
      in meters/sec."
      (/ (* 2 Math/PI earth-equatorial-radius)
         earth-day))
    

So our space center is on the equator (z=0), at y=0 by choice, which means x is the equatorial radius. Since the earth is spinning, the space center is moving at earth-equatorial-speed in the y direction–and not changing at all in x or z.

    (def initial-space-center
      "The initial position and velocity of the launch facility"
      {:time     0
       :position {:x earth-equatorial-radius
                  :y 0
                  :z 0}
       :velocity {:x 0
                  :y earth-equatorial-speed
                  :z 0}})
    

`:position` and `:velocity` are both [vectors](http://en.wikipedia.org/wiki/Euclidean_vector#Representations), in the sense that they describe a position, or a direction, in terms of x, y, and z components. This is a _different_ kind of vector than a Clojure vector, like `[1 2 3]`. We’re actually representing these logical vectors as Clojure _maps_, with `:x`, `:y`, and `:z` keys, corresponding to the distance along the x, y, and z directions, from the center of the earth. Throughout this chapter, I’ll mainly use the term _coordinates_ to talk about these structures, to avoid confusion with Clojure vectors.

Now let’s create a function which positions our craft on the launchpad at time 0. We’ll just _merge_ the spacecraft’s with the initial space center, overwriting the craft’s time and space coordinates.

    (defn prepare
      "Prepares a craft for launch from an equatorial space center."
      [craft]
      (merge craft initial-space-center))
    

[Forces](#forces)
-----------------

Gravity continually pulls the spacecraft towards the center of the Earth, accelerating it by 9.8 meters/second every second. To figure out what direction is towards the Earth, we’ll need the angles of a [spherical coordinate system](http://en.wikipedia.org/wiki/Spherical_coordinate_system). We’ll use the trigonometric functions from [java.lang.Math](http://docs.oracle.com/javase/7/docs/api/java/lang/Math.html).

    (defn magnitude
      "What's the radius of a given set of cartesian coordinates?"
      [c]
      ; By the Pythagorean theorem...
      (Math/sqrt (+ (Math/pow (:x c) 2)
                    (Math/pow (:y c) 2)
                    (Math/pow (:z c) 2))))
    
    (defn cartesian->spherical
      "Converts a map of Cartesian coordinates :x, :y, and :z to spherical coordinates :r, :theta, and :phi."
      [c]
      (let [r (magnitude c)]
        {:r r
         :theta (Math/acos (/ (:z c) r))
         :phi   (Math/atan2 (:y c) (:x c))}))
    
    (defn spherical->cartesian
      "Converts spherical to Cartesian coordinates."
      [c]
      {:x (* (:r c) (Math/sin (:theta c)) (Math/cos (:phi c)))
       :y (* (:r c) (Math/sin (:theta c)) (Math/sin (:phi c)))
       :z (* (:r c) (Math/cos (:phi c)))})
    

With those angles in mind, computing the gravitational acceleration is easy. We take the spherical coordinates of the spacecraft and replace the radius with the total force due to gravity. Then we can transform that spherical force back into Cartesian coordinates.

    (def g "Acceleration of gravity in meters/s^2" -9.8)
    
    (defn gravity-force
      "The force vector, each component in Newtons, due to gravity."
      [craft]
      ; Since force is mass times acceleration...
      (let [total-force (* g (mass craft))]
        (-> craft
            ; Now we'll take the craft's position
            :position
            ; in spherical coordinates,
            cartesian->spherical
            ; replace the radius with the gravitational force...
            (assoc :r total-force)
            ; and transform back to Cartesian-land
            spherical->cartesian)))
    

Rockets produce thrust by consuming fuel. Let’s say the fuel consumption is always the maximum, until we run out:

    (defn fuel-rate
      "How fast is fuel, in kilograms/second, consumed by the craft?"
      [craft]
      (if (pos? (:fuel-mass craft))
        (:max-fuel-rate craft)
        0))
    

Now that we know how much fuel is being consumed, we can compute the force the rocket engine develops. That force is simply the mass consumed per second times the exhaust velocity–which is the specific impulse `:isp`. We’ll ignore atmospheric effects.

    (defn thrust
      "How much force, in newtons, does the craft's rocket engines exert?"
      [craft]
      (* (fuel-rate craft) (:isp craft)))
    

Cool. What about the direction of thrust? Just for grins, let’s keep the rocket pointing entirely along the x axis.

    (defn engine-force
      "The force vector, each component in Newtons, due to the rocket engine."
      [craft]
      (let [t (thrust craft)]
        {:x t
         :y 0
         :z 0}))
    

The total force on a craft is just the sum of gravity and thrust. To sum these maps together, we’ll need a way to sum the x, y, and z components independently. Clojure’s `merge-with` function combines common fields in maps using a function, so this is surprisingly straightforward.

    (defn total-force
      "Total force on a craft."
      [craft]
      (merge-with + (engine-force craft)
                    (gravity-force craft)))
    

The acceleration of a craft, by [Newton’s second law](http://www.physicsclassroom.com/class/newtlaws/u2l3a.cfm), is force divided by mass. This one’s a little trickier; given `{:x 1 :y 2 :z 4}` we want to apply a function–say, multiplication by a factor, to each number. Since maps are sequences of key/value pairs…

    user=> (seq {:x 1 :y 2 :z 3})
    ([:z 3] [:y 2] [:x 1])
    

… and we can build up new maps out of key/value pairs using `into`…

    user=> (into {} [[:x 4] [:y 5]])
    {:x 4, :y 5}
    

… we can write a function `map-values` which works like `map`, but affects the values of a map data structure.

    (defn map-values
      "Applies f to every value in the map m."
      [f m]
      (into {}
            (map (fn [pair]
                   [(key pair) (f (val pair))])
                 m)))
    

And that allows us to define a `scale` function which _scales_ a set of coordinates by some factor:

    (defn scale
      "Multiplies a map of x, y, and z coordinates by the given factor."
      [factor coordinates]
      (map-values (partial * factor) coordinates))
    

What’s that `partial` thing? It’s a function which _takes a function_, and some arguments, and _returns a new function_. What does the new function do? It calls the original function, with the arguments passed to `partial`, followed by any arguments passed to the new function. In short, `(partial * factor)` returns a function that takes any number, and multiplies it by `factor`.

So to divide each component of the force vector by the mass of the craft:

    (defn acceleration
      "Total acceleration of a craft."
      [craft]
      (let [m (mass craft)]
        (scale (/ m) (total-force craft))))
    

Note that `(/ m)` returns 1/m. Our scale function can do double-duty as both multiplication and division.

With the acceleration and fuel consumption all figured out, we’re ready to _apply those changes over time_. We’ll write a function which takes the rocket at a particular time, and returns a version of it `dt` seconds later.

    (defn step
      [craft dt]
      (assoc craft
             ; Time advances by dt seconds
             :t         (+ dt (:t craft))
             ; We burn some fuel
             :fuel-mass (- (:fuel-mass craft) (* dt (fuel-rate craft)))
             ; Our position changes based on our velocity
             :position  (merge-with + (:position craft)
                                      (scale dt (:velocity craft)))
             ; And our velocity changes based on our acceleration
             :velocity  (merge-with + (:velocity craft)
                                      (scale dt (acceleration craft)))))
    

OK. Let’s save the `rocket.clj` file, load that code into the REPL, and fire it up.

    user=> (use 'scratch.rocket :reload)
    nil
    

`use` is like a shorthand for `(:require ... :refer :all)`. We’re passing `:reload` because we want the REPL to re-read the file. Notice that in `ns` declarations, the namespace name `scratch.rocket` is _unquoted_–but when we call `use` or `require` at the repl, we quote the namespace name.

    user=> (atlas-v)
    {:dry-mass 50050, :fuel-mass 284450, :time 0, :isp 3050, :max-fuel-rate 284450/253, :max-thrust 4152000.0}
    

[Launch](#launch)
-----------------

Let’s prepare the rocket. We’ll use `pprint` to print it in a more readable form.

    user=> (-> (atlas-v) prepare pprint)
    {:velocity {:x 0, :y 463.8312116386399, :z 0},
     :position {:x 6378137, :y 0, :z 0},
     :dry-mass 50050,
     :fuel-mass 284450,
     :time 0,
     :isp 3050,
     :max-fuel-rate 284450/253,
     :max-thrust 4152000.0}
    

Great; there it is on the launchpad. Wow, even “standing still”, it’s moving at 463 meters/sec because of the earth’s rotation! That means _you and I_ are flying through space at almost half a kilometer every second! Let’s step forward one second in time.

    user=> (-> (atlas-v) prepare (step 1) pprint)
    
    NullPointerException   clojure.lang.Numbers.ops (Numbers.java:942)
    

In evaluating this expression, Clojure reached a point where it could not continue, and aborted execution. We call this error an _exception_, and the process of aborting _throwing_ the exception. Clojure backs up to the function which _called_ the function that threw, then the function which called _that_ function, and so on, all the way to the top-level expression. The REPL finally intercepts the exception, prints an error to the console, and stashes the exception object in a special variable `*e`.

In this case, we know that the exception in question was a `NullPointerException`, which occurs when a function received `nil` unexpectedly. This one came from `clojure.lang.Numbers.ops`, which suggests some sort of math was involved. Let’s use `pst` to find out where it came from.

    user=> (pst *e)
    NullPointerException 
    	clojure.lang.Numbers.ops (Numbers.java:942)
    	clojure.lang.Numbers.add (Numbers.java:126)
    	scratch.rocket/step (rocket.clj:125)
    	user/eval1478 (NO_SOURCE_FILE:1)
    	clojure.lang.Compiler.eval (Compiler.java:6619)
    	clojure.lang.Compiler.eval (Compiler.java:6582)
    	clojure.core/eval (core.clj:2852)
    	clojure.main/repl/read-eval-print--6588/fn--6591 (main.clj:259)
    	clojure.main/repl/read-eval-print--6588 (main.clj:259)
    	clojure.main/repl/fn--6597 (main.clj:277)
    	clojure.main/repl (main.clj:277)
    	clojure.tools.nrepl.middleware.interruptible-eval/evaluate/fn--589 (interruptible_eval.clj:56)
    

This is called a _stack trace_: the _stack_ is the context of the program at each function call. It traces the path the computer took in evaluating the expression, from the bottom to the top. At the bottom is the REPL, and Clojure compiler. Our code begins at `user/eval1478`–that’s the compiler’s name for the expression we just typed. That function called `scratch.rocket/step`, which in turn called `Numbers.add`, and that called `Numbers.ops`. Let’s start by looking at the last function _we_ wrote before calling into Clojure’s standard library: the `step` function, in `rocket.clj`, on line `125`.

    123  (assoc craft
    124         ; Time advances by dt seconds
    125         :t         (+ dt (:t craft))
    

Ah; we named the time field `:time` earlier, not `:t`. Let’s replace `:t` with `:time`, save the file, and reload.

    user=> (use 'scratch.rocket :reload)
    nil
    user=> (-> (atlas-v) prepare (step 1) pprint)
    {:velocity {:x 0.45154055666826215, :y 463.8312116386399, :z -9.8},
     :position {:x 6378137, :y 463.8312116386399, :z 0},
     :dry-mass 50050,
     :fuel-mass 71681400/253,
     :time 1,
     :isp 3050,
     :max-fuel-rate 284450/253,
     :max-thrust 4152000.0}
    

Look at that! Our position is unchanged (because our velocity was zero), but our _velocity_ has shifted. We’re now moving… wait, -9.8 meters per second _south_? That can’t be right. Gravity points _down_, not sideways. Something must be wrong with our spherical coordinate system. Let’s write a test in `test/scratch/rocket_test.clj` to explore.

    (ns scratch.rocket-test
      (:require [clojure.test :refer :all]
                [scratch.rocket :refer :all]))
    
    (deftest spherical-coordinate-test
      (let [pos {:x 1 :y 2 :z 3}]
        (testing "roundtrip"
          (is (= pos (-> pos cartesian->spherical spherical->cartesian))))))
    

    aphyr@waterhouse:~/scratch$ lein test
    
    lein test scratch.core-test
    
    lein test scratch.rocket-test
    
    lein test :only scratch.rocket-test/spherical-coordinate-test
    
    FAIL in (spherical-coordinate-test) (rocket_test.clj:8)
    roundtrip
    expected: (= pos (-> pos cartesian->spherical spherical->cartesian))
      actual: (not (= {:z 3, :y 2, :x 1} {:x 1.0, :y 1.9999999999999996, :z 1.6733200530681513}))
    
    Ran 2 tests containing 4 assertions.
    1 failures, 0 errors.
    Tests failed.
    

Definitely wrong. Looks like something to do with the z coordinate, since x and y look OK. Let’s try testing a point on the north pole:

    (deftest spherical-coordinate-test
      (testing "spherical->cartesian"
        (is (= (spherical->cartesian {:r 2
                                      :phi 0
                                      :theta 0})
               {:x 0.0 :y 0.0 :z 2.0})))
    
      (testing "roundtrip"
        (let [pos {:x 1.0 :y 2.0 :z 3.0}]
          (is (= pos (-> pos cartesian->spherical spherical->cartesian))))))
    

That checks out OK. Let’s try some values in the repl.

    user=> (cartesian->spherical {:x 0.00001 :y 0.00001 :z 2.0})
    {:r 2.00000000005, :theta 7.071068104411588E-6, :phi 0.7853981633974483}
    user=> (cartesian->spherical {:x 1 :y 2 :z 3})
    {:r 3.7416573867739413, :theta 0.6405223126794245, :phi 1.1071487177940904}
    user=> (spherical->cartesian (cartesian->spherical {:x 1 :y 2 :z 3}))
    {:x 1.0, :y 1.9999999999999996, :z 1.6733200530681513}
    user=> (cartesian->spherical {:x 1 :y 2 :z 0})
    {:r 2.23606797749979, :theta 1.5707963267948966, :phi 1.1071487177940904}
    user=> (cartesian->spherical {:x 1 :y 1 :z 0})
    {:r 1.4142135623730951, :theta 1.5707963267948966, :phi 0.7853981633974483}
    

Oh, wait, that looks odd. `{:x 1 :y 1 :z 0}` is on the equator: phi–the angle from the pole–should be pi/2 or ~1.57, and theta–the angle around the equator–should be pi/4 or 0.78. Those coordinates are reversed! Double-checking our formulas with [Wolfram MathWorld](http://mathworld.wolfram.com/SphericalCoordinates.html) shows that we mixed up phi and theta! Let’s redefine `cartesian->spherical` correctly.

    (defn cartesian->spherical
      "Converts a map of Cartesian coordinates :x, :y, and :z to spherical
      coordinates :r, :theta, and :phi."
      [c]
      (let [r (Math/sqrt (+ (Math/pow (:x c) 2)
                            (Math/pow (:y c) 2)
                            (Math/pow (:z c) 2)))]
        {:r     r
         :phi   (Math/acos (/ (:z c) r))
         :theta (Math/atan2 (:y c) (:x c))}))
    

    aphyr@waterhouse:~/scratch$ lein test
    
    lein test scratch.core-test
    
    lein test scratch.rocket-test
    
    Ran 2 tests containing 5 assertions.
    0 failures, 0 errors.
    

Great. Now let’s check the rocket trajectory again.

    user=> (-> (atlas-v) prepare (step 1) pprint)
    {:velocity
     {:x 0.45154055666826204,
      :y 463.8312116386399,
      :z -6.000769315822031E-16},
     :position {:x 6378137, :y 463.8312116386399, :z 0},
     :dry-mass 50050,
     :fuel-mass 71681400/253,
     :time 1,
     :isp 3050,
     :max-fuel-rate 284450/253,
     :max-thrust 4152000.0}
    

This time, our velocity is increasing in the +x direction, at half a meter per second. We have liftoff!

[Flight](#flight)
-----------------

We have a function that can move the rocket forward by one small step of time, but we’d like to understand the rocket’s trajectory as a _whole_; to see _all_ positions it will take. We’ll use _iterate_ to construct a lazy, infinite sequence of rocket states, each one constructed by stepping forward from the last.

    (defn trajectory
      [dt craft]
      "Returns all future states of the craft, at dt-second intervals."
      (iterate #(step % 1) craft))
    

    user=> (->> (atlas-v) prepare (trajectory 1) (take 3) pprint)
    ({:velocity {:x 0, :y 463.8312116386399, :z 0},
      :position {:x 6378137, :y 0, :z 0},
      :dry-mass 50050,
      :fuel-mass 284450,
      :time 0,
      :isp 3050,
      :max-fuel-rate 284450/253,
      :max-thrust 4152000.0}
     {:velocity
      {:x 0.45154055666826204,
       :y 463.8312116386399,
       :z -6.000769315822031E-16},
      :position {:x 6378137, :y 463.8312116386399, :z 0},
      :dry-mass 50050,
      :fuel-mass 71681400/253,
      :time 1,
      :isp 3050,
      :max-fuel-rate 284450/253,
      :max-thrust 4152000.0}
     {:velocity
      {:x 0.9376544222659078,
       :y 463.83049896253056,
       :z -1.200153863164406E-15},
      :position
      {:x 6378137.451540557,
       :y 927.6624232772798,
       :z -6.000769315822031E-16},
      :dry-mass 50050,
      :fuel-mass 71396950/253,
      :time 2,
      :isp 3050,
      :max-fuel-rate 284450/253,
      :max-thrust 4152000.0})
    

Notice that each map is like a frame of a movie, playing at one frame per second. We can make the simulation more or less accurate by raising or lowering the framerate–adjusting the parameter fed to `trajectory`. For now, though, we’ll stick with one-second intervals.

How high above the surface is the rocket?

    (defn altitude
      "The height above the surface of the equator, in meters."
      [craft]
      (-> craft
          :position
          cartesian->spherical
          :r
          (- earth-equatorial-radius)))
    

Now we can explore the rocket’s path as a series of altitudes over time:

    user=> (->> (atlas-v) prepare (trajectory 1) (map altitude) (take 10) pprint)
    (0.0
     0.016865378245711327
     0.519002066925168
     1.540983198210597
     3.117615718394518
     5.283942770212889
     8.075246102176607
     11.52704851794988
     15.675116359256208
     20.555462017655373)
    

The million dollar question, though, is whether the rocket breaks orbit.

    (defn above-ground?
      "Is the craft at or above the surface?"
      [craft]
      (<= 0 (altitude craft)))
    
    (defn flight
      "The above-ground portion of a trajectory."
      [trajectory]
      (take-while above-ground? trajectory))
    
    (defn crashed?
      "Does this trajectory crash into the surface before 100 hours are up?"
      [trajectory]
      (let [time-limit (* 100 3600)] ; 1 hour
        (not (every? above-ground?
                     (take-while #(<= (:time %) time-limit) trajectory)))))
      
    (defn crash-time
      "Given a trajectory, returns the time the rocket impacted the ground."
      [trajectory]
      (:time (last (flight trajectory))))
    
    (defn apoapsis
      "The highest altitude achieved during a trajectory."
      [trajectory]
      (apply max (map altitude trajectory)))
    
    (defn apoapsis-time
      "The time of apoapsis"
      [trajectory]
      (:time (apply max-key altitude (flight trajectory))))
    

If the rocket goes below ground, we know it crashed. If the rocket stays in orbit, the trajectory will never end. That makes it a bit tricky to tell whether the rocket is in a stable orbit or not, because we can’t ask about every element, or the last element, of an infinite sequence: it’ll take infinite time to evaluate. Instead, we’ll assume that the rocket _should_ crash within the first, say, 100 hours; if it makes it past that point, we’ll assume it made orbit successfully. With these functions in hand, we’ll write a test in `test/scratch/rocket_test.clj` to see whether or not the launch is successful:

    (deftest makes-orbit
      (let [trajectory (->> (atlas-v)
                            prepare
                            (trajectory 1))]
    
        (when (crashed? trajectory)
          (println "Crashed at" (crash-time trajectory) "seconds")
          (println "Maximum altitude" (apoapsis trajectory)
                   "meters at"        (apoapsis-time trajectory) "seconds"))
    
        ; Assert that the rocket eventually made it to orbit.
        (is (not (crashed? trajectory)))))
    

    aphyr@waterhouse:~/scratch$ lein test scratch.rocket-test
    
    lein test scratch.rocket-test
    Crashed at 982 seconds
    Maximum altitude 753838.039645385 meters at 532 seconds
    
    lein test :only scratch.rocket-test/makes-orbit
    
    FAIL in (makes-orbit) (rocket_test.clj:26)
    expected: (not (crashed? trajectory))
      actual: (not (not true))
    
    Ran 2 tests containing 3 assertions.
    1 failures, 0 errors.
    Tests failed.
    

We made it to an altitude of 750 kilometers, and crashed 982 seconds after launch. We’re gonna need a bigger boat.

[Stage II](#stage-ii)
---------------------

The Atlas V isn’t big enough to make it into orbit on its own. It carries a second stage, the [Centaur](http://en.wikipedia.org/wiki/Centaur_(rocket_stage)), which is much smaller and uses [more efficient engines](http://www.astronautix.com/stages/cenaurde.htm).

    (defn centaur
      "The upper rocket stage.
      http://en.wikipedia.org/wiki/Centaur_(rocket_stage)
      http://www.astronautix.com/stages/cenaurde.htm"
      []
      {:dry-mass  2361
       :fuel-mass 13897
       :isp       4354
       :max-fuel-rate (/ 13897 470)})
    

The Centaur lives inside the Atlas V main stage. We’ll re-write `atlas-v` to take an _argument_: its next stage.

    (defn atlas-v
      "The full launch vehicle. http://en.wikipedia.org/wiki/Atlas_V"
      [next-stage]
      {:dry-mass  50050
       :fuel-mass 284450
       :isp 3050
       :max-fuel-rate (/ 284450 253)
       :next-stage next-stage})
    

Now, in our tests, we’ll construct the rocket like so:

      (let [trajectory (->> (atlas-v (centaur))
                            prepare
                            (trajectory 1))]
    

When we exhaust the fuel reserves of the primary stage, we’ll de-couple the main booster from the Centaur. In terms of our simulation, the Atlas V will be _replaced_ by its next stage, the Centaur. We’ll write a function `stage` which separates the vehicles when ready:

    (defn stage
      "When fuel reserves are exhausted, separate stages. Otherwise, return craft
      unchanged."
      [craft]
      (cond
        ; Still fuel left
        (pos? (:fuel-mass craft))
        craft
    
        ; No remaining stages
        (nil? (:next-stage craft))
        craft
    
        ; Stage!
        :else
        (merge (:next-stage craft)
               (select-keys craft [:time :position :velocity]))))
    

We’re using `cond` to handle three distinct cases: where there’s fuel remaining in the craft, where there is no stage to separate, and when we’re ready for stage separation. Separation is easy: we simply return the next stage of the current craft, with the current craft’s time, position, and velocity merged in.

Finally, we’ll have to update our `step` function to take into account the possibility of stage separation.

    (defn step
      [craft dt]
      (let [craft (stage craft)]
        (assoc craft
               ; Time advances by dt seconds
               :time      (+ dt (:time craft))
               ; We burn some fuel
               :fuel-mass (- (:fuel-mass craft) (* dt (fuel-rate craft)))
               ; Our position changes based on our velocity
               :position  (merge-with + (:position craft)
                                      (scale dt (:velocity craft)))
               ; And our velocity changes based on our acceleration
               :velocity  (merge-with + (:velocity craft)
                                      (scale dt (acceleration craft))))))
    

Same as before, only now we call `stage` prior to the physics simulation. Let’s try a launch.

    aphyr@waterhouse:~/scratch$ lein test scratch.rocket-test
    
    lein test scratch.rocket-test
    Crashed at 2415 seconds
    Maximum altitude 4598444.289945109 meters at 1446 seconds
    
    lein test :only scratch.rocket-test/makes-orbit
    
    FAIL in (makes-orbit) (rocket_test.clj:27)
    expected: (not (crashed? trajectory))
      actual: (not (not true))
    
    Ran 2 tests containing 3 assertions.
    1 failures, 0 errors.
    Tests failed.
    

Still crashed–but we increased our apoapsis from 750 kilometers to 4,598 kilometers. That’s plenty high, but we’re still not making orbit. Why? Because we’re going straight up, then straight back down. To orbit, we need to move _sideways_, around the earth.

[Orbital insertion](#orbital-insertion)
---------------------------------------

Our spacecraft is shooting upwards, but in order to remain in orbit around the earth, it has to execute a _second_ burn: an orbital injection maneuver. That injection maneuver is also called a _circularization burn_ because it turns the orbit from an ascending parabola into a circle (or something roughly like it). We don’t need to be precise about circularization–any trajectory that doesn’t hit the planet will suffice. All we have to do is burn towards the horizon, once we get high enough.

To do that, we’ll need to enhance the rocket’s control software. We assumed that the rocket would always thrust in the +x direction; but now we’ll need to thrust in multiple directions. We’ll break up the engine force into two parts: `thrust` (how hard the rocket motor pushes) and `orientation` (which determines the direction the rocket is pointing.)

    (defn unit-vector
      "Scales coordinates to magnitude 1."
      [coordinates]
      (scale (/ (magnitude coordinates)) coordinates))
    
    (defn engine-force
      "The force vector, each component in Newtons, due to the rocket engine."
      [craft]
      (scale (thrust craft) (unit-vector (orientation craft))))
    

We’re taking the orientation of the craft–some coordinates–and scaling it to be of length one with `unit-vector`. Then we’re scaling the orientation vector by the thrust, returning a _thrust vector_.

As we go back and redefine parts of the program, you might see an error like

    Exception in thread "main" java.lang.RuntimeException: Unable to resolve symbol: unit-vector in this context, compiling:(scratch/rocket.clj:69:11)
    	at clojure.lang.Compiler.analyze(Compiler.java:6380)
    	at clojure.lang.Compiler.analyze(Compiler.java:6322)
    

This is a stack trace from the Clojure compiler. It indicates that in `scratch/rocket.clj`, on line `69`, column `11`, we used the symbol `unit-vector`–but it didn’t have a meaning at that point in the program. Perhaps `unit-vector` is defined _below_ that line. There are two ways to solve this.

1.  Organize your functions so that the simple ones come first, and those that depend on them come later. Read this way, namespaces tell a story, progressing from smaller to bigger, more complex problems.
    
2.  Sometimes, ordering functions this way is impossible, or would put related ideas too far apart. In this case you can `(declare unit-vector)` near the top of the namespace. This tells Clojure that `unit-vector` isn’t defined _yet_, but it’ll come later.
    

Now that we’ve broken up `engine-force` into `thrust` and `orientation`, we have to control the thrust properly for our two burns. We’ll start by defining the times for the initial ascent and circularization burn, expressed as vectors of start and end times, in seconds.

    (def ascent
      "The start and end times for the ascent burn."
      [0 3000])
    
    (def circularization
      "The start and end times for the circularization burn."
      [4000 1000])
    

Now we’ll change the thrust by adjusting the rate of fuel consumption. Instead of burning at maximum until running out of fuel, we’ll execute two distinct burns.

    (defn fuel-rate
      "How fast is fuel, in kilograms/second, consumed by the craft?"
      [craft]
      (cond
        ; Out of fuel
        (<= (:fuel-mass craft) 0)
        0
    
        ; Ascent burn
        (<= (first ascent) (:time craft) (last ascent))
        (:max-fuel-rate craft)
    
        ; Circularization burn
        (<= (first circularization) (:time craft) (last circularization))
        (:max-fuel-rate craft)
    
        ; Shut down engines otherwise
        :else 0))
    

We’re using `cond` to express four distinct possibilities: that we’ve run out of fuel, executing either of the two burns, or resting with the engines shut down. Because the comparison function `<=` takes any number of arguments and asserts that they occur in order, expressing intervals like “the time is between the first and last times in the ascent” is easy.

Finally, we need to determine the _direction_ to burn in. This one’s gonna require some tricky linear algebra. You don’t need to worry about the specifics here–the goal is to find out what direction the rocket should burn to fly towards the horizon, in a circle around the planet. We’re doing that by taking the rocket’s velocity vector, and _flattening out_ the velocity towards or away from the planet. All that’s left is the direction the rocket is flying _around_ the earth.

    (defn dot-product
      "Finds the inner product of two x, y, z coordinate maps.
      See http://en.wikipedia.org/wiki/Dot_product."
      [c1 c2]
      (+ (* (:x c1) (:x c2))
         (* (:y c1) (:y c2))
         (* (:z c1) (:z c2))))
    
    (defn projection
      "The component of coordinate map a in the direction of coordinate map b.
      See http://en.wikipedia.org/wiki/Vector_projection."
      [a b]
      (let [b (unit-vector b)]
        (scale (dot-product a b) b)))
    
    (defn rejection
      "The component of coordinate map a *not* in the direction of coordinate map
      b."
      [a b]
      (let [a' (projection a b)]
        {:x (- (:x a) (:x a'))
         :y (- (:y a) (:y a'))
         :z (- (:z a) (:z a'))}))
    

With the mathematical underpinnings ready, we’ll define the orientation control software:

    (defn orientation
      "What direction is the craft pointing?"
      [craft]
      (cond
        ; Initially, point along the *position* vector of the craft--that is
        ; to say, straight up, away from the earth.
        (<= (first ascent) (:time craft) (last ascent))
        (:position craft)
    
        ; During the circularization burn, we want to burn *sideways*, in the
        ; direction of the orbit. We'll find the component of our velocity
        ; which is aligned with our position vector (that is to say, the vertical
        ; velocity), and subtract the vertical component. All that's left is the
        ; *horizontal* part of our velocity.
        (<= (first circularization) (:time craft) (last circularization))
        (rejection (:velocity craft) (:position craft))
    
        ; Otherwise, just point straight ahead.
        :else (:velocity craft)))
    

For the ascent burn, we’ll push straight away from the planet. For circularization, we use the `rejection` function to find the part of the velocity around the planet, and thrust in that direction. By default, we’ll leave the rocket pointing in the direction of travel.

With these changes made, the rocket should execute two burns. Let’s re-run the tests and see.

    aphyr@waterhouse:~/scratch$ lein test scratch.rocket-test
    
    lein test scratch.rocket-test
    
    Ran 2 tests containing 3 assertions.
    0 failures, 0 errors.
    

We finally did it! We’re _rocket scientists_!

[Review](#review)
-----------------

    (ns scratch.rocket)
    
    ;; Linear algebra for {:x 1 :y 2 :z 3} coordinate vectors.
    
    (defn map-values
      "Applies f to every value in the map m."
      [f m]
      (into {}
            (map (fn [pair]
                   [(key pair) (f (val pair))])
                 m)))
    
    (defn magnitude
      "What's the radius of a given set of cartesian coordinates?"
      [c]
      ; By the Pythagorean theorem...
      (Math/sqrt (+ (Math/pow (:x c) 2)
                    (Math/pow (:y c) 2)
                    (Math/pow (:z c) 2))))
    
    (defn scale
      "Multiplies a map of x, y, and z coordinates by the given factor."
      [factor coordinates]
      (map-values (partial * factor) coordinates))
    
    (defn unit-vector
      "Scales coordinates to magnitude 1."
      [coordinates]
      (scale (/ (magnitude coordinates)) coordinates))
    
    (defn dot-product
      "Finds the inner product of two x, y, z coordinate maps. See
      http://en.wikipedia.org/wiki/Dot_product"
      [c1 c2]
      (+ (* (:x c1) (:x c2))
         (* (:y c1) (:y c2))
         (* (:z c1) (:z c2))))
    
    (defn projection
      "The component of coordinate map a in the direction of coordinate map b.
      See http://en.wikipedia.org/wiki/Vector_projection."
      [a b]
      (let [b (unit-vector b)]
        (scale (dot-product a b) b)))
    
    (defn rejection
      "The component of coordinate map a *not* in the direction of coordinate map
      b."
      [a b]
      (let [a' (projection a b)]
        {:x (- (:x a) (:x a'))
         :y (- (:y a) (:y a'))
         :z (- (:z a) (:z a'))}))
    
    ;; Coordinate conversion
    
    (defn cartesian->spherical
      "Converts a map of Cartesian coordinates :x, :y, and :z to spherical
      coordinates :r, :theta, and :phi."
      [c]
      (let [r (magnitude c)]
        {:r     r
         :phi   (Math/acos (/ (:z c) r))
         :theta (Math/atan2 (:y c) (:x c))}))
    
    (defn spherical->cartesian
      "Converts spherical to Cartesian coordinates."
      [c]
      {:x (* (:r c) (Math/cos (:theta c)) (Math/sin (:phi c)))
       :y (* (:r c) (Math/sin (:theta c)) (Math/sin (:phi c)))
       :z (* (:r c) (Math/cos (:phi c)))})
    
    ;; The earth
    
    (def earth-equatorial-radius
      "Radius of the earth, in meters"
      6378137)
    
    (def earth-day
      "Length of an earth day, in seconds."
      86400)
    
    (def earth-equatorial-speed
      "How fast points on the equator move, relative to the center of the earth, in
      meters/sec."
      (/ (* 2 Math/PI earth-equatorial-radius)
         earth-day))
    
    (def g "Acceleration of gravity in meters/s^2" -9.8)
    
    (def initial-space-center
      "The initial position and velocity of the launch facility"
      {:time     0
       :position {:x earth-equatorial-radius
                  :y 0
                  :z 0}
       :velocity {:x 0
                  :y earth-equatorial-speed
                  :z 0}})
    
    
    ;; Craft
    
    (defn centaur
      "The upper rocket stage.
      http://en.wikipedia.org/wiki/Centaur_(rocket_stage)
      http://www.astronautix.com/stages/cenaurde.htm"
      []
      {:dry-mass  2361
       :fuel-mass 13897
       :isp       4354
       :max-fuel-rate (/ 13897 470)})
    
    (defn atlas-v
      "The full launch vehicle. http://en.wikipedia.org/wiki/Atlas_V"
      [next-stage]
      {:dry-mass  50050
       :fuel-mass 284450
       :isp 3050
       :max-fuel-rate (/ 284450 253)
       :next-stage next-stage})
    
    ;; Flight control
    
    (def ascent
      "The start and end times for the ascent burn."
      [0 300])
    
    (def circularization
      "The start and end times for the circularization burn."
      [400 1000])
    
    (defn orientation
      "What direction is the craft pointing?"
      [craft]
      (cond
        ; Initially, point along the *position* vector of the craft--that is
        ; to say, straight up, away from the earth.
        (<= (first ascent) (:time craft) (last ascent))
        (:position craft)
    
        ; During the circularization burn, we want to burn *sideways*, in the
        ; direction of the orbit. We'll find the component of our velocity
        ; which is aligned with our position vector (that is to say, the vertical
        ; velocity), and subtract the vertical component. All that's left is the
        ; *horizontal* part of our velocity.
        (<= (first circularization) (:time craft) (last circularization))
        (rejection (:velocity craft) (:position craft))
    
        ; Otherwise, just point straight ahead.
        :else (:velocity craft)))
    
    (defn fuel-rate
      "How fast is fuel, in kilograms/second, consumed by the craft?"
      [craft]
      (cond
        ; Out of fuel
        (<= (:fuel-mass craft) 0)
        0
    
        ; Ascent burn
        (<= (first ascent) (:time craft) (last ascent))
        (:max-fuel-rate craft)
    
        ; Circularization burn
        (<= (first circularization) (:time craft) (last circularization))
        (:max-fuel-rate craft)
    
        ; Shut down engines otherwise
        :else 0))
    
    (defn stage
      "When fuel reserves are exhausted, separate stages. Otherwise, return craft
      unchanged."
      [craft]
      (cond
        ; Still fuel left
        (pos? (:fuel-mass craft))
        craft
    
        ; No remaining stages
        (nil? (:next-stage craft))
        craft
    
        ; Stage!
        :else
        (merge (:next-stage craft)
               (select-keys craft [:time :position :velocity]))))
    
    ;; Dynamics
    
    (defn thrust
      "How much force, in newtons, does the craft's rocket engines exert?"
      [craft]
      (* (fuel-rate craft) (:isp craft)))
    
    (defn mass
      "The total mass of a craft."
      [craft]
      (+ (:dry-mass craft) (:fuel-mass craft)))
    
    (defn gravity-force
      "The force vector, each component in Newtons, due to gravity."
      [craft]
      ; Since force is mass times acceleration...
      (let [total-force (* g (mass craft))]
        (-> craft
            ; Now we'll take the craft's position
            :position
            ; in spherical coordinates,
            cartesian->spherical
            ; replace the radius with the gravitational force...
            (assoc :r total-force)
            ; and transform back to Cartesian-land
            spherical->cartesian)))
    
    (declare altitude)
    
    (defn engine-force
      "The force vector, each component in Newtons, due to the rocket engine."
      [craft]
    ;  Debugging; useful for working through trajectories in detail.
    ;  (println craft)
    ;  (println "t   " (:time craft) "alt" (altitude craft) "thrust" (thrust craft))
    ;  (println "fuel" (:fuel-mass craft))
    ;  (println "vel " (:velocity craft))
    ;  (println "ori " (unit-vector (orientation craft)))
      (scale (thrust craft) (unit-vector (orientation craft))))
    
    (defn total-force
      "Total force on a craft."
      [craft]
      (merge-with + (engine-force craft)
                  (gravity-force craft)))
    
    (defn acceleration
      "Total acceleration of a craft."
      [craft]
      (let [m (mass craft)]
        (scale (/ m) (total-force craft))))
    
    (defn step
      [craft dt]
      (let [craft (stage craft)]
        (assoc craft
               ; Time advances by dt seconds
               :time      (+ dt (:time craft))
               ; We burn some fuel
               :fuel-mass (- (:fuel-mass craft) (* dt (fuel-rate craft)))
               ; Our position changes based on our velocity
               :position  (merge-with + (:position craft)
                                      (scale dt (:velocity craft)))
               ; And our velocity changes based on our acceleration
               :velocity  (merge-with + (:velocity craft)
                                      (scale dt (acceleration craft))))))
    
    ;; Launch and flight
    
    (defn prepare
      "Prepares a craft for launch from an equatorial space center."
      [craft]
      (merge craft initial-space-center))
    
    (defn trajectory
      [dt craft]
      "Returns all future states of the craft, at dt-second intervals."
      (iterate #(step % 1) craft))
    
    ;; Analyzing trajectories
    
    (defn altitude
      "The height above the surface of the equator, in meters."
      [craft]
      (-> craft
          :position
          cartesian->spherical
          :r
          (- earth-equatorial-radius)))
    
    (defn above-ground?
      "Is the craft at or above the surface?"
      [craft]
      (<= 0 (altitude craft)))
    
    (defn flight
      "The above-ground portion of a trajectory."
      [trajectory]
      (take-while above-ground? trajectory))
    
    (defn crashed?
      "Does this trajectory crash into the surface before 10 hours are up?"
      [trajectory]
      (let [time-limit (* 10 3600)] ; 10 hours
        (not (every? above-ground?
                     (take-while #(<= (:time %) time-limit) trajectory)))))
    
    (defn crash-time
      "Given a trajectory, returns the time the rocket impacted the ground."
      [trajectory]
      (:time (last (flight trajectory))))
    
    (defn apoapsis
      "The highest altitude achieved during a trajectory."
      [trajectory]
      (apply max (map altitude (flight trajectory))))
    
    (defn apoapsis-time
      "The time of apoapsis"
      [trajectory]
      (:time (apply max-key altitude (flight trajectory))))
    

As written here, our first non-trivial program tells a story–though a _different_ one than the process of exploration and refinement that brought the rocket to orbit. It builds from small, abstract ideas: linear algebra and coordinates; physical constants describing the universe for the simulation; and the basic outline of the spacecraft. Then we define the software controlling the rocket; the times for the burns, how much to thrust, in what direction, and when to separate stages. Using those control functions, we build a _physics engine_ including gravity and thrust forces, and use Newton’s second law to build a basic [Euler Method](http://en.wikipedia.org/wiki/Euler_method) solver. Finally, we analyze the trajectories the solver produces to answer key questions: how high, how long, and did it explode?

We used Clojure’s immutable data structures–mostly maps–to represent the state of the universe, and defined _pure functions_ to interpret those states and construct new ones. Using `iterate`, we projected a single state forward into an infinite timeline of the future–evaluated as demanded by the analysis functions. Though we pay a performance penalty, immutable data structures, pure functions, and lazy evaluation make simulating complex systems easier to reason about.

Had we written this simulation in a different language, different techniques might have come into play. In Java, C++, or Ruby, we would have defined a hierarchy of datatypes called _classes_, each one representing a small piece of state. We might define a `Craft` type, and created subtypes `Atlas` and `Centaur`. We’d create a `Coordinate` type, subdivided into `Cartesian` and `Spherical`, and so on. The types add complexity and rigidity, but also prevent mis-spellings, and can prevent us from interpreting, say, coordinates as craft or vice-versa.

To move the system forward in a language emphasizing _mutable_ data structures, we would have updated the time and coordinates of a single craft in-place. This introduces additional complexity, because many of the changes we made depended on the current values of the craft. To ensure the correct ordering of calculations, we’d scatter temporary variables and explicit copies throughout the code, ensuring that functions didn’t see inconsistent pictures of the craft state. The mutable approach would likely be faster, but would still demand some copying of data, and sacrifice clarity.

More _imperative_ languages place less emphasis on laziness, and make it harder to express ideas like `map` and `take`. We might have simulated the trajectory for some fixed time, constructing a history of all the intermediate results we needed, then analyzed it by moving explicitly from slot to slot in that history, checking if the craft had crashed, and so on.

Across all these languages, though, some ideas remain the same. We solve big problems by breaking them up into smaller ones. We use data structures to represent the state of the system, and functions to alter that state. Comments and docstrings clarify the _story_ of the code, making it readable to others. Tests ensure the software is correct, and allow us to work piecewise towards a solution.

[Exercises](#exercises)
-----------------------

1.  We know the spacecraft reached orbit, but we have no idea what that orbit _looks_ like. Since the trajectory is infinite in length, we can’t ask about the _entire_ history using `max`–but we know that all orbits have a high and low point. At the highest point, the difference between successive altitudes changes from increasing to decreasing, and at the lowest point, the difference between successive altitudes changes from decreasing to increasing. Using this technique, refine the `apoapsis` function to find the highest point using that _inflection_ in altitudes–and write a corresponding `periapsis` function that finds the lowest point in the orbit. Display both periapsis and apoapsis in the test suite.
    
2.  We assumed the force of gravity resulted in a constant 9.8 meter/second/second acceleration towards the earth, but in the real world, gravity falls off with the [inverse square law](http://en.wikipedia.org/wiki/Newton's_law_of_universal_gravitation). Using the mass of the earth, mass of the spacecraft, and Newton’s constant, refine the gravitational force used in this simulation to take Newton’s law into account. How does this affect the apoapsis?
    
3.  We ignored the atmosphere, which exerts [drag](http://en.wikipedia.org/wiki/Drag_(physics)) on the craft as it moves through the air. Write a basic air-density function which falls off with altitude. Make some educated guesses as to how much drag a real rocket experiences, and assume that the drag force is proportional to the square of the rocket’s velocity. Can your rocket still reach orbit?
    
4.  Notice that the periapsis and apoapsis of the rocket are _different_. By executing the circularization burn carefully, can you make them the same–achieving a perfectly circular orbit? One way to do this is to pick an orbital altitude and velocity of a known satellite–say, the International Space Station–and write the control software to match that velocity at that altitude.
    

In the next chapter, we talk about [debugging](https://aphyr.com/posts/319-clojure-from-the-ground-up-debugging).

![Donald Parish](https://www.gravatar.com/avatar/244743d581b011243b0a4670e43f1d2e?r=pg&s=96&d=identicon "Donald Parish")

Donald Parish on [2014-02-27](/posts/312-clojure-from-the-ground-up-modeling#comment-1830)

Great addition. One of my first programs was a TRS-80 simulation of a space ship orbiting a planet, I hope to interest my son in using it for his physics class.

![Honza](https://www.gravatar.com/avatar/94e6c47b2f9a76cd39f5c7b7c8ad3a36?r=pg&s=96&d=identicon "Honza")

[Honza](http://honza.ca) on [2014-02-28](/posts/312-clojure-from-the-ground-up-modeling#comment-1831)

There is a tiny typo in the definition of `trajectory`. The `dt` argument is ignored and `1` is hard-coded.

![](https://www.gravatar.com/avatar/294de3557d9d00b3d2d8a1e6aab028cf?r=pg&s=96&d=identicon)

anonymous on [2014-03-01](/posts/312-clojure-from-the-ground-up-modeling#comment-1832)

This is a nice intro to functional techniques, but there are hundreds of these around the web. What’s really missing is more and better examples of dealing with resource management and side effects in FP.

![Noah Easterly](https://www.gravatar.com/avatar/33ca8dd44993cafe3c851c9323111987?r=pg&s=96&d=identicon "Noah Easterly")

Noah Easterly on [2014-03-02](/posts/312-clojure-from-the-ground-up-modeling#comment-1833)

Shouldn’t the `atlas-v` use its `next-stage`’s mass to determine its `dry-mass`?

Great series, thanks!

![Matt](https://www.gravatar.com/avatar/7f2b79cd5329a2c04562a873211e5d16?r=pg&s=96&d=identicon "Matt")

[Matt](http://google.com) on [2014-03-08](/posts/312-clojure-from-the-ground-up-modeling#comment-1838)

Noah is right, your mass function should be adding in the upper stage for the fuel and dry mass. If you modify the function to add that, then the craft can no longer make it into orbit either!

![Lauri](https://www.gravatar.com/avatar/f8db1aa9a5989dda731f9005dbf4a8b8?r=pg&s=96&d=identicon "Lauri")

[Lauri](https://blog.lauripesonen.com) on [2014-03-13](/posts/312-clojure-from-the-ground-up-modeling#comment-1844)

What a great post! There are a few bugs in the code if you follow along with the snippets:

*   the `makes-orbit` test should use `(flight trajectory)` rather than `trajectory` when analyzing the crash time etc.
    
*   `ascent` should be defined as `[0 300]` and `circularization` as `[400 1000]` for the tests to pass
    

![Ivan](https://www.gravatar.com/avatar/7f462de32218dc5e8dbdf0496ae8a957?r=pg&s=96&d=identicon "Ivan")

Ivan on [2014-04-01](/posts/312-clojure-from-the-ground-up-modeling#comment-1853)

Very interesting tutorial, thanks!

Just a question, do you have anything against records and protocols? As far I understand this is be suitable for model data, as presented here. It would be safer than using only maps. Maybe, you consider this approach is too OO? Or you just want to keep it simple?

![Ivan](https://www.gravatar.com/avatar/7f462de32218dc5e8dbdf0496ae8a957?r=pg&s=96&d=identicon "Ivan")

Ivan on [2014-04-01](/posts/312-clojure-from-the-ground-up-modeling#comment-1854)

Well, now I see you use e.g.

(merge craft initial-space-center)

not sure if this would be possible using records (fresh Clojure noob here). So probably you prefer maps for flexibility? Is there a way to combine this with records or protocols to make it safer, or would you stick with this in a serious / bigger app?

Aphyr on [2014-04-19](/posts/312-clojure-from-the-ground-up-modeling#comment-1858)

> Just a question, do you have anything against records and protocols?

Protocols are strictly for polymorphism, and since we don’t have different types of records here, there’s no need for a protocol. We’ll be addressing polymorphism in a later chapter, though. :)

Records have two advantages over maps: first, lookup for the basis fields in a record is an order of magnitude faster than a hashmap, and second, records can specify implementations of polymorphic functions (namely, via protocols and interfaces). They don’t provide type safety for their fields (you can happily assign a Cat to a Dog field) and don’t constrain their fields (you can assoc arbitrary keys into a record, just like a map), so they don’t really provide the sort of type safety you’d be looking for in a typed Object or algebraic datatype.

Because Records print differently and have a few subtle API differences, and because I’m trying to give learners a chance to settle in to the core data abstractions before moving up to polymorphism and types, this chapter sticks to maps. :)

![Aphyr](https://www.gravatar.com/avatar/e145b50faf662e70c066b13c98921900?r=pg&s=96&d=identicon "Aphyr")

![flo](https://www.gravatar.com/avatar/e91e9c0f1c646941d149f2d7cc8b12da?r=pg&s=96&d=identicon "flo")

flo on [2014-05-12](/posts/312-clojure-from-the-ground-up-modeling#comment-1864)

Hey,

your posts are my first dive into clojure and you’re doing a really good job at explaining. Keep up the great work!!!

flo

![Daniel](https://www.gravatar.com/avatar/3593c89d10655366b75059f23c42ba64?r=pg&s=96&d=identicon "Daniel")

Daniel on [2014-05-24](/posts/312-clojure-from-the-ground-up-modeling#comment-1882)

I love this series. Thank you. A few suggestions for making it better:

There are two typos in the narrative that get corrected in the review, but which made following along a bit more difficult:

*   apoapsis uses (map altitude trajectory) in your narrative and (map altitude (flight trajectory)) in review, and my tests were running forever
*   ascent and circularization have typos in the narrative which prevent the circularization stage from ever being triggered - 3000 instead of 300 and 4000 instead of 400

I would also recommend promising the orientation function later while you’re talking about unit-vector and engine-force under Orbital insertion. While I was working on this I thought you had just forgotten to include it, and tried to work it out myself.

Thanks again!

![utkarsh](https://www.gravatar.com/avatar/01361b8a0d84f9ca4e656512db91324e?r=pg&s=96&d=identicon "utkarsh")

utkarsh on [2015-10-23](/posts/312-clojure-from-the-ground-up-modeling#comment-2546)

great series dear friend. You take great pains for our understanding. The capability of clojure to manipulate data structures with some cool in-built functions is exemplary. Do any other languages offer such functions to work with DSs and manipulate them?

regards utkarsh

![Yoav Kleinberger](https://www.gravatar.com/avatar/963d9854afe50c4ff9943e09d33233be?r=pg&s=96&d=identicon "Yoav Kleinberger")

Yoav Kleinberger on [2016-03-02](/posts/312-clojure-from-the-ground-up-modeling#comment-2621)

Great work!

A pedantic physics point: on the equator, if y=0 and z=0, x can be either R _or_ -R. You of course can choose where to put your launcher, but it’s a choice, not an inference. Not really important, just something I noticed.

![Adam](https://www.gravatar.com/avatar/14e5f1aaa71010256db8840f783a4e32?r=pg&s=96&d=identicon "Adam")

[Adam](https://gashlin.net) on [2022-02-03](/posts/312-clojure-from-the-ground-up-modeling#comment-3675)

Thanks for the tutorial! A few more notes years too late:

`spherical->cartesian` is initially written with cos and sin reversed for `:x`, though this is corrected in the final version.

`cartesian->spherical` won’t roundtrip in the negative x hemisphere, as the Wolfram page says, “the inverse tangent must be suitably defined to take the correct quadrant of (x,y) into account.” This would be handled by `(Math/atan2 (:y c) (:x c))`. (It’s also called `cartesian->polar` once.)

To Noah and Matt’s point above, the first stage dry mass of 50050 already takes into account the total mass of the second stage and likely a payload. Wikipedia says that the first stage only has a dry mass of about 21,000 kg, add the 16,258 total mass of the second stage and there’s still ~13,000 left, which looks like a reasonable payload to low-earth orbit.

![Adam](https://www.gravatar.com/avatar/14e5f1aaa71010256db8840f783a4e32?r=pg&s=96&d=identicon "Adam")

[Adam](https://gashlin.net) on [2022-02-03](/posts/312-clojure-from-the-ground-up-modeling#comment-3676)

Although, if there’s a 13,000 kg payload, that would be included in the dry mass of the second stage, and with that added there’s a crash, even if the payload continues as a separate stage. Anyway, this is irrelevant, it was a good example.

Aphyr on [2022-02-11](/posts/312-clojure-from-the-ground-up-modeling#comment-3683)

> spherical->cartesian is initially written with cos and sin reversed for :x, though this is corrected in the final version.

Yep! This is (unless I’ve made _multiple_ mistakes here, which is–knowing me–entirely possible) an intentional bug–the chapter walks you through detecting and resolving it.

> cartesian->spherical won’t roundtrip in the negative x hemisphere, as the Wolfram page says, “the inverse tangent must be suitably defined to take the correct quadrant of (x,y) into account.” This would be handled by (Math/atan2 (:y c) (:x c)). (It’s also called cartesian->polar once.)

Quite right, thank you. Fixed!

![Aphyr](https://www.gravatar.com/avatar/e145b50faf662e70c066b13c98921900?r=pg&s=96&d=identicon "Aphyr")

![matt](https://www.gravatar.com/avatar/cacc2bce062d07c4b80213540d6f4050?r=pg&s=96&d=identicon "matt")

matt on [2022-07-17](/posts/312-clojure-from-the-ground-up-modeling#comment-3817)

Hey, first off – thanks for this amazing tutorial series, I discovered it several years ago and it helped me tremendously in learning Clojure and enabling a career I didn’t expect to end up in. I now often use Clojure professionally in software I write for my research / simulations. Every so often I go through this series for practice and nostalgia; always hoping for more additions to the series (hopefully more modeling and simulation ones similar to this post!)

I’d like to follow up on Adam’s comments from a few months ago – in the correction you made to the cartesian->spherical function (as written in the snippets and full source at the end) it produces a syntax error.

`(Math/atan2 (:y c) (:x c)` expects two args.

As written in the version of the post I’m reading, the function is receiving a single arg which is the return values of the division of the key extracted coordinate values. I believe Adam was correct in that it should be:

`(Math/atan2 (:y c) (:x c))`

_not_

`(Math/atan2 (/ (:y c) (:x c))`

Which I believe was a holdover typo that caused it to pass one argument and produce a syntax error.

Unless I’m mistaken or misunderstanding something, I believe this will fix the issue. Anyway, thanks again. I sincerely want to thank you for this series and hope there will be many more to follow.

Aphyr on [2022-07-18](/posts/312-clojure-from-the-ground-up-modeling#comment-3819)

> I believe Adam was correct in that it should be `(Math/atan2 (:y c) (:x c))` not `(Math/atan2 (/ (:y c) (:x c))`

Ah, yes, that does sound right! My sincere apologies for the confusion. I’m delighted that this series helped in your career! :-)

![Aphyr](https://www.gravatar.com/avatar/e145b50faf662e70c066b13c98921900?r=pg&s=96&d=identicon "Aphyr")

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