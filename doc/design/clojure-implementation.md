# Clojure Color Library Implementation Plan

## Overview

This document provides a detailed implementation plan to convert the Go fatih/color library to a Clojure library following Clojure best practices, protocols, and multi-methods where applicable.

## Original Library Analysis

The Go fatih/color library provides:
- ANSI escape code generation for terminal colors
- Support for foreground/background colors, styles (bold, italic, underline)
- 24-bit RGB color support
- Color composition and chaining
- Print functions with color formatting
- Global and local color enable/disable functionality
- Cross-platform support (including Windows)

### Key Go Library Features:
- `Color` struct with SGR parameters
- Attribute constants (FgRed, Bold, etc.)
- Color creation with `New()` and `Add()` methods  
- Print functions (`Print`, `Printf`, `Println`, `Fprint`, etc.)
- String formatting functions (`Sprint`, `Sprintf`, `Sprintln`)
- Function creators (`PrintfFunc`, `SprintFunc`, etc.)
- RGB color support (`RGB()`, `BgRGB()`, `AddRGB()`)
- Global color control (`Set()`, `Unset()`, `NoColor`)

## Clojure Library Design

### Project Structure

```
colours/
├── deps.edn
├── README.md
├── src/clojusc/
│   └── colours/
│       ├── core.clj           ; Main public API
│       ├── attributes.clj     ; Color and style attributes
│       ├── ansi.clj          ; ANSI escape code generation
│       ├── color.clj         ; Color creation and manipulation
│       ├── print.clj         ; Print functions with color
│       └── rgb.clj           ; RGB color support
├── test/clojusc
│   └── colours/
│       ├── core_test.clj
│       ├── color_test.clj
│       ├── print_test.clj
│       └── rgb_test.clj
└── resources/
    └── color_mappings.edn    ; Color name mappings
```

### Core Namespace Design

#### 1. Color Attributes (attributes.clj)

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

;; Foreground colors
(def ^:const fg-black 30)
(def ^:const fg-red 31)
(def ^:const fg-green 32)
(def ^:const fg-yellow 33)
(def ^:const fg-blue 34)
(def ^:const fg-magenta 35)
(def ^:const fg-cyan 36)
(def ^:const fg-white 37)

;; High-intensity foreground colors
(def ^:const fg-hi-black 90)
(def ^:const fg-hi-red 91)
(def ^:const fg-hi-green 92)
(def ^:const fg-hi-yellow 93)
(def ^:const fg-hi-blue 94)
(def ^:const fg-hi-magenta 95)
(def ^:const fg-hi-cyan 96)
(def ^:const fg-hi-white 97)

;; Background colors
(def ^:const bg-black 40)
(def ^:const bg-red 41)
(def ^:const bg-green 42)
(def ^:const bg-yellow 43)
(def ^:const bg-blue 44)
(def ^:const bg-magenta 45)
(def ^:const bg-cyan 46)
(def ^:const bg-white 47)

;; High-intensity background colors
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
(def fg-color-attributes #{fg-black fg-red fg-green fg-yellow fg-blue 
                           fg-magenta fg-cyan fg-white fg-hi-black fg-hi-red 
                           fg-hi-green fg-hi-yellow fg-hi-blue fg-hi-magenta 
                           fg-hi-cyan fg-hi-white})
(def bg-color-attributes #{bg-black bg-red bg-green bg-yellow bg-blue 
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

(defprotocol Colorable
  "Protocol for applying colors to text"
  (colorize [this text] "Apply color formatting to text")
  (strip-colors [this text] "Remove color formatting from text"))

(defn- join-codes [codes]
  (str/join ";" (map str codes)))

(defn make-escape-sequence [codes]
  (str escape-sequence (join-codes codes) "m"))

(defn rgb-foreground-code [r g b]
  (format "38;2;%d;%d;%d" r g b))

(defn rgb-background-code [r g b]
  (format "48;2;%d;%d;%d" r g b))
```

#### 3. Color Record and Multi-methods (color.clj)

```clojure
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
```

#### 4. RGB Color Support (rgb.clj)

```clojure
(ns clojusc.colours.rgb
  (:require [clojusc.colours.color :as color]
            [clojusc.colours.ansi :as ansi]))

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
```

#### 5. Print Functions (print.clj)

```clojure
(ns clojusc.colours.print
  (:require [clojusc.colours.ansi :as ansi]
            [clojure.java.io :as io]))

(def ^:dynamic *no-color* 
  "Global color disable flag"
  (not (nil? (System/getenv "NO_COLOR"))))

(def ^:dynamic *output-writer* 
  "Default output writer for colored text"
  *out*)

(defprotocol ColorPrinter
  "Protocol for color printing operations"
  (print-colored [this writer text] "Print colored text to writer")
  (format-colored [this format-str & args] "Format and colorize text"))

(defn- should-disable-color? [colorable]
  (or *no-color* 
      (and (satisfies? ansi/Colorable colorable)
           (get colorable :no-color?))))

(extend-protocol ColorPrinter
  Object
  (print-colored [color writer text]
    (if (should-disable-color? color)
      (.write writer text)
      (.write writer (ansi/colorize color text))))
  
  (format-colored [color format-str & args]
    (let [formatted (apply format format-str args)]
      (if (should-disable-color? color)
        formatted
        (ansi/colorize color formatted)))))

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
   (printf-with-color color *output-writer* format-str args))
  ([color writer format-str & args]
   (let [formatted (format-colored color format-str args)]
     (.write writer formatted)
     (.flush writer))))

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
```

#### 6. Core Public API (core.clj)

```clojure
(ns clojusc.colours.core
  "Main public API for the Clojure color library"
  (:require [clojusc.colours.attributes :as attr]
            [clojusc.colours.color :as color]
            [clojusc.colours.rgb :as rgb]
            [clojusc.colours.print :as print]
            [clojusc.colours.ansi :as ansi])
  (:import [clj_color.color Color]
           [clj_color.rgb RGBColor]))

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

