(ns clojusc.colours
  (:require [clojusc.colours.core :as colours]
            [clojusc.colours.attr :as attr])
  (:gen-class))

(defn demo
  "A quick demo."
  [_]
  (println "\nColour library demo ...\n")
  (colours/print (colours/colour attr/fg-green attr/bold) "Hello, ")
  (colours/print (colours/colour attr/fg-cyan) "World")
  (colours/println (colours/colour attr/fg-green attr/bold) "!\n")

  #_{:clj-kondo/ignore [:unresolved-var]}
  (colours/red "This is red text\n")
  #_{:clj-kondo/ignore [:unresolved-var]}
  (colours/green "This is green text\n")
  #_{:clj-kondo/ignore [:unresolved-var]}
  (colours/blue "This is blue text\n")
  (colours/println (colours/colour attr/fg-yellow attr/bold) "Bold yellow text")

  (let [orange (colours/rgb 255 128 0)]
    (colours/println orange "This is orange RGB text"))

  (println "\nString formatting:")
  #_{:clj-kondo/ignore [:unresolved-var]}
  (println "Status:" (colours/green-string "SUCCESS") "- Don't worry, be happy!\n"))

(defn -main
  "Colour library demonstration."
  [& _]
  (demo nil))
