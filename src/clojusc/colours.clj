(ns clojusc.colours
  (:require [clojusc.colours.core :as colours]
            [clojusc.colours.attr :as attr])
  (:gen-class))

(defn greet
  "Colourful greeting function demonstrating the colour library."
  [data]
  (println "\nColour library demo ...\n")
  (let [name (or (:name data) "World")]
    (colours/print (colours/colour attr/fg-green attr/bold) "Hello, ")
    (colours/print (colours/colour attr/fg-cyan) name)
    (colours/println (colours/colour attr/fg-green attr/bold) "!\n")

    (colours/red "This is red text\n")
    (colours/green "This is green text\n")
    (colours/blue "This is blue text\n")
    (colours/println (colours/colour attr/fg-yellow attr/bold) "Bold yellow text")
    
    (let [orange (colours/rgb 255 128 0)]
      (colours/println orange "This is orange RGB text"))
    
    (println "\nString formatting:")
    (println "Status:" (colours/green-string "SUCCESS") "- Don't worry, be happy!\n")))

(defn -main
  "Colour library demonstration."
  [& args]
  (greet {:name (first args)}))
