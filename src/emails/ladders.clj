(ns emails.ladders
  (:require [emails.load-emails :as le]
            [emails.process-emails :as pe])
  (:gen-class))

(def defaults {:file "resources/sample-emails-100"})

(defn -main
  "I don't do a whole lot ... yet."
  [& args]
  (let [xf (pe/generate-xf {})]
    (->> (:file defaults)
         le/load-emails-from-file
         (into [] xf))))
