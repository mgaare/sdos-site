(ns sauerworld.sdos.models.tournaments
  (:require [korma.core :as k]
            [clj-time.core :refer (date-time now minus days)]
            [clj-time.coerce :refer (to-date to-date-time)]))

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

(defn insert-tournament
  [db {:keys [name date registration-open] :as tournament}]
  (let [registration-open (or registration-open false)
        date (to-date date)
        t-entity {:name name
                  :date date
                  :registration_open registration-open}]
    (-> (base-tournaments-query db)
        (k/insert
         (k/values t-entity)))))

(defn insert-event
  [db {:keys [tournament name team-mode]}]
  (let [team-mode (or team-mode false)
        tournament (if (number? tournament)
                     (int tournament)
                     (-> tournament :id int))]
    (-> (base-events-query db)
        (k/insert
         (k/values {:name name
                    :tournament tournament
                    :team_mode team-mode})))))

(defn insert-registration
  [db {:keys [event user team created]}]
  (let [user (if (number? user)
               (int user)
               (-> user :id int))
        event (if (number? event)
                (int event)
                (-> event :id int))
        team (or team "")
        created (if created
                  (to-date created)
                  (to-date (now)))]
    (-> (base-registrations-query db)
        (k/insert
         (k/values {:event event
                    :user user
                    :team team
                    :created created})))))

(defn get-next-tournament
  [db & [date]]
  (let [date (to-date (or date (now)))]
    (-> (base-tournaments-query db)
        (k/select
         (k/where {:date [> date]})
         (k/order :date :asc))
        first)))

(defn get-tournament-by-id
  [db id]
  (-> (base-tournaments-query db)
      (k/select
       (k/where {:id id})
       (k/limit 1))
      first))

(defn get-current-tournament
  [db & [date]]
  (let [date (to-date-time (or date (now)))
        yesterday (-> date
                      (minus (days 1))
                      to-date)]
    (-> (base-tournaments-query db)
        (k/select
         (k/where {:date [> yesterday]})
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
  (let [id (if (number? tournament)
             (int tournament)
             (-> tournament :id int))
        events (get-tournament-events db id)

        ]
    (-> (base-registrations-query db)
        (k/select
         (k/join :inner
                 (k/create-entity "events")
                 (= :events.id :event))
         (k/join (k/create-entity "users")
                 (= :users.id :user))
         (k/where {:events.tournament id})))))

(defn update-team
  [db registration team]
  (when-let [id (:id registration)]
    (-> (base-registrations-query db)
        (k/update
         (k/set-fields {"team" team})
         (k/where {:id id})))))
