# Clojure colour Library Implementation Plan

## Overview

This document provides a detailed implementation plan to convert the Go fatih/colour library to a Clojure library following Clojure best practices, protocols, and multi-methods where applicable.

## Original Library Analysis

The Go fatih/colour library provides:
- ANSI escape code generation for terminal colours
- Support for foreground/background colours, styles (bold, italic, underline)
- 24-bit RGB colour support
- colour composition and chaining
- Print functions with colour formatting
- Global and local colour enable/disable functionality
- Cross-platform support (including Windows)

### Key Go Library Features:
- `colour` struct with SGR parameters
- Attribute constants (FgRed, Bold, etc.)
- Colour creation with `New()` and `Add()` methods  
- Print functions (`Print`, `Printf`, `Println`, `Fprint`, etc.)
- String formatting functions (`Sprint`, `Sprintf`, `Sprintln`)
- Function creators (`PrintfFunc`, `SprintFunc`, etc.)
- RGB colour support (`RGB()`, `BgRGB()`, `AddRGB()`)
- Global colour control (`Set()`, `Unset()`, `Nocolour`)

## Clojure Library Design

### Project Structure

```
colours/
├── deps.edn
├── README.md
├── src/clojusc/
│   └── colours/
│       ├── core.clj           ; Main public API
│       ├── attributes.clj     ; colour and style attributes
│       ├── ansi.clj          ; ANSI escape code generation
│       ├── colour.clj         ; colour creation and manipulation
│       ├── print.clj         ; Print functions with colour
│       └── rgb.clj           ; RGB colour support
├── test/clojusc
│   └── colours/
│       ├── core_test.clj
│       ├── colour_test.clj
│       ├── print_test.clj
│       └── rgb_test.clj
└── resources/
    └── colour_mappings.edn    ; colour name mappings
```

### Core Namespace Design

#### 1. colour Attributes (attributes.clj)

```clojure
(ns clojusc.colours.attributes)

;; Base formatting attributes
(def ^:const reset 0)
(def ^:const bold 1)
(def ^:const faint 2)
(def ^:const italic 3)
(def ^:const underline 4)
(def ^:const blink-slow 5)
(def ^:const blink-rapid 6)
(def ^:const reverse-video 7)
(def ^:const concealed 8)
(def ^:const crossed-out 9)

;; Foreground colours
(def ^:const fg-black 30)
(def ^:const fg-red 31)
(def ^:const fg-green 32)
(def ^:const fg-yellow 33)
(def ^:const fg-blue 34)
(def ^:const fg-magenta 35)
(def ^:const fg-cyan 36)
(def ^:const fg-white 37)

;; High-intensity foreground colours
(def ^:const fg-hi-black 90)
(def ^:const fg-hi-red 91)
(def ^:const fg-hi-green 92)
(def ^:const fg-hi-yellow 93)
(def ^:const fg-hi-blue 94)
(def ^:const fg-hi-magenta 95)
(def ^:const fg-hi-cyan 96)
(def ^:const fg-hi-white 97)

;; Background colours
(def ^:const bg-black 40)
(def ^:const bg-red 41)
(def ^:const bg-green 42)
(def ^:const bg-yellow 43)
(def ^:const bg-blue 44)
(def ^:const bg-magenta 45)
(def ^:const bg-cyan 46)
(def ^:const bg-white 47)

;; High-intensity background colours
(def ^:const bg-hi-black 100)
(def ^:const bg-hi-red 101)
(def ^:const bg-hi-green 102)
(def ^:const bg-hi-yellow 103)
(def ^:const bg-hi-blue 104)
(def ^:const bg-hi-magenta 105)
(def ^:const bg-hi-cyan 106)
(def ^:const bg-hi-white 107)

;; Attribute type classifications
(def format-attributes #{bold faint italic underline blink-slow blink-rapid 
                         reverse-video concealed crossed-out})
(def fg-colour-attributes #{fg-black fg-red fg-green fg-yellow fg-blue 
                           fg-magenta fg-cyan fg-white fg-hi-black fg-hi-red 
                           fg-hi-green fg-hi-yellow fg-hi-blue fg-hi-magenta 
                           fg-hi-cyan fg-hi-white})
(def bg-colour-attributes #{bg-black bg-red bg-green bg-yellow bg-blue 
                           bg-magenta bg-cyan bg-white bg-hi-black bg-hi-red 
                           bg-hi-green bg-hi-yellow bg-hi-blue bg-hi-magenta 
                           bg-hi-cyan bg-hi-white})
```

