(ns airbbb.book.process
  (:require
   [airbbb.time.interface :as time]))

(defn available-rooms [rooms days current-time]
  (keep (fn [room]
          (-> room
              (update :room/books
                      #(if (not-empty %)
                         (->> %
                              (sort-by :book/start compare)
                              (partition 2 1)
                              (keep (fn [[{:keys [book/end]} {next-start :book/start}]]
                                      (let [available-days (time/days-diff end next-start)]
                                        (when (< days available-days)
                                          {:book/min-start end
                                           :book/max-end next-start
                                           :book/max-days available-days}))))
                              seq)
                         {:book/min-start (time/add-days current-time days)
                          :book/min-end current-time
                          :book/min-days days}))
              (#(when (:room/books %) %)))) rooms))
