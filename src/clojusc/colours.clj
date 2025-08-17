(ns clojusc.colours
  (:require [clojusc.colours.core :as colors])
  (:gen-class))

(defn greet
  "Colorful greeting function demonstrating the color library."
  [data]
  (let [name (or (:name data) "World")]
    (colors/print-color (colors/color colors/fg-green colors/bold) "Hello, ")
    (colors/print-color (colors/color colors/fg-cyan) name)
    (colors/println-color (colors/color colors/fg-green colors/bold) "!")
    
    (println "\nColor library demo:")
    (colors/red "This is red text")
    (colors/green "This is green text")
    (colors/blue "This is blue text")
    (colors/println-color (colors/color colors/fg-yellow colors/bold) "Bold yellow text")
    
    (let [orange (colors/rgb 255 128 0)]
      (colors/println-color orange "This is orange RGB text"))
    
    (println "\nString formatting:")
    (println "Status:" (colors/green-string "SUCCESS") "- All tests passed!")))

(defn -main
  "Color library demonstration."
  [& args]
  (greet {:name (first args)}))
