(ns sauerworld.sdos.email
  (:require [postal.core :refer (send-message)]
            [clojure.tools.logging :refer (error)]))

(defn send-email
  "Sends an email message. Two required args -
   server - map in the format as required by postal
   message - map with :to :from :subject :body"
  [server message]
  (let [result (send-message server message)
        code (:code result)]
    (if (zero? code)
      true
      (do
        (error {:result result :server server :message message})
        false))))
