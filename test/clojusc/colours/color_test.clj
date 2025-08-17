(ns clojusc.colours.color-test
  (:require [clojure.test :refer :all]
            [clojusc.colours.color :as color]
            [clojusc.colours.attributes :as attr]
            [clojusc.colours.ansi :as ansi]))

(deftest test-color-record
  (testing "Color record creation"
    (let [c (color/create-color [attr/fg-red attr/bold])]
      (is (= [31 1] (:attributes c)))
      (is (false? (:no-color? c)))))
  
  (testing "Color with no-color flag"
    (let [c (color/create-color [attr/fg-red] true)]
      (is (true? (:no-color? c))))))

(deftest test-ansi-formattable
  (testing "Format sequence generation"
    (let [c (color/create-color [attr/fg-red attr/bold])]
      (is (= "\u001b[31;1m" (ansi/format-sequence c)))))
  
  (testing "No format when no-color is true"
    (let [c (color/create-color [attr/fg-red] true)]
      (is (nil? (ansi/format-sequence c)))))
  
  (testing "Reset sequence detection"
    (let [reset-color (color/create-color [attr/reset])
          regular-color (color/create-color [attr/fg-red])]
      (is (ansi/reset-sequence? reset-color))
      (is (not (ansi/reset-sequence? regular-color))))))

(deftest test-colorable
  (testing "Colorize text"
    (let [c (color/create-color [attr/fg-red])]
      (is (= "\u001b[31mtest\u001b[0m" (ansi/colorize c "test")))))
  
  (testing "No colorize when no-color is true"
    (let [c (color/create-color [attr/fg-red] true)]
      (is (= "test" (ansi/colorize c "test")))))
  
  (testing "Strip colors"
    (let [c (color/create-color [])
          colored-text "\u001b[31mtest\u001b[0m"]
      (is (= "test" (ansi/strip-colors c colored-text))))))

(deftest test-add-attributes
  (testing "Adding single attribute"
    (let [red (color/create-color [attr/fg-red])
          red-bold (color/add-attributes red attr/bold)]
      (is (= [31 1] (:attributes red-bold)))))
  
  (testing "Adding multiple attributes"
    (let [red (color/create-color [attr/fg-red])
          styled (color/add-attributes red attr/bold attr/underline)]
      (is (= [31 1 4] (:attributes styled))))))

(deftest test-color-operations
  (testing "Combine operation"
    (let [red (color/create-color [attr/fg-red])
          bold (color/create-color [attr/bold])
          combined (color/color-operation :combine red bold)]
      (is (= [31 1] (:attributes combined)))))
  
  (testing "Enable operation"
    (let [disabled (color/create-color [attr/fg-red] true)
          enabled (color/color-operation :enable disabled)]
      (is (false? (:no-color? enabled)))))
  
  (testing "Disable operation"
    (let [enabled (color/create-color [attr/fg-red])
          disabled (color/color-operation :disable enabled)]
      (is (true? (:no-color? disabled)))))
  
  (testing "Has foreground check"
    (let [fg-color (color/create-color [attr/fg-red])
          format-color (color/create-color [attr/bold])]
      (is (color/color-operation :has-foreground? fg-color))
      (is (not (color/color-operation :has-foreground? format-color)))))
  
  (testing "Has background check"
    (let [bg-color (color/create-color [attr/bg-red])
          fg-color (color/create-color [attr/fg-red])]
      (is (color/color-operation :has-background? bg-color))
      (is (not (color/color-operation :has-background? fg-color)))))
  
  (testing "Has formatting check"
    (let [formatted (color/create-color [attr/bold])
          plain (color/create-color [attr/fg-red])]
      (is (color/color-operation :has-formatting? formatted))
      (is (not (color/color-operation :has-formatting? plain))))))