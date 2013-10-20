(ns sauerworld.sdos.models.users
  (:require [clojurewerkz.scrypt.core :as sc]
            [korma.core :as k]
            [clj-time.coerce :refer (to-date)]
            [clj-time.core :refer (now)]
            [validateur.validation :as v])
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

(defn get-by-validation-key
  [db validation-key]
  (let [base (base-users-query db)]
    (-> base
        (k/select (k/where {:validation_key validation-key}))
        first)))

(defn set-validated
  [db user]
  (when-let [id (:id user)]
    (-> (base-users-query db)
        (k/update
         (k/set-fields {"VALIDATED" true})
         (k/where {:id id})))))

(defn get-by-username
  [db username]
  (-> (base-users-query db)
      (k/select (k/where {:username username}))
      first))

(defn check-login
  [db username password]
  (when-let [user (get-by-username db username)]
    (when (check-password password (:password user))
      user)))

(defn add-pubkey
  [db user pubkey]
  (when-let [id (:id user)]
    (-> (base-users-query db)
        (k/update
         (k/set-fields {"PUBKEY" pubkey})
         (k/where {:id id})))))

(defn update-password
  "Updates a user's password."
  [db user new-password]
  (when-let [id (:id user)]
    (-> (base-users-query db)
        (k/update
         (k/set-fields {"PASSWORD" (hash-password new-password)})
         (k/where {:id id})))))

(defn match-of
  "Creates validation function that specifies that two attributes must be the
   same (for example, for password & password confirmation fields."
  [attribute1 attribute2]
  (let [getter (fn [k] (fn [m] (if (vector? k)
                                 (get-in m k)
                                 (get m k))))
        f1 (getter attribute1)
        f2 (getter attribute2)]
    (fn [m]
      (let [[v1 v2] ((juxt f1 f2) m)
            res (= v1 v2)
            msg (str "must match " attribute2)
            errors (if res {} {attribute1 #{msg}})]
        [(empty? errors) errors]))))

(defn uniqueness-of
  "Validation function that checks that a value unique. Takes a key to retrieve
   the value, and a function that, when passed the value, will report its
   uniqueness. Like, for instance, a database query function."
  [attribute check-fn]
  (let [f (if (vector? attribute) get-in get)]
    (fn [m]
      (let [value (f m attribute)
            unique? (check-fn value)
            msg (str value " not available, already taken.")
            errors (if unique? {} {attribute #{msg}})]
        [(empty? errors) errors]))))

(def password-match
  (match-of :password :password-confirm))

(defn make-registration-validator
  [db]
  (v/validation-set
   (v/presence-of :email)
   (v/presence-of :password)
   password-match
   (v/presence-of :username)
   (uniqueness-of :username #(->> %
                                 (get-by-username db)
                                 nil?))))

(def password-validator
  (v/validation-set
   (v/presence-of :password)
   password-match))
