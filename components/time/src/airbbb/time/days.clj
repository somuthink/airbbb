(ns airbbb.time.days)

(defn diff [from to]
  (.between java.time.temporal.ChronoUnit/DAYS
            (.toInstant from)
            (.toInstant to)))

(defn add [time num]
  (java.util.Date. (+ (.getTime time)
                      (* num 24 60 60 1000))))
