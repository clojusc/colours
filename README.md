# colours

[![Build Status][gh-actions-badge]][gh-actions]

[![Project Logo][logo]][logo-large]

*Another ANSI colour library for Clojure*

## Installation

`deps.edn`:

```clojure
{:deps {com.github.clojusc/colours {:mvn/version "0.1.0"}}}
```

A lein `project.clj` file:


```clojure
(defproject my-project "1.0.0"
  :dependencies [[com.github.clojusc/colours "0.1.0"]])
```

## Demo

Run the project's demo directly, via `:exec-fn`:

    $ clojure -X:run-x
    Hello, Clojure!

## Usage

The demo runs example code like the following:

```clojure
(ns clojusc.colours
  (:require [clojusc.colours.core :as c]
            [clojusc.colours.attr :as attr]))
  
(defn my-fun []
  (c/print (c/colour attr/fg-green attr/bold) "Hello, ")
  (c/print (c/colour attr/fg-cyan) "World")
  (c/println (c/colour attr/fg-green attr/bold) "!\n")

  (c/red "This is red text\n")
  (c/green "This is green text\n")
  (c/blue "This is blue text\n")
  (c/println (c/colour attr/fg-yellow attr/bold) "Bold yellow text")

  (let [orange (c/rgb 255 128 0)]
    (c/println orange "This is orange RGB text"))

  (println "\nString formatting:")
  (println "Status:" (c/str-green "SUCCESS") "- Don't worry, be happy!\n"))
```

## License

Copyright © 2025 Clojusc

Apache License, Version 2.0

[//]: ---Named-Links---

[logo]: https://github.com/clojusc/colours/blob/main/resources/images/logo.jpg?raw=true
[logo-large]: https://github.com/clojusc/colours/blob/main/resources/images/logo-large.jpg?raw=true
[gh-actions-badge]: https://github.com/clojusc/colours/workflows/CI%2FCD/badge.svg
[gh-actions]: https://github.com/clojusc/colours/actions?query=workflow%3ACI%2FCD
