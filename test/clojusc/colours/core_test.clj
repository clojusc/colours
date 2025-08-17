(ns clojusc.colours.core-test
  (:require [clojure.test :refer :all]
            [clojusc.colours.core :as colours]))

(deftest test-colour-creation
  (testing "Basic colour creation"
    (let [red-colour (colours/colour colours/fg-red)]
      (is (= [31] (:attributes red-colour)))
      (is (false? (:no-colour? red-colour)))))
  
  (testing "Multiple attribute colour creation"
    (let [red-bold (colours/colour colours/fg-red colours/bold)]
      (is (= [31 1] (:attributes red-bold))))))

(deftest test-rgb-colours
  (testing "RGB foreground colour"
    (let [orange (colours/rgb 255 128 0)]
      (is (= 255 (:r orange)))
      (is (= 128 (:g orange)))
      (is (= 0 (:b orange)))
      (is (false? (:background? orange)))))
  
  (testing "RGB background colour"
    (let [orange-bg (colours/rgb-bg 255 128 0)]
      (is (= 255 (:r orange-bg)))
      (is (true? (:background? orange-bg))))))

(deftest test-colour-manipulation
  (testing "Adding attributes"
    (let [red (colours/colour colours/fg-red)
          red-bold (colours/add red colours/bold)]
      (is (= [31 1] (:attributes red-bold)))))
  
  (testing "Combining colours"
    (let [red (colours/colour colours/fg-red)
          bold (colours/colour colours/bold)
          combined (colours/combine red bold)]
      (is (= [31 1] (:attributes combined))))))

(deftest test-colour-enable-disable
  (testing "Enable/disable colour"
    (let [red (colours/colour colours/fg-red)
          disabled (colours/disable-colour red)
          enabled (colours/enable-colour disabled)]
      (is (true? (:no-colour? disabled)))
      (is (false? (:no-colour? enabled))))))

(deftest test-string-operations
  (testing "Colourize text"
    (let [red (colours/colour colours/fg-red)
          coloured-text (colours/colourize red "test")]
      (is (.contains coloured-text "\u001b[31m"))
      (is (.contains coloured-text "test"))
      (is (.contains coloured-text "\u001b[0m"))))
  
  (testing "Strip colours"
    (let [coloured-text "\u001b[31mtest\u001b[0m"
          stripped (colours/strip-colours coloured-text)]
      (is (= "test" stripped)))))

(deftest test-global-colour-control
  (testing "Global no-colour setting"
    (let [original (colours/no-colour?)]
      (colours/set-no-colour! true)
      (is (colours/no-colour?))
      (colours/set-no-colour! false)
      (is (not (colours/no-colour?)))
      (colours/set-no-colour! original))))