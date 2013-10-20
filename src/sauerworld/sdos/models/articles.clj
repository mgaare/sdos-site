(ns sauerworld.sdos.models.articles
  (:require [korma.core :as k]
            [clj-time.coerce :refer (to-date)]
            [clj-time.core :refer (now)]
            [sauerworld.sdos.utils :refer :all]))

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
        (k/insert (k/values article)))))

(defn update-article
  [db {:keys [id date title author content category]}]
  (when id
    (let [fields (-> {}
                     (assoc-if date "DATE" date)
                     (assoc-if title "TITLE" title)
                     (assoc-if author "AUTHOR" author)
                     (assoc-if content "CONTENT" content)
                     (assoc-if category "CATEGORY" category))]
      (-> (base-articles-query db)
          (k/update
           (k/set-fields
            fields)
           (k/where {:id id}))))))

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
