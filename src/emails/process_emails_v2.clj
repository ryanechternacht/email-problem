(ns emails.process-emails-v2)

(def default-settings {:spam-score-limit 0.3
                       :limit-per-email 1
                       :running-mean {:limit 0.1
                                      :lookback 100}
                       :global-mean 0.05})

(defn generate-skip-spammy-emails-xf [status spam-limit]
  (fn [xf]
    (fn
      ([] xf)
      ([result] (xf result))
      ([result input]
       (if (<= (:spam-score input) spam-limit)
         (xf result input)
         (do
           (swap! status
                  (fn [m]
                    (let [new-count (-> m :skipped-causes :too-spammy inc)]
                      (assoc-in m [:skipped-causes :too-spammy] new-count))))
           result))))))

(defn generate-limit-per-email-xf [status limit]
  (fn [xf]
    (let [contacted (atom {})]
      (fn
        ([] xf)
        ([result] (xf result))
        ([result input]
         (let [email (:email-address input)
               num-sent (get @contacted email 0)]
           (if (>= num-sent limit)
             (do
               (swap! status
                      (fn [m]
                        (let [new-count (-> m :skipped-causes :limit-per-user inc)]
                          (assoc-in m [:skipped-causes :limit-per-user] new-count))))
               result)
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
  ([status setting limit]
   (generate-running-mean-xf status setting limit Integer/MAX_VALUE))
  ([status setting limit lookback]
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
              (do
                (swap! status
                       (fn [m]
                         (let [new-count (-> m :skipped-causes setting inc)]
                           (assoc-in m [:skipped-causes setting] new-count))))
                result)))))))))

(defn generate-xf
  ([status] (generate-xf status {}))
  ([status settings]
   (let [{:keys [spam-score-limit
                 limit-per-email
                 running-mean
                 global-mean]} (merge default-settings settings)]
     (comp
      (generate-skip-spammy-emails-xf status spam-score-limit)
      (generate-limit-per-email-xf status limit-per-email)
      (generate-running-mean-xf status :global-mean global-mean)
      (generate-running-mean-xf status :running-mean (:limit running-mean) (:lookback running-mean))))))
