(ns clojusc.colours.ansi
  (:require [clojure.string :as str])
  (:refer-clojure :exclude [seq]))

(def ^:const escape-sequence "\u001b[")
(def ^:const reset-sequence "\u001b[0m")

(defprotocol ANSIFormattable
  "Protocol for objects that can be formatted with ANSI escape codes"
  (format-sequence [this] "Generate ANSI escape sequence")
  (is-reset? [this] "Check if this represents a reset"))

(defprotocol colourable
  "Protocol for applying colours to text"
  (colourize [this text] "Apply colour formatting to text")
  (strip [this text] "Remove colour formatting from text"))

(defn- join-codes [codes]
  (str/join ";" (map str codes)))

(defn seq [codes]
  (str escape-sequence (join-codes codes) "m"))

(defn rgb-fg-code [r g b]
  (format "38;2;%d;%d;%d" r g b))

(defn rgb-bg-code [r g b]
  (format "48;2;%d;%d;%d" r g b))