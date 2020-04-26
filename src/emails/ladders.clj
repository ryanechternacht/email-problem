(ns emails.ladders
  (:require [emails.load-emails :as le]
            [emails.process-emails :as pe]
            [clojure.core.async :refer [go chan <! >! <!! >!! alts!]])
  (:gen-class))

(def defaults {:file "resources/sample-emails-100"})

;; (def xf (pe/generate-xf))

;; (def potential-emails (chan 10 xf))

;; (def to-email (atom []))

;; (go (while true
;;       (let [email (<! potential-emails)]
;;         (swap! to-email conj email))))

(defn- run-simple []
  (let [file (:file defaults)
        xf (pe/generate-xf)]
    (->> file
         le/load-emails-from-file
         (into [] xf))))



(defn -main
  [& args]
  (run-simple))

;; (defn -main
;;   "I don't do a whole lot ... yet."
;;   [& args]
;;   (let [emails (le/load-emails-from-file (:file defaults))]
;;     (for [e emails]
;;       (go
;;         (>! potential-emails e)))))