#### 2. ANSI Protocol (ansi.clj)

```clojure
(ns clojusc.colours.ansi
  (:require [clojure.string :as str]))

(def ^:const escape-sequence "\u001b[")
(def ^:const reset-sequence "\u001b[0m")

(defprotocol ANSIFormattable
  "Protocol for objects that can be formatted with ANSI escape codes"
  (format-sequence [this] "Generate ANSI escape sequence")
  (reset-sequence? [this] "Check if this represents a reset"))

(defprotocol colourable
  "Protocol for applying colours to text"
  (colourize [this text] "Apply colour formatting to text")
  (strip-colours [this text] "Remove colour formatting from text"))

(defn- join-codes [codes]
  (str/join ";" (map str codes)))

(defn make-escape-sequence [codes]
  (str escape-sequence (join-codes codes) "m"))

(defn rgb-foreground-code [r g b]
  (format "38;2;%d;%d;%d" r g b))

(defn rgb-background-code [r g b]
  (format "48;2;%d;%d;%d" r g b))
```

#### 3. colour Record and Multi-methods (colour.clj)

```clojure
(ns clojusc.colours.colour
  (:require [clojusc.colours.ansi :as ansi]
            [clojusc.colours.attributes :as attr]
            [clojure.string :as str]))

(defrecord colour [attributes no-colour?]
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
   (->colour (vec attributes) no-colour?)))

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
```

#### 4. RGB colour Support (rgb.clj)

```clojure
(ns clojusc.colours.rgb
  (:require [clojusc.colours.colour :as colour]
            [clojusc.colours.ansi :as ansi]))

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
```

#### 5. Print Functions (print.clj)

```clojure
(ns clojusc.colours.print
  (:require [clojusc.colours.ansi :as ansi]
            [clojure.java.io :as io]))

(def ^:dynamic *no-colour* 
  "Global colour disable flag"
  (not (nil? (System/getenv "NO_colour"))))

(def ^:dynamic *output-writer* 
  "Default output writer for coloured text"
  *out*)

(defprotocol ColourPrinter
  "Protocol for colour printing operations"
  (print-coloured [this writer text] "Print coloured text to writer")
  (format-coloured [this format-str & args] "Format and colourize text"))

(defn- should-disable-colour? [colourable]
  (or *no-colour* 
      (and (satisfies? ansi/colourable colourable)
           (get colourable :no-colour?))))

(extend-protocol ColourPrinter
  Object
  (print-coloured [colour writer text]
    (if (should-disable-colour? colour)
      (.write writer text)
      (.write writer (ansi/colourize colour text))))
  
  (format-coloured [colour format-str & args]
    (let [formatted (apply format format-str args)]
      (if (should-disable-colour? colour)
        formatted
        (ansi/colourize colour formatted)))))

;; High-level printing functions
(defn print-with-colour
  "Print text with colour to *output-writer*"
  ([colour text]
   (print-with-colour colour *output-writer* text))
  ([colour writer text]
   (print-coloured colour writer text)
   (.flush writer)))

(defn println-with-colour
  "Print text with colour and newline"
  ([colour text]
   (println-with-colour colour *output-writer* text))
  ([colour writer text]
   (print-coloured colour writer (str text \newline))
   (.flush writer)))

(defn printf-with-colour
  "Printf with colour formatting"
  ([colour format-str & args]
   (printf-with-colour colour *output-writer* format-str args))
  ([colour writer format-str & args]
   (let [formatted (format-coloured colour format-str args)]
     (.write writer formatted)
     (.flush writer))))

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
```

#### 6. Core Public API (core.clj)

