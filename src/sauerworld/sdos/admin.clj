(ns sauerworld.sdos.admin
  (:require [sauerworld.sdos.settings :refer :all]
            [sauerworld.sdos.models.articles :as articles]
            [sauerworld.sdos.models.users :as users]
            [sauerworld.sdos.views.admin :as view]
            [sauerworld.sdos.layout :as layout]))

(defn wrap-require-admin
  [h]
  (fn [req]
    (if (true? (-> req :session :user :admin))
      (h req)
      redirect-home)))

(defn show-articles-summary
  [req]
  (let [db (:db req)
        articles (-> ((:storage-api req) :articles/find-all-articles)
                     (deref 1000 nil))
        content (view/articles-summary articles)]
    (layout/app-page (get-settings req) content)))

(defn create-article-page
  [req]
  (let [content (view/article-new)]
    (layout/app-page (get-settings req) content)))

(defn do-create-article
  [req]
  (let [form (:params req)
        article {:title (:title form)
                 :author (:author form)
                 :category (:category form)
                 :content (:content form)}
        save (->
              ((:storage-api req) :articles/insert-article article)
              (deref 1000 nil))
        content (if save
                  "Article inserted successfully."
                  "Article failed to insert properly.")]
    (layout/app-page (get-settings req) content)))

(defn edit-article-page
  [req]
  (let [id (-> req :params :id)
        article (->
                 ((:storage-api req) :articles/find-article id)
                 (deref 1000 nil))
        content (view/article article true)]
    (layout/app-page (get-settings req) content)))

(defn do-edit-article
  [req]
  (let [form (:params req)
        article {:id (:id form)
                 :date (:date form)
                 :title (:title form)
                 :author (:author form)
                 :category (:category form)
                 :content (:content form)}
        save (-> ((:storage-api req) :articles/update-article article)
                 (deref 1000 nil))
        content (if save
                  "Article edited successfully."
                  "Article failed to edit properly.")]
    (layout/app-page (get-settings req) content)))

(defn do-delete-article
 [req]
 "do delete article")

(defn show-users
  [req]
  "show users")

(defn add-user-page
  [req]
  "add user page")

(defn do-add-user
  [req]
  "do add user")

(defn show-user
  [req]
  "show user")

(defn do-edit-user
  [req]
  "do edit user")

(defn do-delete-user
  [req]
  "do delete user")

(defn show-tournaments
  [req]
  "show tournaments")

(defn add-tournament-page
  [req]
  "add tournament page")

(defn do-add-tournament
  [req]
  "do add tournament")

(defn show-tournament
  [req]
  "show tournament")

(defn do-edit-tournament
  [req]
  "do edit tournament")
