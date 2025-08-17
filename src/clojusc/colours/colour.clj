(ns clojusc.colours.colour
  (:require [clojusc.colours.ansi :as ansi]
            [clojusc.colours.attr :as attr]
            [clojure.string :as str]))

(defrecord Colour [attributes no-colour?]
  ansi/ANSIFormattable
  (format-sequence [_this]
    (when (and (seq attributes) (not no-colour?))
      (ansi/seq attributes)))
  
  (is-reset? [_this]
    (= attributes [attr/reset]))
  
  ansi/colourable
  (colourize [this text]
    (if (or no-colour? (empty? attributes))
      text
      (str (ansi/format-sequence this) text ansi/reset-sequence)))
  
  (strip [_this text]
    (str/replace text #"\u001b\[[0-9;]*m" "")))

;; Constructor functions
(defn create-colour
  "Create a new colour with the given attributes"
  ([attributes] (create-colour attributes false))
  ([attributes no-colour?]
   (->Colour (vec attributes) no-colour?)))

(defn add-attrs
  "Add attributes to an existing colour"
  [colour & attributes]
  (update colour :attributes #(vec (concat % attributes))))

;; Multi-method for colour operations
(defmulti op
  "Multi-method for different colour operations"
  (fn [op & _] op))

(defmethod op :combine
  [_ colour1 colour2]
  (create-colour
    (concat (:attributes colour1) (:attributes colour2))
    (or (:no-colour? colour1) (:no-colour? colour2))))

(defmethod op :enable
  [_ colour]
  (assoc colour :no-colour? false))

(defmethod op :disable
  [_ colour]
  (assoc colour :no-colour? true))

(defmethod op :has-foreground?
  [_ colour]
  (some attr/fg-colour-attributes (:attributes colour)))

(defmethod op :has-background?
  [_ colour]
  (some attr/bg-colour-attributes (:attributes colour)))

(defmethod op :has-formatting?
  [_ colour]
  (some attr/format-attributes (:attributes colour)))