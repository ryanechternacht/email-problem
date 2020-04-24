(ns emails.load-emails
  (:require [clojure.java.io :as io]))

(defn load-emails-from-file
  [file]
  (-> file
      slurp
      read-string))