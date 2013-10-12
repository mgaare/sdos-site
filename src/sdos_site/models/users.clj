(ns sdos-site.models.users
  (:require [clojurewerkz.scrypt.core :as sc]
            [korma.core :as k]
            [clj-time.coerce :refer (to-date)]
            [clj-time.core :refer (now)])
  (:import [java.util UUID]))

(defn hash-password
  [pw]
  (sc/encrypt pw 16384 8 1))

(defn check-password
  [pw hash]
  (sc/verify pw hash))

(defn random-uuid
  []
  (str (java.util.UUID/randomUUID)))

(defn base-users-query
  [db]
  (-> (k/create-entity "users")
      (k/database db)))

(defn insert-user
  [db {:keys [username password email admin
              pubkey validation-key validated created]}]
  (let [admin (true? admin)
        pubkey (or pubkey nil)
        validation-key (or validation-key (random-uuid))
        validated (or validated false)
        password (hash-password password)
        created (or created (to-date (now)))]
    (-> (base-users-query db)
        (k/insert
         (k/values {:username username
                    :password password
                    :email email
                    :validation_key validation-key
                    :validated validated
                    :pubkey pubkey
                    :created created
                    :admin admin})))))

(defn validate-key
  [db validation-key]
  (let [base (base-users-query db)
        user (-> base
                 (k/select (k/where {:validation_key validation-key}))
                 first)]
    (when user
      (do
        (-> base
            (k/update
             (k/set-fields {:validated true})
             (k/where {:id (:id user)})))
        true))))

(defn check-login
  [db username password]
  (when-let [user (-> (base-users-query db)
                 (k/select (k/where {:username username}))
                 first)]
    (when (check-password password (:password user))
      user)))

(defn add-pubkey
  [db user pubkey]
  (when-let [id (:id user)]
    (-> (base-users-query db)
        (k/update
         (k/set-fields {:pubkey pubkey})
         (k/where {:id id})))))
