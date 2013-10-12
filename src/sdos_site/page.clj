(ns sdos-site.page
  (:require [sdos-site.settings :refer (get-settings)]
            [sdos-site.models.articles :refer (find-category-articles
                                               find-article)]
            [sdos-site.layout :refer (main-template)]))

(defn page
  [category]
  (fn [req]
    (let [db (:db req)
          articles (find-category-articles db category)
          settings (get-settings req)]
      (main-template settings articles))))

(defn show-article
  [req]
  (let [id (some->
            (get-in req [:route-params :id])
            (Integer/parseInt))
        db (:db req)]
    (if-let [article (find-article db id)]
      (let [settings (get-settings req)]
        (main-template settings [article]))
      {:status 404 :headers {} :body "Sorry, article not found."})))
