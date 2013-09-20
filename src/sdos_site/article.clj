(ns sdos-site.article
  (:require [korma.db :as db]
            [korma.core :as k]
            [clojure.java.jdbc :as sql]
            [clojure.string :as str]))

(defn create-db
  [resource-loc]
  (db/create-db (db/h2 {:db resource-loc
                        :naming {:keys str/lower-case
                                 :fields str/upper-case}})))

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

(defn base-query
  [db]
  (-> (k/create-entity "articles")
      (k/database db)))

(defn find-all-articles
  [db]
  (-> (base-query db)
      (k/select (k/order :id :DESC))))

(defn find-article
  [db id]
  (-> (base-query db)
      (k/select (k/where {:id id}))
      (first)))

(defn find-category-articles
  [db cat]
  (-> (base-query db)
      (k/select (k/where {:category cat})
                (k/order :id :DESC))))
