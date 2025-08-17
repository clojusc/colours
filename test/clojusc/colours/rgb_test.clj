(ns clojusc.colours.rgb-test
  (:require [clojure.test :refer :all]
            [clojusc.colours.rgb :as rgb]
            [clojusc.colours.ansi :as ansi]))

(deftest test-rgb-color-creation
  (testing "RGB foreground color creation"
    (let [orange (rgb/rgb-color 255 128 0)]
      (is (= 255 (:r orange)))
      (is (= 128 (:g orange)))
      (is (= 0 (:b orange)))
      (is (false? (:background? orange)))
      (is (false? (:no-color? orange)))))
  
  (testing "RGB background color creation"
    (let [orange-bg (rgb/rgb-bg-color 255 128 0)]
      (is (= 255 (:r orange-bg)))
      (is (= 128 (:g orange-bg)))
      (is (= 0 (:b orange-bg)))
      (is (true? (:background? orange-bg)))
      (is (false? (:no-color? orange-bg)))))
  
  (testing "RGB color with no-color flag"
    (let [orange (rgb/rgb-color 255 128 0 true)]
      (is (true? (:no-color? orange))))))

(deftest test-rgb-validation
  (testing "Valid RGB values"
    (is (rgb/rgb-color 0 0 0))
    (is (rgb/rgb-color 255 255 255))
    (is (rgb/rgb-color 128 64 32)))
  
  (testing "Invalid RGB values throw errors"
    (is (thrown? AssertionError (rgb/rgb-color -1 0 0)))
    (is (thrown? AssertionError (rgb/rgb-color 256 0 0)))
    (is (thrown? AssertionError (rgb/rgb-color 0 -1 0)))
    (is (thrown? AssertionError (rgb/rgb-color 0 256 0)))
    (is (thrown? AssertionError (rgb/rgb-color 0 0 -1)))
    (is (thrown? AssertionError (rgb/rgb-color 0 0 256)))))

(deftest test-rgb-ansi-formattable
  (testing "RGB foreground format sequence"
    (let [orange (rgb/rgb-color 255 128 0)]
      (is (= "\u001b[38;2;255;128;0m" (ansi/format-sequence orange)))))
  
  (testing "RGB background format sequence"
    (let [orange-bg (rgb/rgb-bg-color 255 128 0)]
      (is (= "\u001b[48;2;255;128;0m" (ansi/format-sequence orange-bg)))))
  
  (testing "No format sequence when no-color is true"
    (let [orange (rgb/rgb-color 255 128 0 true)]
      (is (nil? (ansi/format-sequence orange)))))
  
  (testing "RGB colors are never reset sequences"
    (let [orange (rgb/rgb-color 255 128 0)]
      (is (false? (ansi/reset-sequence? orange))))))

(deftest test-rgb-colorable
  (testing "RGB colorize text"
    (let [orange (rgb/rgb-color 255 128 0)]
      (is (= "\u001b[38;2;255;128;0mtest\u001b[0m" (ansi/colorize orange "test")))))
  
  (testing "RGB background colorize text"
    (let [orange-bg (rgb/rgb-bg-color 255 128 0)]
      (is (= "\u001b[48;2;255;128;0mtest\u001b[0m" (ansi/colorize orange-bg "test")))))
  
  (testing "No colorize when no-color is true"
    (let [orange (rgb/rgb-color 255 128 0 true)]
      (is (= "test" (ansi/colorize orange "test")))))
  
  (testing "Strip colors from RGB colored text"
    (let [orange (rgb/rgb-color 255 128 0)
          colored-text "\u001b[38;2;255;128;0mtest\u001b[0m"]
      (is (= "test" (ansi/strip-colors orange colored-text))))))