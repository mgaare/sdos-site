(ns sauerworld.sdos.page
  (:require [sauerworld.sdos.settings :refer (get-settings)]
            [sauerworld.sdos.models.articles :refer (find-category-articles
                                               find-article)]
            [sauerworld.sdos.layout :refer (main-template)]))

(defn page
  [category]
  (fn [req]
    (let [articles (-> ((:storage-api req) :find-category-articles category)
                       (deref 1000 nil))
          settings (get-settings req)]
      (main-template settings articles))))

(defn show-article
  [req]
  (let [id (some->
            (get-in req [:route-params :id])
            (Integer/parseInt))]
    (if-let [article (->
                      ((:storage-api req) :find-article id)
                      (deref 1000 nil))]
      (let [settings (get-settings req)]
        (main-template settings [article]))
      {:status 404 :headers {} :body "Sorry, article not found."})))
