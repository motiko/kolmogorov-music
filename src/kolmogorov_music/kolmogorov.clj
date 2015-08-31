(ns kolmogorov-music.kolmogorov
  (:require [clojure.repl :as repl]))

(defn in-ns? [sym ns]
  (contains? (ns-interns ns) sym))

(defn sexpr [sym]
  (-> sym repl/source-fn read-string))

(defn definition [sym]
  (-> sym sexpr last))

(declare complexity-sexpr)

(defn complexity-sym [sym ns]
  (if (in-ns? sym ns)
    (->> (definition sym)
         (complexity-sexpr ns))
    0))

(defn complexity-sexpr [ns nested-sexpr]
  (let [sexpr (flatten nested-sexpr)]
    (->> sexpr
       (map #(complexity-sym % ns))
       (reduce + (count sexpr)))))

(defn complexity* [expr]
  (if (seq? expr)
    (complexity-sexpr *ns* expr)
    (complexity-sym expr *ns*)))

(defmacro complexity [expr]
  (complexity* expr))
