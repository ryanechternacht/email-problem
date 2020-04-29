(ns emails.generate-test-emails
  (:require [clojure.spec.alpha :as s]
            [clojure.spec.gen.alpha :as gen]))

(defn make-sample-emails
  ([] (make-sample-emails 100))
  ([num] (gen/sample (s/gen :emails.spec.emails/email-record) num)))
