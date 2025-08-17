(ns clojusc.colours.color
  (:require [clojusc.colours.ansi :as ansi]
            [clojusc.colours.attributes :as attr]
            [clojure.string :as str]))

(defrecord Color [attributes no-color?]
  ansi/ANSIFormattable
  (format-sequence [this]
    (when (and (seq attributes) (not no-color?))
      (ansi/make-escape-sequence attributes)))
  
  (reset-sequence? [this]
    (= attributes [attr/reset]))
  
  ansi/Colorable
  (colorize [this text]
    (if (or no-color? (empty? attributes))
      text
      (str (ansi/format-sequence this) text ansi/reset-sequence)))
  
  (strip-colors [this text]
    (str/replace text #"\u001b\[[0-9;]*m" "")))

;; Constructor functions
(defn create-color 
  "Create a new color with the given attributes"
  ([attributes] (create-color attributes false))
  ([attributes no-color?]
   (->Color (vec attributes) no-color?)))

(defn add-attributes
  "Add attributes to an existing color"
  [color & attributes]
  (update color :attributes #(vec (concat % attributes))))

;; Multi-method for color operations
(defmulti color-operation 
  "Multi-method for different color operations"
  (fn [op & _] op))

(defmethod color-operation :combine
  [_ color1 color2]
  (create-color 
    (concat (:attributes color1) (:attributes color2))
    (or (:no-color? color1) (:no-color? color2))))

(defmethod color-operation :enable
  [_ color]
  (assoc color :no-color? false))

(defmethod color-operation :disable
  [_ color]
  (assoc color :no-color? true))

(defmethod color-operation :has-foreground?
  [_ color]
  (some attr/fg-color-attributes (:attributes color)))

(defmethod color-operation :has-background?
  [_ color]
  (some attr/bg-color-attributes (:attributes color)))

(defmethod color-operation :has-formatting?
  [_ color]
  (some attr/format-attributes (:attributes color)))