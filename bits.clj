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
  
  (bit-set 0 4)
;;16
  
  (Integer/toBinaryString
   (bit-set 0 4))
;;10000
  
  (bit-clear 2r0100 2)
;;0
  
  (bit-test 2r0100 2)
;;true
  
  (bit-test 2r000 2)
;;false
  
  (bit-shift-left 2r0001 2) 
;;4
  (Integer/toBinaryString
   (bit-shift-left 2r0001 2))
;;100
  
  (bit-shift-left 1 4)
;;16
  
  (Integer/toBinaryString
   (bit-shift-left 1 4))
;;10000
  
  (bit-shift-left 2r1 4)
;;16
  
  (bit-shift-left 2r1 2r100)
;;16
  
  (bit-shift-left 2r1 0)  
;;1
  
  (bit-shift-left 2r1 1) 
;;2
  
;  (bit-shift-left 2r1 nil)
;; Execution error


  (bit-shift-left 2r1 1) 
  (bit-shift-left 2r1 2) 
  (bit-shift-left 2r1 3) 
  (bit-shift-left 2r1 4) 
  (bit-shift-left 2r1 5) 
  (bit-shift-left 2r1 6) 
  (bit-shift-left 2r1 7) 

  ;;2,4,8,16,32,64,128
  



  :rcf)


