(ns sdos-site.models.tournaments
  (:require [korma.core :as k]
            [clj-time.core :refer (date-time now minus days)]
            [clj-time.coerce :refer (to-date)]))

(defn base-tournaments-query
  [db]
  (-> (k/create-entity "tournaments")
      (k/database db)))

(defn base-events-query
  [db]
  (-> (k/create-entity "events")
      (k/database db)))

(defn base-registrations-query
  [db]
  (-> (k/create-entity "registrations")
      (k/database db)))

(defn get-next-tournament
  [db & [date]]
  (let [date (to-date (or date (now)))]
    (-> (base-tournaments-query db)
        (k/select
         (k/where {:date [> date]})
         (k/order :date :asc))
        first)))

(defn get-current-tournament
  [db & [date]]
  (let [date (date-time (or date (now)))
        yesterday (-> date
                      (minus (days 1))
                      to-date)]
    (-> (base-tournaments-query db)
        (k/select
         (k/where {:date [> date]})
         (k/order :date :asc)))))

(defn get-tournaments
  [db]
  (-> (base-tournaments-query db)
      (k/select)))

(defn get-tournament-events
  [db tournament]
  {:pre [(number? (:id tournament))]}
  (let [id (:id tournament)]
    (-> (base-events-query db)
        (k/select
         (k/where {:tournament id})))))

(defn get-event-signups
  [db event]
  {:pre [(number? (:id event))]}
  (let [id (:id event)]
    (-> (base-registrations-query db)
        (k/select
         (k/where {:event id})))))

(defn get-tournament-signups
  [db tournament]
  {:pre [(number? (:id tournament))]}
  (let [id (:id tournament)]
    (-> (base-registrations-query db)
        (k/select
         (k/join :inner
                 (k/create-entity "events")
                 (= :events.id :event))
         (k/join (k/create-entity "users")
                 (= :users.id :user))
         (k/where {:events.tournament id})
         (k/group :event)))))
