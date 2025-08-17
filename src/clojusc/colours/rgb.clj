(ns clojusc.colours.rgb
  (:require [clojusc.colours.colour :as colour]
            [clojusc.colours.ansi :as ansi]
            [clojure.string :as str]))

(defrecord RGBcolour [r g b background? no-colour?]
  ansi/ANSIFormattable
  (format-sequence [this]
    (when (not no-colour?)
      (if background?
        (ansi/make-escape-sequence [(ansi/rgb-background-code r g b)])
        (ansi/make-escape-sequence [(ansi/rgb-foreground-code r g b)]))))
  
  (reset-sequence? [this] false)
  
  ansi/colourable
  (colourize [this text]
    (if no-colour?
      text
      (str (ansi/format-sequence this) text ansi/reset-sequence)))
  
  (strip-colours [this text]
    (str/replace text #"\u001b\[[0-9;]*m" "")))

(defn rgb-colour
  "Create an RGB foreground colour"
  ([r g b] (rgb-colour r g b false))
  ([r g b no-colour?]
   {:pre [(and (>= r 0) (<= r 255))
          (and (>= g 0) (<= g 255))
          (and (>= b 0) (<= b 255))]}
   (->RGBcolour r g b false no-colour?)))

(defn rgb-bg-colour
  "Create an RGB background colour"
  ([r g b] (rgb-bg-colour r g b false))
  ([r g b no-colour?]
   {:pre [(and (>= r 0) (<= r 255))
          (and (>= g 0) (<= g 255))
          (and (>= b 0) (<= b 255))]}
   (->RGBcolour r g b true no-colour?)))

(defn add-rgb
  "Add RGB foreground colour to existing colour"
  [colour r g b]
  (colour/colour-operation :combine colour (rgb-colour r g b)))

(defn add-rgb-bg
  "Add RGB background colour to existing colour"
  [colour r g b]
  (colour/colour-operation :combine colour (rgb-bg-colour r g b)))