;; Color creation
(defn color
  "Create a new color with the given attributes"
  [& attributes]
  (color/create-color attributes))

(defn rgb
  "Create RGB foreground color"
  [r g b]
  (rgb/rgb-color r g b))

(defn rgb-bg
  "Create RGB background color"
  [r g b]
  (rgb/rgb-bg-color r g b))

;; Color manipulation
(defn add
  "Add attributes to a color"
  [color & attributes]
  (apply color/add-attributes color attributes))

(defn combine
  "Combine two colors"
  [color1 color2]
  (color/color-operation :combine color1 color2))

(defn enable-color
  "Enable color output for a color"
  [color]
  (color/color-operation :enable color))

(defn disable-color
  "Disable color output for a color"
  [color]
  (color/color-operation :disable color))

;; String operations
(defn colorize
  "Apply color to text string"
  [color text]
  (ansi/colorize color text))

(defn strip-colors
  "Remove ANSI color codes from text"
  [text]
  (ansi/strip-colors (color/create-color []) text))

;; Printing functions
(defn print-color
  "Print colored text"
  [color text]
  (print/print-with-color color text))

(defn println-color
  "Print colored text with newline"
  [color text]
  (print/println-with-color color text))

(defn printf-color
  "Printf with color"
  [color format-str & args]
  (apply print/printf-with-color color format-str args))

