(ns clojusc.colours.print
  (:require [clojusc.colours.ansi :as ansi]
            [clojure.java.io :as io]))

(def ^:dynamic *no-color* 
  "Global color disable flag"
  (not (nil? (System/getenv "NO_COLOR"))))

(def ^:dynamic *output-writer* 
  "Default output writer for colored text"
  *out*)

(defn- should-disable-color? [colorable]
  (or *no-color* 
      (and (satisfies? ansi/Colorable colorable)
           (get colorable :no-color?))))

(defn print-colored
  "Print colored text to writer"
  [color writer text]
  (if (should-disable-color? color)
    (.write writer text)
    (.write writer (ansi/colorize color text))))

(defn format-colored
  "Format and colorize text"
  [color format-str & args]
  (let [formatted (apply format format-str args)]
    (if (should-disable-color? color)
      formatted
      (ansi/colorize color formatted))))

;; High-level printing functions
(defn print-with-color
  "Print text with color to *output-writer*"
  ([color text]
   (print-with-color color *output-writer* text))
  ([color writer text]
   (print-colored color writer text)
   (.flush writer)))

(defn println-with-color
  "Print text with color and newline"
  ([color text]
   (println-with-color color *output-writer* text))
  ([color writer text]
   (print-colored color writer (str text \newline))
   (.flush writer)))

(defn printf-with-color
  "Printf with color formatting"
  ([color format-str & args]
   (let [formatted (apply format-colored color format-str args)]
     (.write *output-writer* formatted)
     (.flush *output-writer*))))

;; Function generators (like Go's PrintfFunc)
(defn make-print-fn
  "Create a print function with pre-configured color"
  [color]
  (fn [text] (print-with-color color text)))

(defn make-println-fn
  "Create a println function with pre-configured color"
  [color]
  (fn [text] (println-with-color color text)))

(defn make-printf-fn
  "Create a printf function with pre-configured color"
  [color]
  (fn [format-str & args]
    (apply printf-with-color color format-str args)))

(defn make-format-fn
  "Create a string formatting function with pre-configured color"
  [color]
  (fn [format-str & args]
    (apply format-colored color format-str args)))