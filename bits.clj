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

  (bit-or
   2r1111
   2r1100)
;;15
  (Integer/toBinaryString
   (bit-or
    2r1111
    2r1100))
;;1111

  (= 15 2r1111)
;;true

  (bit-and
   2r1010
   2r0010)
;;2
  (Integer/toBinaryString
   (bit-and
    2r1010
    2r0010))
;;10

  (bit-not 2r0010)
  ;;-3
  (Integer/toBinaryString
   (bit-not 2r0010))
  ;;11111
  ;;11111111111111111111111111111101

  (Integer/toBinaryString
   (bit-not 0000))
  ;;11111
  ;;"11111111111111111111111111111111"

  (Integer/toBinaryString
   (bit-not 2r1111111111111111111111111111111))
  ;;10000000000000000000000000000000

  (bit-not 0)
  ;;-1

  (bit-flip 2r1011 2)
  ;;15 

  (Integer/toBinaryString
   (bit-flip 2r1011 2))
  ;;1111

  (bit-shift-left 2r0010 2)
  ;;8
  (Integer/toBinaryString
   (bit-shift-left 2r0010 2))
  ;;1000

  
   (bit-flip 2r1111 3)
;;7
  
  (Integer/toBinaryString
   (bit-flip 2r1111 3))
;;111


  :rcf)


