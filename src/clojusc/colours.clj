(ns clojusc.colours
  (:require [clojusc.colours.core :as c]
            [clojusc.colours.attr :as attr])
  (:gen-class))

(defn demo
  "A quick demo."
  [_]
  (println "\nColour library demo ...\n")
  (c/print (c/colour attr/fg-green attr/bold) "Hello, ")
  (c/print (c/colour attr/fg-cyan) "World")
  (c/println (c/colour attr/fg-green attr/bold) "!\n")

  #_{:clj-kondo/ignore [:unresolved-var]}
  (c/red "This is red text\n")
  #_{:clj-kondo/ignore [:unresolved-var]}
  (c/green "This is green text\n")
  #_{:clj-kondo/ignore [:unresolved-var]}
  (c/blue "This is blue text\n")
  (c/println (c/colour attr/fg-yellow attr/bold) "Bold yellow text")

  (let [orange (c/rgb 255 128 0)]
    (c/println orange "This is orange RGB text"))

  (println "\nString formatting:")
  #_{:clj-kondo/ignore [:unresolved-var]}
  (println "Status:" (c/str-green "SUCCESS") "- Don't worry, be happy!\n"))

(defn -main
  "Colour library demonstration."
  [& _]
  (demo nil))
