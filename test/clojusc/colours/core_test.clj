(ns clojusc.colours.core-test
  (:require [clojure.test :refer :all]
            [clojusc.colours.core :as colors]))

(deftest test-color-creation
  (testing "Basic color creation"
    (let [red-color (colors/color colors/fg-red)]
      (is (= [31] (:attributes red-color)))
      (is (false? (:no-color? red-color)))))
  
  (testing "Multiple attribute color creation"
    (let [red-bold (colors/color colors/fg-red colors/bold)]
      (is (= [31 1] (:attributes red-bold))))))

(deftest test-rgb-colors
  (testing "RGB foreground color"
    (let [orange (colors/rgb 255 128 0)]
      (is (= 255 (:r orange)))
      (is (= 128 (:g orange)))
      (is (= 0 (:b orange)))
      (is (false? (:background? orange)))))
  
  (testing "RGB background color"
    (let [orange-bg (colors/rgb-bg 255 128 0)]
      (is (= 255 (:r orange-bg)))
      (is (true? (:background? orange-bg))))))

(deftest test-color-manipulation
  (testing "Adding attributes"
    (let [red (colors/color colors/fg-red)
          red-bold (colors/add red colors/bold)]
      (is (= [31 1] (:attributes red-bold)))))
  
  (testing "Combining colors"
    (let [red (colors/color colors/fg-red)
          bold (colors/color colors/bold)
          combined (colors/combine red bold)]
      (is (= [31 1] (:attributes combined))))))

(deftest test-color-enable-disable
  (testing "Enable/disable color"
    (let [red (colors/color colors/fg-red)
          disabled (colors/disable-color red)
          enabled (colors/enable-color disabled)]
      (is (true? (:no-color? disabled)))
      (is (false? (:no-color? enabled))))))

(deftest test-string-operations
  (testing "Colorize text"
    (let [red (colors/color colors/fg-red)
          colored-text (colors/colorize red "test")]
      (is (.contains colored-text "\u001b[31m"))
      (is (.contains colored-text "test"))
      (is (.contains colored-text "\u001b[0m"))))
  
  (testing "Strip colors"
    (let [colored-text "\u001b[31mtest\u001b[0m"
          stripped (colors/strip-colors colored-text)]
      (is (= "test" stripped)))))

(deftest test-global-color-control
  (testing "Global no-color setting"
    (let [original (colors/no-color?)]
      (colors/set-no-color! true)
      (is (colors/no-color?))
      (colors/set-no-color! false)
      (is (not (colors/no-color?)))
      (colors/set-no-color! original))))