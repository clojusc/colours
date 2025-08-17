(ns clojusc.colours.print
  (:require [clojusc.colours.ansi :as ansi]))

(def ^:dynamic *no-colour*
  "Global colour disable flag"
  (not (nil? (System/getenv "NO_colour"))))

(def ^:dynamic *output-writer* 
  "Default output writer for coloured text"
  *out*)

(defn- disable? [colourable]
  (or *no-colour*
      (and (satisfies? ansi/colourable colourable)
           (get colourable :no-colour?))))

(defn printed
  "Print coloured text to writer"
  [colour writer text]
  (if (disable? colour)
    (.write writer text)
    (.write writer (ansi/colourize colour text))))

(defn format-coloured
  "Format and colourize text"
  [colour format-str & args]
  (let [formatted (apply format format-str args)]
    (if (disable? colour)
      formatted
      (ansi/colourize colour formatted))))

;; High-level printing functions
(defn print-with-colour
  "Print text with colour to *output-writer*"
  ([colour text]
   (print-with-colour colour *output-writer* text))
  ([colour writer text]
   (printed colour writer text)
   (.flush writer)))

(defn println-with-colour
  "Print text with colour and newline"
  ([colour text]
   (println-with-colour colour *output-writer* text))
  ([colour writer text]
   (printed colour writer (str text \newline))
   (.flush writer)))

(defn printf-with-colour
  "Printf with colour formatting"
  ([colour format-str & args]
   (let [formatted (apply format-coloured colour format-str args)]
     (.write *output-writer* formatted)
     (.flush *output-writer*))))

;; Function generators (like Go's PrintfFunc)
(defn make-print-fn
  "Create a print function with pre-configured colour"
  [colour]
  (fn [text] (print-with-colour colour text)))

(defn make-println-fn
  "Create a println function with pre-configured colour"
  [colour]
  (fn [text] (println-with-colour colour text)))

(defn make-printf-fn
  "Create a printf function with pre-configured colour"
  [colour]
  (fn [format-str & args]
    (apply printf-with-colour colour format-str args)))

(defn make-format-fn
  "Create a string formatting function with pre-configured colour"
  [colour]
  (fn [format-str & args]
    (apply format-coloured colour format-str args)))
