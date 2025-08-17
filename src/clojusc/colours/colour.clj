(ns clojusc.colours.colour
  (:require [clojusc.colours.ansi :as ansi]
            [clojusc.colours.attributes :as attr]
            [clojure.string :as str]))

(defrecord Colour [attributes no-colour?]
  ansi/ANSIFormattable
  (format-sequence [this]
    (when (and (seq attributes) (not no-colour?))
      (ansi/make-escape-sequence attributes)))
  
  (reset-sequence? [this]
    (= attributes [attr/reset]))
  
  ansi/colourable
  (colourize [this text]
    (if (or no-colour? (empty? attributes))
      text
      (str (ansi/format-sequence this) text ansi/reset-sequence)))
  
  (strip-colours [this text]
    (str/replace text #"\u001b\[[0-9;]*m" "")))

;; Constructor functions
(defn create-colour
  "Create a new colour with the given attributes"
  ([attributes] (create-colour attributes false))
  ([attributes no-colour?]
   (->Colour (vec attributes) no-colour?)))

(defn add-attributes
  "Add attributes to an existing colour"
  [colour & attributes]
  (update colour :attributes #(vec (concat % attributes))))

;; Multi-method for colour operations
(defmulti colour-operation
  "Multi-method for different colour operations"
  (fn [op & _] op))

(defmethod colour-operation :combine
  [_ colour1 colour2]
  (create-colour
    (concat (:attributes colour1) (:attributes colour2))
    (or (:no-colour? colour1) (:no-colour? colour2))))

(defmethod colour-operation :enable
  [_ colour]
  (assoc colour :no-colour? false))

(defmethod colour-operation :disable
  [_ colour]
  (assoc colour :no-colour? true))

(defmethod colour-operation :has-foreground?
  [_ colour]
  (some attr/fg-colour-attributes (:attributes colour)))

(defmethod colour-operation :has-background?
  [_ colour]
  (some attr/bg-colour-attributes (:attributes colour)))

(defmethod colour-operation :has-formatting?
  [_ colour]
  (some attr/format-attributes (:attributes colour)))