```clojure
(ns clojusc.colours.core
  "Main public API for the Clojure colour library"
  (:require [clojusc.colours.attributes :as attr]
            [clojusc.colours.colour :as colour]
            [clojusc.colours.rgb :as rgb]
            [clojusc.colours.print :as print]
            [clojusc.colours.ansi :as ansi])
  (:import [clj_colour.colour colour]
           [clj_colour.rgb RGBcolour]))

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

;; Colour creation
(defn colour
  "Create a new colour with the given attributes"
  [& attributes]
  (colour/create-colour attributes))

(defn rgb
  "Create RGB foreground colour"
  [r g b]
  (rgb/rgb-colour r g b))

(defn rgb-bg
  "Create RGB background colour"
  [r g b]
  (rgb/rgb-bg-colour r g b))

;; Colour manipulation
(defn add
  "Add attributes to a colour"
  [colour & attributes]
  (apply colour/add-attributes colour attributes))

(defn combine
  "Combine two colours"
  [colour1 colour2]
  (colour/colour-operation :combine colour1 colour2))

(defn enable-colour
  "Enable colour output for a colour"
  [colour]
  (colour/colour-operation :enable colour))

(defn disable-colour
  "Disable colour output for a colour"
  [colour]
  (colour/colour-operation :disable colour))

;; String operations
(defn colourize
  "Apply colour to text string"
  [colour text]
  (ansi/colourize colour text))

(defn strip-colours
  "Remove ANSI colour codes from text"
  [text]
  (ansi/strip-colours (colour/create-colour []) text))

;; Printing functions
(defn print-colour
  "Print coloured text"
  [colour text]
  (print/print-with-colour colour text))

(defn println-colour
  "Print coloured text with newline"
  [colour text]
  (print/println-with-colour colour text))

(defn printf-colour
  "Printf with colour"
  [colour format-str & args]
  (apply print/printf-with-colour colour format-str args))

;; Convenient colour functions (like Go's colour.Red(), colour.Green(), etc.)
(defmacro defcolour
  "Define a convenient colour function"
  [name attr]
  `(defn ~name
     ([text#] (~name "%s" text#))
     ([format-str# & args#]
      (let [colour# (colour/create-colour [~attr])]
        (apply print/printf-with-colour colour# (str format-str# "\\n") args#)))))

(defcolour red attr/fg-red)
(defcolour green attr/fg-green)
(defcolour blue attr/fg-blue)
(defcolour yellow attr/fg-yellow)
(defcolour cyan attr/fg-cyan)
(defcolour magenta attr/fg-magenta)
(defcolour white attr/fg-white)
(defcolour black attr/fg-black)

;; String formatting functions (like Go's colour.RedString())
(defmacro defcolour-string
  "Define a colour string function"
  [name attr]
  `(defn ~(symbol (str name "-string"))
     ([text#] (~(symbol (str name "-string")) "%s" text#))
     ([format-str# & args#]
      (let [colour# (colour/create-colour [~attr])]
        (apply print/format-coloured colour# format-str# args#)))))

(defcolour-string red attr/fg-red)
(defcolour-string green attr/fg-green)
(defcolour-string blue attr/fg-blue)
(defcolour-string yellow attr/fg-yellow)
(defcolour-string cyan attr/fg-cyan)
(defcolour-string magenta attr/fg-magenta)
(defcolour-string white attr/fg-white)
(defcolour-string black attr/fg-black)

;; Global colour control
(defn set-no-colour!
  "Globally disable colour output"
  [disabled?]
  (alter-var-root #'print/*no-colour* (constantly disabled?)))

(defn no-colour?
  "Check if colour output is globally disabled"
  []
  print/*no-colour*)

;; High-intensity colour variants
(def hi-red (colour/create-colour [attr/fg-hi-red]))
(def hi-green (colour/create-colour [attr/fg-hi-green]))
(def hi-blue (colour/create-colour [attr/fg-hi-blue]))
(def hi-yellow (colour/create-colour [attr/fg-hi-yellow]))
(def hi-cyan (colour/create-colour [attr/fg-hi-cyan]))
(def hi-magenta (colour/create-colour [attr/fg-hi-magenta]))
(def hi-white (colour/create-colour [attr/fg-hi-white]))
(def hi-black (colour/create-colour [attr/fg-hi-black]))
```

## Implementation Details

### 1. Clojure Best Practices Applied

- **Immutable Data Structures**: colours are immutable records that return new instances when modified
- **Protocols**: Define clear interfaces for ANSI formatting and colour operations
- **Multi-methods**: Used for extensible colour operations based on dispatch values
- **Namespaced Organization**: Logical separation of concerns across namespaces
- **Dynamic Variables**: For global configuration like `*no-colour*` and `*output-writer*`
- **Pre/Post Conditions**: Input validation for RGB values
- **Destructuring**: Used throughout for clean parameter handling

### 2. Protocol Usage

- **ANSIFormattable**: For objects that can generate ANSI escape sequences
- **colourable**: For applying and stripping colours from text
- **ColourPrinter**: For consistent printing behavior across different colour types

### 3. Multi-method Usage

- **colour-operation**: Extensible operations on colours (combine, enable, disable, etc.)
- Allows for easy extension with new colour operations without modifying existing code

### 4. Key Features

#### Immutable colour Composition
```clojure
(def red-bold (-> (colour fg-red)
                  (add bold)))

(def red-bold-underline (add red-bold underline))
```

#### RGB Support with Validation
```clojure
(def orange (rgb 255 128 0))
(def orange-bg (rgb-bg 255 128 0))
```

#### Flexible Printing API
```clojure
(println-colour (colour fg-red bold) "Error message")
(printf-colour (colour fg-green) "Success: %d items processed" 42)

;; Or use convenience functions
(red "Error: %s" error-msg)
(green "Success!")
```

#### Function Generation
```clojure
(def error-printer (make-println-fn (colour fg-red bold)))
(def success-formatter (make-format-fn (colour fg-green)))

(error-printer "Something went wrong!")
```

### 5. Configuration and Environment

- Respects `NO_colour` environment variable
- Global colour disable with `set-no-colour!`
- Per-colour disable with `disable-colour`
- Configurable output writer

### 6. Cross-platform Considerations

- Uses standard ANSI escape sequences
- Handles colour stripping for non-terminal outputs
- Environment variable detection for colour support

## Testing Strategy

### Unit Tests
- Test all colour attributes and combinations
- Validate ANSI escape sequence generation
- Test RGB colour creation and validation
- Verify print function behavior with mocked writers

### Integration Tests
- Test complete colour workflows
- Verify environment variable handling
- Test protocol implementations across different types

### Property-based Tests
- Use `test.check` for RGB value validation
- Test colour combination properties
- Verify ANSI sequence correctness

## Usage Examples

### Basic Usage
```clojure
(require '[clojusc.colours.core :as colour])

;; Simple coloured output
(colour/red "This is red text")
(colour/green "Success: %d files processed" 42)

;; Create custom colours
(def warning-style (colour/colour colour/fg-yellow colour/bold))
(colour/println-colour warning-style "Warning message")

;; RGB colours
(def orange (colour/rgb 255 128 0))
(colour/print-colour orange "Orange text")

;; Combine colours and styles
(def error-style (-> (colour/colour colour/fg-red)
                     (colour/add colour/bold colour/underline)))
```

### Advanced Usage
```clojure
;; Function generation
(def log-error (colour/make-println-fn 
                (colour/colour colour/fg-red colour/bold)))
(def log-info (colour/make-println-fn 
               (colour/colour colour/fg-blue)))

(log-error "Critical error occurred!")
(log-info "Processing complete")

;; String formatting
(println "Status:" (colour/green-string "OK") 
         "Errors:" (colour/red-string "%d" error-count))

;; Disable colours conditionally
(when (some-condition?)
  (colour/set-no-colour! true))
```

## Migration Guide

### From Go fatih/colour to clojusc.colours

| Go Code | Clojure Equivalent |
|---------|-------------------|
| `colour.Red("text")` | `(colour/red "text")` |
| `colour.New(colour.FgRed, colour.Bold)` | `(colour/colour colour/fg-red colour/bold)` |
| `c.Add(colour.Underline)` | `(colour/add c colour/underline)` |
| `colour.RGB(255, 0, 0)` | `(colour/rgb 255 0 0)` |
| `c.PrintfFunc()` | `(colour/make-printf-fn c)` |
| `colour.NoColour = true` | `(colour/set-no-colour! true)` |

## Dependencies

Add to `deps.edn`:
```clojure
{:deps {org.clojure/clojure {:mvn/version "1.11.1"}}
 :dev-deps {org.clojure/test.check {:mvn/version "1.1.1"}}}
```

## Performance Considerations

- Use of records for efficient attribute storage
- Lazy sequence generation for ANSI codes
- Efficient string concatenation for colour formatting
- Memoization opportunities for frequently used colours

## Future Enhancements

1. **Colour Themes**: Support for named colour themes
2. **Terminal Detection**: Auto-disable colours for non-TTY outputs  
3. **256-colour Support**: Extended colour palette beyond RGB
4. **Colour Interpolation**: Generate colour gradients
5. **Styled Component**: Higher-level styling abstractions

This implementation plan provides a comprehensive, idiomatic Clojure translation of the Go fatih/colour library while leveraging Clojure's strengths in immutability, protocols, and functional composition.
