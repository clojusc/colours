(ns clojusc.colours-test
  (:require [clojure.test :refer :all]
            [clojusc.colours.core :as colours]
            [clojusc.colours.attr :as attr]))

(deftest basic-functionality-test
  (testing "Basic colour library functionality"
    (let [red-colour (colours/colour attr/fg-red)]
      (is (= [31] (:attributes red-colour)))
      (is (string? (colours/colourize red-colour "test"))))))
