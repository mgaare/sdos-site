(ns sauerworld.sdos.db
  (:require [korma.db :as db]
            [clojure.java.jdbc :as sql]
            [clojure.java.jdbc.ddl :as ddl]
            [clojure.string :as str]))

(defn create-h2-spec
  [resource-loc]
  (db/h2 {:db resource-loc
          :naming {:keys str/lower-case
                   :fields str/upper-case}}))

(defn create-db
  [resource-loc]
  (let [db-spec (create-h2-spec resource-loc)]
    (-> db-spec
        db/create-db
        (assoc :config db-spec))))

(defn create-articles-table
  [db-spec]
  (sql/db-do-commands db-spec
    (ddl/create-table
     :articles
     [:id "INTEGER PRIMARY KEY AUTO_INCREMENT"]
     [:date :timestamp]
     [:title "varchar"]
     [:author "varchar"]
     [:content "varchar"]
     [:category "varchar"])))

(defn create-users-table
  [db-spec]
  (sql/db-do-commands db-spec
    (ddl/create-table
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

(defn create-tournaments-table
  [db-spec]
  (sql/db-do-commands db-spec
    (ddl/create-table
     :tournaments
     [:id "INTEGER PRIMARY KEY AUTO_INCREMENT"]
     [:date :timestamp]
     [:name "varchar"]
     [:registration_open "boolean"])))

(defn create-events-table
  [db-spec]
  (sql/db-do-commands db-spec
    (ddl/create-table
     :events
     [:id "INTEGER PRIMARY KEY AUTO_INCREMENT"]
     [:tournament "integer"]
     [:name "varchar"]
     [:team_mode "boolean"])))

(defn create-registrations-table
  [db-spec]
  (sql/db-do-commands db-spec
    (ddl/create-table
     :registrations
     [:id "INTEGER PRIMARY KEY AUTO_INCREMENT"]
     [:event "integer"]
     [:user "integer"]
     [:team "varchar"]
     [:created :timestamp])
    (ddl/create-index :uniquereg :registrations
                      [:event :user] :unique)))

(def tables
  {:articles create-articles-table
   :users create-users-table})
