(ns sdos-site.db
  (:require [korma.db :as db]
            [korma.core :as k]
            [clojure.java.jdbc :as sql]
            [clojure.string :as str]
            [clojurewerkz.scrypt.core :as sc]
            [clj-time.core :refer (now)]
            [clj-time.coerce :refer (to-date)])
  (:import [java.util UUID]))

(defn create-db
  [resource-loc]
  (let [db-spec (db/h2 {:db resource-loc
                        :naming {:keys str/lower-case
                                 :fields str/upper-case}})]
    (-> db-spec
        db/create-db
        (assoc :config db-spec))))

(defn create-articles-table
  [db-spec]
  (sql/with-connection db-spec
    (sql/create-table
     :articles
     [:id "INTEGER PRIMARY KEY AUTO_INCREMENT"]
     [:date :timestamp]
     [:title "varchar"]
     [:author "varchar"]
     [:content "varchar"]
     [:category "varchar"])))

(defn create-users-table
  [db-spec]
  (sql/with-connection db-spec
    (sql/create-table
     :users
     [:id "INTEGER PRIMARY KEY AUTO_INCREMENT"]
     [:username "varchar"]
     [:password "varchar"]
     [:email "varchar"]
     [:validation_key "varchar"]
     [:validated "boolean"]
     [:pubkey "varchar"]
     [:created :timestamp]
     [:admin "boolean"])))

(defn base-articles-query
  [db]
  (-> (k/create-entity "articles")
      (k/database db)))

(defn insert-article
  [db {:keys [date title author content category]}]
  (let [date (or date (to-date (now)))
        article {:date date
                 :title title
                 :author author
                 :content content
                 :category category}]
    (-> (base-articles-query db)
        (k/insert article))))

(defn update-article
  [db {:keys [id date title author content category]}]
  (-> (base-articles-query db)
      (k/update
       (k/set-fields
        {:date date
         :title title
         :author author
         :content content
         :category category})
       (k/where {:id id}))))

(defn find-all-articles
  [db]
  (-> (base-articles-query db)
      (k/select (k/order :id :DESC))))

(defn find-article
  [db id]
  (-> (base-articles-query db)
      (k/select (k/where {:id id}))
      (first)))

(defn find-category-articles
  [db cat]
  (-> (base-articles-query db)
      (k/select (k/where {:category cat})
                (k/order :id :DESC))))

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
