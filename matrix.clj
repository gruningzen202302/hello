(ns matrix
  (:require [clojure.string :as str]))

(def raw 
  "0 4 1 9 3 0 6 11 4 1 0 2 6 5 -4 0")

(def lines (str/split-lines raw))

(print lines)

(def str-to-num (map #(str/split % #" ") lines))

str-to-num

(map identity str-to-num)