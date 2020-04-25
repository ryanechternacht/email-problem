(ns emails.ladders
  (:require [emails.load-emails :as le])
  (:gen-class))

(def defaults {:file "resources/sample-emails-100"})

(defn -main
  "I don't do a whole lot ... yet."
  [& args]
  (le/load-emails-from-file (:file defaults)))

;; TESTING
;; (le/load-emails-from-file "resources/sample-emails-100")
