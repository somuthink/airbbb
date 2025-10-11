(ns airbbb.flight.process)

(defn transfers [flights start-from end-to]
  (loop [prev-to start-from
         path []
         flights flights]
    (let [{:keys [flight/from flight/to] :as flight} (first flights)]
      (cond
        (and (= from prev-to) (= to end-to))
        (conj path (assoc flight :transfer/item :end))

        (= from start-from)
        (recur to (conj path (assoc flight :transfer/item :start)) (rest flights))

        (and (not-empty path) (= prev-to from))
        (recur to (conj path (assoc flight :transfer/item :step)) (rest flights))

        (empty? flights)
        []
        ;; (fail-with {:msg "no such transfer possible or some transfer chain flight is not available"
        ;;             :data {:code 400}})
        :else
        (recur prev-to path (rest flights))))))

(defn map-of-transfers [m]
  (letfn [(tag-mins [comp tag m]
            (update-in m
                       [(apply min-key
                               (fn [key]
                                 (-> m (get key) :path/transfers (#(apply + (map comp %)))))
                               (keys m)) :path/tags] conj tag))]
    (->> m
         (reduce-kv (fn [acc k v] (assoc acc k {:path/transfers (apply transfers v k) :path/tags #{}})) {})
         (tag-mins :flight/price :cheapest)
         (tag-mins :flight/duration :fastest)
         (reduce-kv (fn [acc [from to] {:keys [path/transfers path/tags] :as v}]
                      (cond (= (count transfers) 1)
                            (conj acc (-> transfers first (assoc :path/tags tags) (dissoc :transfer/item)))
                            (not-empty transfers)
                            (conj acc (assoc v :path/from from :path/to to))
                            :else
                            acc)) []))))


