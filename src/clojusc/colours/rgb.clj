(ns clojusc.colours.rgb
  (:require [clojusc.colours.color :as color]
            [clojusc.colours.ansi :as ansi]
            [clojure.string :as str]))

(defrecord RGBColor [r g b background? no-color?]
  ansi/ANSIFormattable
  (format-sequence [this]
    (when (not no-color?)
      (if background?
        (ansi/make-escape-sequence [(ansi/rgb-background-code r g b)])
        (ansi/make-escape-sequence [(ansi/rgb-foreground-code r g b)]))))
  
  (reset-sequence? [this] false)
  
  ansi/Colorable
  (colorize [this text]
    (if no-color?
      text
      (str (ansi/format-sequence this) text ansi/reset-sequence)))
  
  (strip-colors [this text]
    (str/replace text #"\u001b\[[0-9;]*m" "")))

(defn rgb-color
  "Create an RGB foreground color"
  ([r g b] (rgb-color r g b false))
  ([r g b no-color?]
   {:pre [(and (>= r 0) (<= r 255))
          (and (>= g 0) (<= g 255))
          (and (>= b 0) (<= b 255))]}
   (->RGBColor r g b false no-color?)))

(defn rgb-bg-color
  "Create an RGB background color"
  ([r g b] (rgb-bg-color r g b false))
  ([r g b no-color?]
   {:pre [(and (>= r 0) (<= r 255))
          (and (>= g 0) (<= g 255))
          (and (>= b 0) (<= b 255))]}
   (->RGBColor r g b true no-color?)))

(defn add-rgb
  "Add RGB foreground color to existing color"
  [color r g b]
  (color/color-operation :combine color (rgb-color r g b)))

(defn add-rgb-bg
  "Add RGB background color to existing color"
  [color r g b]
  (color/color-operation :combine color (rgb-bg-color r g b)))