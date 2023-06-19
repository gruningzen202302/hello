(ns bits)

(comment
  (defn to-left [n]
    (bit-shift-left 1 n))

  (to-left 4)

  (bit-xor
   2r1100
   2r1110)
;;2

  (Integer/toBinaryString
   (bit-xor
    2r1100
    2r1110))
;;10

  
  
  )