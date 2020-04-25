(ns emails.process-emails)

;; for simple tests
(def test-emails [{:email-address "7@indeediot.com", :spam-score 0.0}
                  {:email-address "M@lice.com", :spam-score 1}
                  {:email-address "7@indeediot.com", :spam-score 0.0}
                  {:email-address "M@lice.com", :spam-score 0.1}
                  {:email-address "I@monstrous.com", :spam-score 0.1}
                  {:email-address "M@lice.com", :spam-score 0.1}
                  {:email-address "M@lice.com", :spam-score 0.2}
                  {:email-address "b1@lice.com", :spam-score 0.0}
                  {:email-address "M@lice.com", :spam-score 0.3}])

(def default-settings {:spam-score-limit 0.3
                       :limit-per-email 1
                       :running-mean {:limit 0.1
                                      :lookback 100}
                       :global-mean 0.05})

(defn generate-skip-spammy-emails-xf [spam-limit]
  (filter #(<= (:spam-score %) spam-limit)))

(defn generate-limit-per-email-xf [limit]
  (fn [xf]
    (let [contacted (atom {})]
      (fn
        ([] xf)
        ([result] (xf result))
        ([result input]
         (let [email (:email-address input)
               num-sent (get @contacted email 0)]
           (if (>= num-sent limit)
             result
             (do
               (swap! contacted assoc email (inc num-sent))
               (xf result input)))))))))

(defn add-to-circle-vec [v size item]
  (if (< (count v) size)
    (conj v item)
    (-> v
        (subvec 1)
        (conj item))))

(defn generate-running-mean-xf
  ([limit]
   (generate-running-mean-xf limit Integer/MAX_VALUE))
  ([limit lookback]
   (fn [xf]
     (let [running (atom [])]
       (fn
         ([] xf)
         ([result] (xf result))
         ([result input]
          (let [spam-score (:spam-score input)
                new-running (add-to-circle-vec @running lookback spam-score)
                new-avg (/ (reduce + new-running) (count new-running))]
            (if (<= new-avg limit)
              (do
                (reset! running new-running)
                (xf result input))
              result))))))))



(defn generate-xf [settings]
  (let [{:keys [spam-score-limit
                limit-per-email
                running-mean
                global-mean]} (merge default-settings settings)]
    (comp
     (generate-skip-spammy-emails-xf spam-score-limit)
     (generate-limit-per-email-xf limit-per-email)
     (generate-running-mean-xf global-mean)
     (generate-running-mean-xf (:limit running-mean) (:lookback running-mean)))))

(def my-xf (generate-xf {}))

(into [] my-xf test-emails)
