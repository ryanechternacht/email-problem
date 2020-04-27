(ns emails.ladders
  (:require [emails.load-emails :as le]
            [emails.process-emails :as pe]
            [emails.process-emails-v2 :as pe-v2]
            [emails.generate-test-emails :as gte]
            [clojure.core.async :refer [go chan <! >! <!! >!!]])
  (:gen-class))

(def defaults {:file "resources/sample-emails-100"
               :emails 100000
               :partitions 10000})

(defn- run-simple []
  (let [file (:file defaults)
        xf (pe/generate-xf)]
    (->> file
         le/load-emails-from-file
         (into [] xf))))

(defn- format-status [to-email status]
  (let [accepted (count to-email)
        {:keys [too-spammy
                limit-per-user
                global-mean
                running-mean] :as rejected} (:rejected status)
        rejected-count (->> rejected vals (reduce +))]
    (format (str "Accepted: %d\r\n"
                 "Rejected: %d\r\n"
                 "- Too Spammy: %d\r\n"
                 "- User Limit: %d\r\n"
                 "- Global Mean: %d\r\n"
                 "- Running Mean: %d\r\n")
            accepted rejected-count too-spammy
            limit-per-user global-mean running-mean)))

(defn- run-advanced []
  (let [status (atom {})
        xf (pe-v2/generate-xf status)
        potential-emails (chan 10 xf)
        to-email (atom [])
        {:keys [emails partitions]} defaults]
    ;; setup channel reading
    (go (while true
          (let [email (<! potential-emails)]
            (swap! to-email conj email))))
    ;; write to channel
    (let [email-chunks (->> (gte/make-sample-emails emails)
                            (partition partitions partitions []))]
      (doseq [chunk email-chunks]
        (println (format-status @to-email @status))
        (doseq [e chunk]
          (>!! potential-emails e)))
      (println (format-status @to-email @status)))))


(defn -main
  [& args]
;;   (run-simple)
  (run-advanced))
