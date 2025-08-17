(ns clojusc.colours.rgb-test
  (:require [clojure.test :refer :all]
            [clojusc.colours.rgb :as rgb]
            [clojusc.colours.ansi :as ansi]))

(deftest test-rgb-colour-creation
  (testing "RGB foreground colour creation"
    (let [orange (rgb/fg-colour 255 128 0)]
      (is (= 255 (:r orange)))
      (is (= 128 (:g orange)))
      (is (= 0 (:b orange)))
      (is (false? (:background? orange)))
      (is (false? (:no-colour? orange)))))
  
  (testing "RGB background colour creation"
    (let [orange-bg (rgb/bg-colour 255 128 0)]
      (is (= 255 (:r orange-bg)))
      (is (= 128 (:g orange-bg)))
      (is (= 0 (:b orange-bg)))
      (is (true? (:background? orange-bg)))
      (is (false? (:no-colour? orange-bg)))))
  
  (testing "RGB colour with no-colour flag"
    (let [orange (rgb/fg-colour 255 128 0 true)]
      (is (true? (:no-colour? orange))))))

(deftest test-rgb-validation
  (testing "Valid RGB values"
    (is (rgb/fg-colour 0 0 0))
    (is (rgb/fg-colour 255 255 255))
    (is (rgb/fg-colour 128 64 32)))
  
  (testing "Invalid RGB values throw errors"
    (is (thrown? AssertionError (rgb/fg-colour -1 0 0)))
    (is (thrown? AssertionError (rgb/fg-colour 256 0 0)))
    (is (thrown? AssertionError (rgb/fg-colour 0 -1 0)))
    (is (thrown? AssertionError (rgb/fg-colour 0 256 0)))
    (is (thrown? AssertionError (rgb/fg-colour 0 0 -1)))
    (is (thrown? AssertionError (rgb/fg-colour 0 0 256)))))

(deftest test-rgb-ansi-formattable
  (testing "RGB foreground format sequence"
    (let [orange (rgb/fg-colour 255 128 0)]
      (is (= "\u001b[38;2;255;128;0m" (ansi/format-sequence orange)))))
  
  (testing "RGB background format sequence"
    (let [orange-bg (rgb/bg-colour 255 128 0)]
      (is (= "\u001b[48;2;255;128;0m" (ansi/format-sequence orange-bg)))))
  
  (testing "No format sequence when no-colour is true"
    (let [orange (rgb/fg-colour 255 128 0 true)]
      (is (nil? (ansi/format-sequence orange)))))
  
  (testing "RGB colours are never reset sequences"
    (let [orange (rgb/fg-colour 255 128 0)]
      (is (false? (ansi/is-reset? orange))))))

(deftest test-rgb-colourable
  (testing "RGB colourize text"
    (let [orange (rgb/fg-colour 255 128 0)]
      (is (= "\u001b[38;2;255;128;0mtest\u001b[0m" (ansi/colourize orange "test")))))
  
  (testing "RGB background colourize text"
    (let [orange-bg (rgb/bg-colour 255 128 0)]
      (is (= "\u001b[48;2;255;128;0mtest\u001b[0m" (ansi/colourize orange-bg "test")))))
  
  (testing "No colourize when no-colour is true"
    (let [orange (rgb/fg-colour 255 128 0 true)]
      (is (= "test" (ansi/colourize orange "test")))))
  
  (testing "Strip colours from RGB coloured text"
    (let [orange (rgb/fg-colour 255 128 0)
          coloured-text "\u001b[38;2;255;128;0mtest\u001b[0m"]
      (is (= "test" (ansi/strip orange coloured-text))))))