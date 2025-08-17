(ns clojusc.colours.core
  "Main public API for the Clojure colour library"
  (:require [clojusc.colours.attr :as attr]
            [clojusc.colours.colour :as colour]
            [clojusc.colours.rgb :as rgb]
            [clojusc.colours.print :as print]
            [clojusc.colours.ansi :as ansi])
  (:import [clojusc.colours.colour Colour]
           [clojusc.colours.rgb RGBColour])
  (:refer-clojure :exclude [print, println]))

;; Colour creation
(defn colour
  "Create a new colour with the given attributes"
  [& attributes]
  (colour/create-colour attributes))

(defn rgb
  "Create RGB foreground colour"
  [r g b]
  (rgb/fg-colour r g b))

(defn rgb-bg
  "Create RGB background colour"
  [r g b]
  (rgb/bg-colour r g b))

;; Colour manipulation
(defn add
  "Add attributes to a colour"
  [colour & attributes]
  (apply colour/add-attrs colour attributes))

(defn combine
  "Combine two colours"
  [colour1 colour2]
  (colour/op :combine colour1 colour2))

(defn enable-colour
  "Enable colour output for a colour"
  [colour]
  (colour/op :enable colour))

(defn disable-colour
  "Disable colour output for a colour"
  [colour]
  (colour/op :disable colour))

;; String operations
(defn colourize
  "Apply colour to text string"
  [colour text]
  (ansi/colourize colour text))

(defn strip
  "Remove ANSI colour codes from text"
  [text]
  (ansi/strip (colour/create-colour []) text))

;; Printing functions
(defn print
  "Print coloured text"
  [colour text]
  (print/print-with-colour colour text))

(defn println
  "Print coloured text with newline"
  [colour text]
  (print/println-with-colour colour text))

(defn printf-colour
  "Printf with colour"
  [colour format-str & args]
  (apply print/printf-with-colour colour format-str args))

;; Convenient colour functions (like Go's colour.Red(), colour.Green(), etc.)
(defmacro defcolour
  "Define a convenient colour function"
  [name attr]
  `(defn ~name
     ([text#] (~name "%s" text#))
     ([format-str# & args#]
      (let [colour# (colour/create-colour [~attr])]
        (apply print/printf-with-colour colour# (str format-str#) args#)))))

(defcolour red attr/fg-red)
(defcolour green attr/fg-green)
(defcolour blue attr/fg-blue)
(defcolour yellow attr/fg-yellow)
(defcolour cyan attr/fg-cyan)
(defcolour magenta attr/fg-magenta)
(defcolour white attr/fg-white)
(defcolour black attr/fg-black)

;; String formatting functions (like Go's colour.RedString())
(defmacro defcolour-string
  "Define a colour string function"
  [name attr]
  `(defn ~(symbol (str name "-string"))
     ([text#] (~(symbol (str name "-string")) "%s" text#))
     ([format-str# & args#]
      (let [colour# (colour/create-colour [~attr])]
        (apply print/format-coloured colour# format-str# args#)))))

(defcolour-string red attr/fg-red)
(defcolour-string green attr/fg-green)
(defcolour-string blue attr/fg-blue)
(defcolour-string yellow attr/fg-yellow)
(defcolour-string cyan attr/fg-cyan)
(defcolour-string magenta attr/fg-magenta)
(defcolour-string white attr/fg-white)
(defcolour-string black attr/fg-black)

;; Global colour control
(defn set-no-colour!
  "Globally disable colour output"
  [disabled?]
  (alter-var-root #'print/*no-colour* (constantly disabled?)))

(defn no-colour?
  "Check if colour output is globally disabled"
  []
  print/*no-colour*)

;; High-intensity colour variants
(def hi-red (colour/create-colour [attr/fg-hi-red]))
(def hi-green (colour/create-colour [attr/fg-hi-green]))
(def hi-blue (colour/create-colour [attr/fg-hi-blue]))
(def hi-yellow (colour/create-colour [attr/fg-hi-yellow]))
(def hi-cyan (colour/create-colour [attr/fg-hi-cyan]))
(def hi-magenta (colour/create-colour [attr/fg-hi-magenta]))
(def hi-white (colour/create-colour [attr/fg-hi-white]))
(def hi-black (colour/create-colour [attr/fg-hi-black]))