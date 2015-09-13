(ns kolmogorov-music.kolmogorov
  (:require [clojure.repl :as repl]
            [midje.sweet :refer :all :exclude [after]]
            [overtone.live :refer :all]
            [leipzig.melody :refer :all]
            [leipzig.scale :as scale]
            [leipzig.live :as live]
            [leipzig.chord :as chord]
            [leipzig.temperament :as temperament]
            [kolmogorov-music.instrument :as instrument]
            [kolmogorov-music.coding :as coding]))

;;;;;;;;;;;;;;;;;;;;;;;;;;
;;; Air on a \G String ;;;
;;;;;;;;;;;;;;;;;;;;;;;;;;

(comment
  (repeat 1000000000000 \G)
  )

(defmacro description-length [expr]
  (-> expr print-str count))

(fact "The description-length is how long the string representation of the expression is."
  (description-length (repeat 65 \G)) => 13)


(defn result-length [result]
  (-> result print-str count))

(fact "The result-length is how long the string representation of the evaluated result is."
  (result-length (repeat 65 \G)) => 131)


(defmacro randomness [expr]
  `(/ (description-length ~expr) (result-length ~expr)))

(fact "Kolmogorov randomness is the compression ratio between the description and the result."
  (randomness (repeat 65 \G)) => 13/131)

(defmacro random? [expr]
  `(>= (randomness ~expr) 1))

(fact "A value is random if its description isn't shorter than its result."
  (random? (repeat 65 \G)) => false
  (random? (->> 66 char (repeat 14) (take 3))) => true)


;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;
;;; Row, row, row your boat ;;;
;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;

;;;;;;;;;;;;;;;;;;;;;;;;;;;;
;;; The Library of Babel ;;;
;;;;;;;;;;;;;;;;;;;;;;;;;;;;

(defn kleene* [elements]
  (letfn [(expand [strings] (for [s strings e elements] (conj s e)))]
    (->>
      (lazy-seq (kleene* elements))
      expand
      (cons []))))

(defn babel []
  (let [ascii (->> (range 32 127) (map char))]
    (->> ascii
       kleene*
       (map (partial apply str)))))

(fact "We can construct all strings as a lazy sequence."
  (->> (babel) (take 5)) => ["" " " "!" "\"" "#"]
  (nth (babel) 364645) => "GEB")

(defn dna []
  (->> "GATC"
       kleene*
       (map (partial apply str))))

(fact "We can construct all genes as a lazy sequence."
  (->> (dna) (take 5)) => ["" "G" "A" "T" "C"]
  (nth (dna) 7154) => "GATTACA")

(fact "Lexicons aren't very random."
  (randomness (take 1000 (babel))) => #(< % 1/100)
  (randomness (take 1000 (dna))) => #(< % 1/100))


;;;;;;;;;;;;;;;;;;;;;
;;; Drawing Hands ;;;
;;;;;;;;;;;;;;;;;;;;;

(defn complexity
  "A hypothetical function that determines the Kolmogorov complexity of any value."
  [string]
  (->> string (map int) (reduce + 0)))

(defmacro enterprise
  "Calculate the shortest string that is more complicated than the specified sym."
  [sym]
  `(let [source# (-> ~sym quote repl/source-fn)]
     (->> (babel)
       (drop-while #(<= (complexity %) (result-length source#)))
       first)))

(defn yo-dawg
  "I heard you like complexity, so I put some enterprise in your enterprise."
  []
  (enterprise enterprise))


;;;;;;;;;;;;;;;;
;;; Contact ;;;;
;;;;;;;;;;;;;;;;

(defn decompose [n]
  (let [[remainder quotient] ((juxt mod quot) n 10)]
    (if (zero? quotient)
      [remainder]
      (conj (decompose quotient) remainder))))

(defn word
  ([from]
   (->> (range)
        (map (partial + from))
        (mapcat decompose)))
  ([]
   (word 0)))

(fact "The Champernowne word is defined by concatenating the natural numbers base 10."
  (->> (word) (take 16)) => [0 1 2 3 4 5 6 7 8 9 1 0 1 1 1 2])


;;;;;;;;;;;;;;;;
;;; Anti EP ;;;;
;;;;;;;;;;;;;;;;

;;;;;;;;;;;;;;;;;;;;;
;;; Blurred Lines ;;;
;;;;;;;;;;;;;;;;;;;;;

; Arrangement
(defmethod live/play-note :default
  [{hertz :pitch seconds :duration}]
  (when hertz (instrument/overchauffeur hertz seconds 0.02)))

(def blurred-lines 12450012001200311273127612731276127312761273127612731276127312761245001200121245001200120031127312761273127612731276127312761273127612731276124500120012124500120012003112731276127312761273127612731276127312761273127612450012001212450012001200311273127612731276127312761273127612731276127312761245001200121240001200120031126812711268127112681271126812711268127112681271124000120012124000120012003112681271126812711268127112681271126812711268127112400012001212400012001200311268127112681271126812711268127112681271126812711240001200121252004100411264125012621249126112471245)

(defn track [start]
  (->>
    (word start)
    (coding/decode 3)
    (wherever :pitch, :pitch temperament/equal)
    (where :time (bpm 120))
    (where :duration (bpm 120))))

(comment

   ; Loop the track, allowing live editing.
  (live/stop)
  (live/play (track row))

  (fx-reverb)
  (fx-chorus)
  (fx-distortion)

  (live/play (track blurred-lines))
  )

;;;;;;;;;;;;;;;;;;;
;;; In the Mood ;;;
;;;;;;;;;;;;;;;;;;;
