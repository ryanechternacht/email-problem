(ns emails.ladders
  (:require [emails.load-emails :as le])
  (:gen-class))

(defn -main
  "I don't do a whole lot ... yet."
  [& args]
  (println "Hello, World!"))

;; TESTING
(le/load-emails-from-file "resources/sample-emails-100")