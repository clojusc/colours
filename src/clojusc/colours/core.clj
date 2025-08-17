(ns clojusc.colours.core
  "Main public API for the Clojure color library"
  (:require [clojusc.colours.attributes :as attr]
            [clojusc.colours.color :as color]
            [clojusc.colours.rgb :as rgb]
            [clojusc.colours.print :as print]
            [clojusc.colours.ansi :as ansi])
  (:import [clojusc.colours.color Color]
           [clojusc.colours.rgb RGBColor]))

;; Re-export commonly used attributes
(def bold attr/bold)
(def italic attr/italic)
(def underline attr/underline)
(def fg-red attr/fg-red)
(def fg-green attr/fg-green)
(def fg-blue attr/fg-blue)
(def fg-yellow attr/fg-yellow)
(def fg-cyan attr/fg-cyan)
(def fg-magenta attr/fg-magenta)
(def fg-black attr/fg-black)
(def fg-white attr/fg-white)
(def bg-red attr/bg-red)
(def bg-green attr/bg-green)
(def bg-blue attr/bg-blue)

;; Color creation
(defn color
  "Create a new color with the given attributes"
  [& attributes]
  (color/create-color attributes))

(defn rgb
  "Create RGB foreground color"
  [r g b]
  (rgb/rgb-color r g b))

(defn rgb-bg
  "Create RGB background color"
  [r g b]
  (rgb/rgb-bg-color r g b))

;; Color manipulation
(defn add
  "Add attributes to a color"
  [color & attributes]
  (apply color/add-attributes color attributes))

(defn combine
  "Combine two colors"
  [color1 color2]
  (color/color-operation :combine color1 color2))

(defn enable-color
  "Enable color output for a color"
  [color]
  (color/color-operation :enable color))

(defn disable-color
  "Disable color output for a color"
  [color]
  (color/color-operation :disable color))

;; String operations
(defn colorize
  "Apply color to text string"
  [color text]
  (ansi/colorize color text))

(defn strip-colors
  "Remove ANSI color codes from text"
  [text]
  (ansi/strip-colors (color/create-color []) text))

;; Printing functions
(defn print-color
  "Print colored text"
  [color text]
  (print/print-with-color color text))

(defn println-color
  "Print colored text with newline"
  [color text]
  (print/println-with-color color text))

(defn printf-color
  "Printf with color"
  [color format-str & args]
  (apply print/printf-with-color color format-str args))

;; Convenient color functions (like Go's color.Red(), color.Green(), etc.)
(defmacro defcolor
  "Define a convenient color function"
  [name attr]
  `(defn ~name
     ([text#] (~name "%s" text#))
     ([format-str# & args#]
      (let [color# (color/create-color [~attr])]
        (apply print/printf-with-color color# (str format-str# "\\n") args#)))))

(defcolor red attr/fg-red)
(defcolor green attr/fg-green)
(defcolor blue attr/fg-blue)
(defcolor yellow attr/fg-yellow)
(defcolor cyan attr/fg-cyan)
(defcolor magenta attr/fg-magenta)
(defcolor white attr/fg-white)
(defcolor black attr/fg-black)

;; String formatting functions (like Go's color.RedString())
(defmacro defcolor-string
  "Define a color string function"
  [name attr]
  `(defn ~(symbol (str name "-string"))
     ([text#] (~(symbol (str name "-string")) "%s" text#))
     ([format-str# & args#]
      (let [color# (color/create-color [~attr])]
        (apply print/format-colored color# format-str# args#)))))

(defcolor-string red attr/fg-red)
(defcolor-string green attr/fg-green)
(defcolor-string blue attr/fg-blue)
(defcolor-string yellow attr/fg-yellow)
(defcolor-string cyan attr/fg-cyan)
(defcolor-string magenta attr/fg-magenta)
(defcolor-string white attr/fg-white)
(defcolor-string black attr/fg-black)

;; Global color control
(defn set-no-color!
  "Globally disable color output"
  [disabled?]
  (alter-var-root #'print/*no-color* (constantly disabled?)))

(defn no-color?
  "Check if color output is globally disabled"
  []
  print/*no-color*)

;; High-intensity color variants
(def hi-red (color/create-color [attr/fg-hi-red]))
(def hi-green (color/create-color [attr/fg-hi-green]))
(def hi-blue (color/create-color [attr/fg-hi-blue]))
(def hi-yellow (color/create-color [attr/fg-hi-yellow]))
(def hi-cyan (color/create-color [attr/fg-hi-cyan]))
(def hi-magenta (color/create-color [attr/fg-hi-magenta]))
(def hi-white (color/create-color [attr/fg-hi-white]))
(def hi-black (color/create-color [attr/fg-hi-black]))