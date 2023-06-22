(ns matrix
  (:require [clojure.string :as str]))

(def raw 
  "0 4 1 9 3 0 6 11 4 1 0 2 6 5 -4 0")

(def lines (str/split-lines raw))

(print lines)

(def str-to-num (map (fn[args](str/split args #" ")) lines))

str-to-num

(first str-to-num)

(def number-list (map #(Integer/parseInt %) (first str-to-num)))

number-list 