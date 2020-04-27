(ns emails.generate-test-emails
  (:require [clojure.spec.alpha :as s]
            [clojure.spec.gen.alpha :as gen]))
            ;; [emails.spec.emails :as emails-spec]

;; These should not be copied here, but I don't understand
;; auto-resolve keywords, and how I can reference
;; ::email-record from another file
(def email-domains
  #{"indeediot.com"
    "monstrous.com"
    "linkedarkpattern.com"
    "dired.com"
    "lice.com"
    "careershiller.com"
    "glassbore.com"})

(def email-regex
  #"^[a-zA-Z0-9._%+-]+@[a-zA-Z0-9.-]+\.[a-zA-Z]{2,63}$")

(s/def ::email-address
  (s/with-gen
    (s/and string? #(re-matches email-regex %))
    #(->>
      (gen/tuple (gen/such-that not-empty (gen/string-alphanumeric))
                 (s/gen email-domains))
      (gen/fmap (fn [[addr domain]] (str addr "@" domain))))))

(s/def ::spam-score
  (s/double-in :min 0 :max 1))

(s/def ::email-record
  (s/keys :req-un [::email-address ::spam-score]))

(defn make-sample-email-file
  "generates email records based on the ::email-record spec"
  [file num]
  (with-open [w (clojure.java.io/writer file)]
    (binding [*out* w]
      (pr (gen/sample (s/gen ::email-record) num)))))

(defn make-sample-emails
  ([] (make-sample-emails 100))
  ([num] (gen/sample (s/gen ::email-record) num)))
