(ns clojusc.colours.rgb
  (:require [clojusc.colours.colour :as colour]
            [clojusc.colours.ansi :as ansi]
            [clojure.string :as str]))

(defrecord RGBColour [r g b background? no-colour?]
  ansi/ANSIFormattable
  (format-sequence [this]
    (when (not no-colour?)
      (if background?
        (ansi/seq [(ansi/rgb-bg-code r g b)])
        (ansi/seq [(ansi/rgb-fg-code r g b)]))))
  
  (is-reset? [this] false)
  
  ansi/colourable
  (colourize [this text]
    (if no-colour?
      text
      (str (ansi/format-sequence this) text ansi/reset-sequence)))
  
  (strip [this text]
    (str/replace text #"\u001b\[[0-9;]*m" "")))

(defn fg-colour
  "Create an RGB foreground colour"
  ([r g b] (fg-colour r g b false))
  ([r g b no-colour?]
   {:pre [(and (>= r 0) (<= r 255))
          (and (>= g 0) (<= g 255))
          (and (>= b 0) (<= b 255))]}
   (->RGBColour r g b false no-colour?)))

(defn bg-colour
  "Create an RGB background colour"
  ([r g b] (bg-colour r g b false))
  ([r g b no-colour?]
   {:pre [(and (>= r 0) (<= r 255))
          (and (>= g 0) (<= g 255))
          (and (>= b 0) (<= b 255))]}
   (->RGBColour r g b true no-colour?)))

(defn add-fg
  "Add RGB foreground colour to existing colour"
  [colour r g b]
  (colour/op :combine colour (fg-colour r g b)))

(defn add-bg
  "Add RGB background colour to existing colour"
  [colour r g b]
  (colour/op :combine colour (bg-colour r g b)))