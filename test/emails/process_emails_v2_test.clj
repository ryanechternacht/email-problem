(ns emails.process-emails-v2-test
  (:require [clojure.test :refer [deftest is testing]]
            [emails.process-emails-v2 :as pe]))

(def test-emails [{:email-address "hello3@hello.com" :spam-score 0.9}
                  {:email-address "hello2@hello.com" :spam-score 0.6}
                  {:email-address "hello@hello.com" :spam-score 0.1}
                  {:email-address "hello2@hello.com" :spam-score 0.7}
                  {:email-address "hello@hello.com" :spam-score 0}
                  {:email-address "hello4@hello.com" :spam-score 1}
                  {:email-address "hello2@hello.com" :spam-score 0.3}
                  {:email-address "hello2@hello.com" :spam-score 0.4}
                  {:email-address "hello2@hello.com" :spam-score 0.5}
                  {:email-address "hello2@hello.com" :spam-score 0.8}
                  {:email-address "hello@hello.com" :spam-score 0.2}])

(deftest genereate-skip-spammy-emails-xf-test
  (testing "Generate Skip Spammy Emails Xf Test"
    (let [status (atom {:accepted 0 :rejected {:too-spammy 0} :other 4})
          xf (pe/generate-skip-spammy-emails-xf status 0.5)
          result (into [] xf test-emails)]
      (is (= 6 (count result)) "spammy emails are skipped")
      (is (= 5 (get-in @status [:rejected :too-spammy]))
          "skipped causes is updated correclty")
      (is (= 6 (:accepted @status)) "accepted is updated correctly")
      (is (= 4 (:other @status)) "other settings are unaffected"))))

(deftest generate-limit-per-email-xf-test
  (testing "Generate Limit Per Email Xf Test"
    (let [status (atom {:accepted 0 :rejected {:limit-per-user 0} :other 4})
          xf (pe/generate-limit-per-email-xf status 2)
          result (into [] xf test-emails)]
      (is (= 6 (count result)) "don't over-email people")
      (is (= 5 (get-in @status [:rejected :limit-per-user]))
          "skipped causes is updated correclty")
      (is (= 6 (:accepted @status)) "accepted is updated correctly")
      (is (= 4 (:other @status)) "other settings are unaffected"))))

(deftest add-to-circle-vec-test
  (testing "Add To Circle Vec Test"
    (let [v [1 2 3]]
      (let [v+1 (pe/add-to-circle-vec v 4 4)]
        (is (= [1 2 3 4] v+1) "can add to a vector with room"))
      (let [v+1 (pe/add-to-circle-vec v 3 4)]
        (is (= [2 3 4] v+1) "drops from the beginning ands to the end if its full")))))

(deftest generate-running-mean-xf-test
  (testing "Generate Running Mean Xf Test"
    (let [status (atom {:accepted 0 :rejected {:mean 0} :other 4})
          xf (pe/generate-running-mean-xf status :mean 0.4 2)
          result (into [] xf test-emails)]
      (is (= 6 (count result)) "running mean works properly")
      (is (= 5 (get-in @status [:rejected :mean]))
          "skipped causes is updated correctly")
      (is (= 6 (:accepted @status)) "accepted is updated correctly")
      (is (= 4 (:other @status)) "other settings are unaffected"))))
