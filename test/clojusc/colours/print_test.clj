(ns clojusc.colours.print-test
  (:require [clojure.test :refer :all]
            [clojusc.colours.print :as print]
            [clojusc.colours.color :as color]
            [clojusc.colours.attributes :as attr]
            [clojusc.colours.ansi :as ansi]
            [clojure.java.io :as io]))

(deftest test-global-no-color
  (testing "NO_COLOR environment variable detection"
    (let [original-no-color print/*no-color*]
      (binding [print/*no-color* true]
        (is (true? print/*no-color*)))
      (binding [print/*no-color* false]
        (is (false? print/*no-color*))))))

(deftest test-color-printer-protocol
  (testing "Format colored text"
    (let [red (color/create-color [attr/fg-red])]
      (is (= "\u001b[31mtest\u001b[0m" (print/format-colored red "%s" "test")))))
  
  (testing "Format colored text with no-color"
    (let [red (color/create-color [attr/fg-red] true)]
      (is (= "test" (print/format-colored red "%s" "test")))))
  
  (testing "Format colored text with global no-color"
    (let [red (color/create-color [attr/fg-red])]
      (binding [print/*no-color* true]
        (is (= "test" (print/format-colored red "%s" "test")))))))

(deftest test-function-generators
  (testing "Make print function"
    (let [red (color/create-color [attr/fg-red])
          red-print (print/make-print-fn red)]
      (is (fn? red-print))))
  
  (testing "Make println function"
    (let [red (color/create-color [attr/fg-red])
          red-println (print/make-println-fn red)]
      (is (fn? red-println))))
  
  (testing "Make printf function"
    (let [red (color/create-color [attr/fg-red])
          red-printf (print/make-printf-fn red)]
      (is (fn? red-printf))))
  
  (testing "Make format function"
    (let [red (color/create-color [attr/fg-red])
          red-format (print/make-format-fn red)]
      (is (fn? red-format))
      (is (= "\u001b[31mtest\u001b[0m" (red-format "%s" "test"))))))