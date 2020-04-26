(ns emails.ladders
  (:require [emails.load-emails :as le]
            [emails.process-emails :as pe]
            [emails.process-emails-v2 :as pe-v2]
            [clojure.core.async :refer [go chan <! >! <!! >!!]])
  (:gen-class))

(def defaults {:file "resources/sample-emails-100"})

(defn- run-simple []
  (let [file (:file defaults)
        xf (pe/generate-xf)]
    (->> file
         le/load-emails-from-file
         (into [] xf))))

(defn- format-status [status]
  (let [{:keys [accepted rejected]} status
        rejected-count (->> rejected vals (reduce +))]
    (format "Accepted: %d | Rejected: %d"
            accepted rejected-count)))

(defn- run-advanced []
  (let [status (atom {})
        xf (pe-v2/generate-xf status)
        potential-emails (chan 10 xf)
        to-email (atom [])]
    ;; setup channel reading
    (go (while true
          (let [email (<! potential-emails)]
            (swap! to-email conj email))))
    ;; write to channel
    (let [email-chunks (->> defaults
                            :file
                            le/load-emails-from-file
                            (partition 10 10 []))]
      (doseq [chunk email-chunks]
        (prn (format-status @status))
        (doseq [e chunk]
          (>!! potential-emails e))
        (Thread/sleep 2000))
      (prn (format-status @status)))))


(defn -main
  [& args]
;;   (run-simple)
  (run-advanced))
