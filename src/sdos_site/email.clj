(ns sdos-site.email
  (:require [postal.core :refer (send-message)]
            [clojure.tools.logging :refer (error)]))

(defn send
  [server message]
  (let [result (send-message server message)
        code (:code result)]
    (if (zero? code)
      true
      (do
        (error {:result result :server server :message message})
        false))))
