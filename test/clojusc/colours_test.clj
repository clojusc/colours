(ns clojusc.colours-test
  (:require [clojure.test :refer :all]
            [clojusc.colours.core :as colors]))

(deftest basic-functionality-test
  (testing "Basic color library functionality"
    (let [red-color (colors/color colors/fg-red)]
      (is (= [31] (:attributes red-color)))
      (is (string? (colors/colorize red-color "test"))))))
