(ns clojusc.colours.attributes)

;; Base formatting attributes
(def ^:const reset 0)
(def ^:const bold 1)
(def ^:const faint 2)
(def ^:const italic 3)
(def ^:const underline 4)
(def ^:const blink-slow 5)
(def ^:const blink-rapid 6)
(def ^:const reverse-video 7)
(def ^:const concealed 8)
(def ^:const crossed-out 9)

;; Foreground colours
(def ^:const fg-black 30)
(def ^:const fg-red 31)
(def ^:const fg-green 32)
(def ^:const fg-yellow 33)
(def ^:const fg-blue 34)
(def ^:const fg-magenta 35)
(def ^:const fg-cyan 36)
(def ^:const fg-white 37)

;; High-intensity foreground colours
(def ^:const fg-hi-black 90)
(def ^:const fg-hi-red 91)
(def ^:const fg-hi-green 92)
(def ^:const fg-hi-yellow 93)
(def ^:const fg-hi-blue 94)
(def ^:const fg-hi-magenta 95)
(def ^:const fg-hi-cyan 96)
(def ^:const fg-hi-white 97)

;; Background colours
(def ^:const bg-black 40)
(def ^:const bg-red 41)
(def ^:const bg-green 42)
(def ^:const bg-yellow 43)
(def ^:const bg-blue 44)
(def ^:const bg-magenta 45)
(def ^:const bg-cyan 46)
(def ^:const bg-white 47)

;; High-intensity background colours
(def ^:const bg-hi-black 100)
(def ^:const bg-hi-red 101)
(def ^:const bg-hi-green 102)
(def ^:const bg-hi-yellow 103)
(def ^:const bg-hi-blue 104)
(def ^:const bg-hi-magenta 105)
(def ^:const bg-hi-cyan 106)
(def ^:const bg-hi-white 107)

;; Attribute type classifications
(def format-attributes #{bold faint italic underline blink-slow blink-rapid 
                         reverse-video concealed crossed-out})
(def fg-colour-attributes #{fg-black fg-red fg-green fg-yellow fg-blue
                           fg-magenta fg-cyan fg-white fg-hi-black fg-hi-red 
                           fg-hi-green fg-hi-yellow fg-hi-blue fg-hi-magenta 
                           fg-hi-cyan fg-hi-white})
(def bg-colour-attributes #{bg-black bg-red bg-green bg-yellow bg-blue
                           bg-magenta bg-cyan bg-white bg-hi-black bg-hi-red 
                           bg-hi-green bg-hi-yellow bg-hi-blue bg-hi-magenta 
                           bg-hi-cyan bg-hi-white})