;; Convenient color functions (like Go's color.Red(), color.Green(), etc.)
(defmacro defcolor
  "Define a convenient color function"
  [name attr]
  `(defn ~name
     ([text#] (~name "%s" text#))
     ([format-str# & args#]
      (let [color# (color/create-color [~attr])]
        (apply print/printf-with-color color# (str format-str# "\\n") args#)))))

(defcolor red attr/fg-red)
(defcolor green attr/fg-green)
(defcolor blue attr/fg-blue)
(defcolor yellow attr/fg-yellow)
(defcolor cyan attr/fg-cyan)
(defcolor magenta attr/fg-magenta)
(defcolor white attr/fg-white)
(defcolor black attr/fg-black)

;; String formatting functions (like Go's color.RedString())
(defmacro defcolor-string
  "Define a color string function"
  [name attr]
  `(defn ~(symbol (str name "-string"))
     ([text#] (~(symbol (str name "-string")) "%s" text#))
     ([format-str# & args#]
      (let [color# (color/create-color [~attr])]
        (apply print/format-colored color# format-str# args#)))))

(defcolor-string red attr/fg-red)
(defcolor-string green attr/fg-green)
(defcolor-string blue attr/fg-blue)
(defcolor-string yellow attr/fg-yellow)
(defcolor-string cyan attr/fg-cyan)
(defcolor-string magenta attr/fg-magenta)
(defcolor-string white attr/fg-white)
(defcolor-string black attr/fg-black)

;; Global color control
(defn set-no-color!
  "Globally disable color output"
  [disabled?]
  (alter-var-root #'print/*no-color* (constantly disabled?)))

(defn no-color?
  "Check if color output is globally disabled"
  []
  print/*no-color*)

;; High-intensity color variants
(def hi-red (color/create-color [attr/fg-hi-red]))
(def hi-green (color/create-color [attr/fg-hi-green]))
(def hi-blue (color/create-color [attr/fg-hi-blue]))
(def hi-yellow (color/create-color [attr/fg-hi-yellow]))
(def hi-cyan (color/create-color [attr/fg-hi-cyan]))
(def hi-magenta (color/create-color [attr/fg-hi-magenta]))
(def hi-white (color/create-color [attr/fg-hi-white]))
(def hi-black (color/create-color [attr/fg-hi-black]))
```

## Implementation Details

### 1. Clojure Best Practices Applied

- **Immutable Data Structures**: Colors are immutable records that return new instances when modified
- **Protocols**: Define clear interfaces for ANSI formatting and color operations
- **Multi-methods**: Used for extensible color operations based on dispatch values
- **Namespaced Organization**: Logical separation of concerns across namespaces
- **Dynamic Variables**: For global configuration like `*no-color*` and `*output-writer*`
- **Pre/Post Conditions**: Input validation for RGB values
- **Destructuring**: Used throughout for clean parameter handling

### 2. Protocol Usage

- **ANSIFormattable**: For objects that can generate ANSI escape sequences
- **Colorable**: For applying and stripping colors from text
- **ColorPrinter**: For consistent printing behavior across different color types

### 3. Multi-method Usage

- **color-operation**: Extensible operations on colors (combine, enable, disable, etc.)
- Allows for easy extension with new color operations without modifying existing code

### 4. Key Features

#### Immutable Color Composition
```clojure
(def red-bold (-> (color fg-red)
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
(println-color (color fg-red bold) "Error message")
(printf-color (color fg-green) "Success: %d items processed" 42)

;; Or use convenience functions
(red "Error: %s" error-msg)
(green "Success!")
```

#### Function Generation
```clojure
(def error-printer (make-println-fn (color fg-red bold)))
(def success-formatter (make-format-fn (color fg-green)))

(error-printer "Something went wrong!")
```

### 5. Configuration and Environment

- Respects `NO_COLOR` environment variable
- Global color disable with `set-no-color!`
- Per-color disable with `disable-color`
- Configurable output writer

### 6. Cross-platform Considerations

- Uses standard ANSI escape sequences
- Handles color stripping for non-terminal outputs
- Environment variable detection for color support

## Testing Strategy

### Unit Tests
- Test all color attributes and combinations
- Validate ANSI escape sequence generation
- Test RGB color creation and validation
- Verify print function behavior with mocked writers

### Integration Tests
- Test complete color workflows
- Verify environment variable handling
- Test protocol implementations across different types

### Property-based Tests
- Use `test.check` for RGB value validation
- Test color combination properties
- Verify ANSI sequence correctness

## Usage Examples

### Basic Usage
```clojure
(require '[clojusc.colours.core :as color])

;; Simple colored output
(color/red "This is red text")
(color/green "Success: %d files processed" 42)

;; Create custom colors
(def warning-style (color/color color/fg-yellow color/bold))
(color/println-color warning-style "Warning message")

;; RGB colors
(def orange (color/rgb 255 128 0))
(color/print-color orange "Orange text")

;; Combine colors and styles
(def error-style (-> (color/color color/fg-red)
                     (color/add color/bold color/underline)))
```

### Advanced Usage
```clojure
;; Function generation
(def log-error (color/make-println-fn 
                (color/color color/fg-red color/bold)))
(def log-info (color/make-println-fn 
               (color/color color/fg-blue)))

(log-error "Critical error occurred!")
(log-info "Processing complete")

;; String formatting
(println "Status:" (color/green-string "OK") 
         "Errors:" (color/red-string "%d" error-count))

;; Disable colors conditionally
(when (some-condition?)
  (color/set-no-color! true))
```

## Migration Guide

### From Go fatih/color to clojusc.colours

| Go Code | Clojure Equivalent |
|---------|-------------------|
| `color.Red("text")` | `(color/red "text")` |
| `color.New(color.FgRed, color.Bold)` | `(color/color color/fg-red color/bold)` |
| `c.Add(color.Underline)` | `(color/add c color/underline)` |
| `color.RGB(255, 0, 0)` | `(color/rgb 255 0 0)` |
| `c.PrintfFunc()` | `(color/make-printf-fn c)` |
| `color.NoColor = true` | `(color/set-no-color! true)` |

## Dependencies

Add to `deps.edn`:
```clojure
{:deps {org.clojure/clojure {:mvn/version "1.11.1"}}
 :dev-deps {org.clojure/test.check {:mvn/version "1.1.1"}}}
```

## Performance Considerations

- Use of records for efficient attribute storage
- Lazy sequence generation for ANSI codes
- Efficient string concatenation for color formatting
- Memoization opportunities for frequently used colors

## Future Enhancements

1. **Color Themes**: Support for named color themes
2. **Terminal Detection**: Auto-disable colors for non-TTY outputs  
3. **256-Color Support**: Extended color palette beyond RGB
4. **Color Interpolation**: Generate color gradients
5. **Styled Component**: Higher-level styling abstractions

This implementation plan provides a comprehensive, idiomatic Clojure translation of the Go fatih/color library while leveraging Clojure's strengths in immutability, protocols, and functional composition.
