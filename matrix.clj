(ns matrix
  (:require [clojure.string :as str]))

(def raw
  "0 4 1 9 3 0 6 11 4 1 0 2 6 5 -4 0")

(def lines (str/split-lines raw))

(print lines)

(def str-to-num (map (fn [args] (str/split args #" ")) lines))

str-to-num

(first str-to-num)

(def number-list (map #(Integer/parseInt %) (first str-to-num)))

number-list

(def length (count number-list))

length

;get the square root of the length
(def sqrt-length (Math/sqrt length))

sqrt-length

(int sqrt-length)

(loop [i 0]
  (when (< i sqrt-length)
    (println (nth number-list i))
    (recur (inc i))))

(loop [i 0]
  (when (< i length)
    (println (nth number-list i))
    (recur (inc i))))

(loop [x 1]
  (when (<= x sqrt-length)
    (println (str "index " x) )
    (recur (+ x 1))))

(reduce + number-list)

(map #(* % %) number-list)

(split-at 4 number-list)

(str/split raw #" ")