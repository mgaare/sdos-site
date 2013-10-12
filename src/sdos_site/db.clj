(ns sdos-site.db
  (:require [korma.db :as db]
            [clojure.java.jdbc :as sql]
            [clojure.string :as str]))

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

(def tables
  {:articles create-articles-table
   :users create-users-table})
