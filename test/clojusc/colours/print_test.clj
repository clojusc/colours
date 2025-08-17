(ns clojusc.colours.print-test
  (:require [clojure.test :refer [deftest testing is]]
            [clojusc.colours.print :as print]
            [clojusc.colours.colour :as colour]
            [clojusc.colours.attr :as attr]))

(deftest test-global-no-colour
  (testing "NO_colour environment variable detection"
    (let [_original-no-colour print/*no-colour*]
      (binding [print/*no-colour* true]
        (is (true? print/*no-colour*)))
      (binding [print/*no-colour* false]
        (is (false? print/*no-colour*))))))

(deftest test-colour-printer-protocol
  (testing "Format coloured text"
    (let [red (colour/create-colour [attr/fg-red])]
      (is (= "\u001b[31mtest\u001b[0m" (print/format-coloured red "%s" "test")))))
  
  (testing "Format coloured text with no-colour"
    (let [red (colour/create-colour [attr/fg-red] true)]
      (is (= "test" (print/format-coloured red "%s" "test")))))
  
  (testing "Format coloured text with global no-colour"
    (let [red (colour/create-colour [attr/fg-red])]
      (binding [print/*no-colour* true]
        (is (= "test" (print/format-coloured red "%s" "test")))))))

(deftest test-function-generators
  (testing "Make print function"
    (let [red (colour/create-colour [attr/fg-red])
          red-print (print/make-print-fn red)]
      (is (fn? red-print))))
  
  (testing "Make println function"
    (let [red (colour/create-colour [attr/fg-red])
          red-println (print/make-println-fn red)]
      (is (fn? red-println))))
  
  (testing "Make printf function"
    (let [red (colour/create-colour [attr/fg-red])
          red-printf (print/make-printf-fn red)]
      (is (fn? red-printf))))
  
  (testing "Make format function"
    (let [red (colour/create-colour [attr/fg-red])
          red-format (print/make-format-fn red)]
      (is (fn? red-format))
      (is (= "\u001b[31mtest\u001b[0m" (red-format "%s" "test"))))))