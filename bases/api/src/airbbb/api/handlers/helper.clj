(ns airbbb.api.handlers.helper)

(defn format-fail [fail]
  (tap> fail)
  (let [{:keys [code] :or {code 500} :as data} (ex-data fail)]
    {:status code
     :body {:error (ex-message fail)
            :details data}}))
