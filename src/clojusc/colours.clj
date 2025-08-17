(ns clojusc.colours
  (:require [clojusc.colours.core :as colours])
  (:gen-class))

(defn greet
  "Colourful greeting function demonstrating the colour library."
  [data]
  (println "\nColour library demo ...\n")
  (let [name (or (:name data) "World")]
    (colours/print-colour (colours/colour colours/fg-green colours/bold) "Hello, ")
    (colours/print-colour (colours/colour colours/fg-cyan) name)
    (colours/println-colour (colours/colour colours/fg-green colours/bold) "!\n")

    (colours/red "This is red text\n")
    (colours/green "This is green text\n")
    (colours/blue "This is blue text\n")
    (colours/println-colour (colours/colour colours/fg-yellow colours/bold) "Bold yellow text")
    
    (let [orange (colours/rgb 255 128 0)]
      (colours/println-colour orange "This is orange RGB text"))
    
    (println "\nString formatting:")
    (println "Status:" (colours/green-string "SUCCESS") "- Don't worry, be happy!\n")))

(defn -main
  "Colour library demonstration."
  [& args]
  (greet {:name (first args)}))
