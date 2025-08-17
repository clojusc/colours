# colours

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
  (colours/print (colours/colour attr/fg-green attr/bold) "Hello, ")
  (colours/print (colours/colour attr/fg-cyan) "World")
  (colours/println (colours/colour attr/fg-green attr/bold) "!\n")

  (colours/red "This is red text\n")
  (colours/green "This is green text\n")
  (colours/blue "This is blue text\n")
  (colours/println (colours/colour attr/fg-yellow attr/bold) "Bold yellow text")

  (let [orange (colours/rgb 255 128 0)]
    (colours/println orange "This is orange RGB text"))

  (println "\nString formatting:")
  (println "Status:" (colours/green-string "SUCCESS") "- Don't worry, be happy!\n"))
```

## License

Copyright © 2025 Clojusc

Apache License, Version 2.0