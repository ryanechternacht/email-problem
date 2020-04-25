(ns emails.load-emails)

(defn load-emails-from-file
  [file]
  (-> file
      slurp
      read-string))