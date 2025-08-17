(ns clojusc.colours.colour-test
  (:require [clojure.test :refer :all]
            [clojusc.colours.colour :as colour]
            [clojusc.colours.attributes :as attr]
            [clojusc.colours.ansi :as ansi]))

(deftest test-colour-record
  (testing "Colour record creation"
    (let [c (colour/create-colour [attr/fg-red attr/bold])]
      (is (= [31 1] (:attributes c)))
      (is (false? (:no-colour? c)))))
  
  (testing "Colour with no-colour flag"
    (let [c (colour/create-colour [attr/fg-red] true)]
      (is (true? (:no-colour? c))))))

(deftest test-ansi-formattable
  (testing "Format sequence generation"
    (let [c (colour/create-colour [attr/fg-red attr/bold])]
      (is (= "\u001b[31;1m" (ansi/format-sequence c)))))
  
  (testing "No format when no-colour is true"
    (let [c (colour/create-colour [attr/fg-red] true)]
      (is (nil? (ansi/format-sequence c)))))
  
  (testing "Reset sequence detection"
    (let [reset-colour (colour/create-colour [attr/reset])
          regular-colour (colour/create-colour [attr/fg-red])]
      (is (ansi/reset-sequence? reset-colour))
      (is (not (ansi/reset-sequence? regular-colour))))))

(deftest test-colourable
  (testing "Colourize text"
    (let [c (colour/create-colour [attr/fg-red])]
      (is (= "\u001b[31mtest\u001b[0m" (ansi/colourize c "test")))))
  
  (testing "No colourize when no-colour is true"
    (let [c (colour/create-colour [attr/fg-red] true)]
      (is (= "test" (ansi/colourize c "test")))))
  
  (testing "Strip colours"
    (let [c (colour/create-colour [])
          coloured-text "\u001b[31mtest\u001b[0m"]
      (is (= "test" (ansi/strip-colours c coloured-text))))))

(deftest test-add-attributes
  (testing "Adding single attribute"
    (let [red (colour/create-colour [attr/fg-red])
          red-bold (colour/add-attributes red attr/bold)]
      (is (= [31 1] (:attributes red-bold)))))
  
  (testing "Adding multiple attributes"
    (let [red (colour/create-colour [attr/fg-red])
          styled (colour/add-attributes red attr/bold attr/underline)]
      (is (= [31 1 4] (:attributes styled))))))

(deftest test-colour-operations
  (testing "Combine operation"
    (let [red (colour/create-colour [attr/fg-red])
          bold (colour/create-colour [attr/bold])
          combined (colour/colour-operation :combine red bold)]
      (is (= [31 1] (:attributes combined)))))
  
  (testing "Enable operation"
    (let [disabled (colour/create-colour [attr/fg-red] true)
          enabled (colour/colour-operation :enable disabled)]
      (is (false? (:no-colour? enabled)))))
  
  (testing "Disable operation"
    (let [enabled (colour/create-colour [attr/fg-red])
          disabled (colour/colour-operation :disable enabled)]
      (is (true? (:no-colour? disabled)))))
  
  (testing "Has foreground check"
    (let [fg-colour (colour/create-colour [attr/fg-red])
          format-colour (colour/create-colour [attr/bold])]
      (is (colour/colour-operation :has-foreground? fg-colour))
      (is (not (colour/colour-operation :has-foreground? format-colour)))))
  
  (testing "Has background check"
    (let [bg-colour (colour/create-colour [attr/bg-red])
          fg-colour (colour/create-colour [attr/fg-red])]
      (is (colour/colour-operation :has-background? bg-colour))
      (is (not (colour/colour-operation :has-background? fg-colour)))))
  
  (testing "Has formatting check"
    (let [formatted (colour/create-colour [attr/bold])
          plain (colour/create-colour [attr/fg-red])]
      (is (colour/colour-operation :has-formatting? formatted))
      (is (not (colour/colour-operation :has-formatting? plain))))))