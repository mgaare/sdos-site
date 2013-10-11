(ns sdos-site.core
  (:require [sdos-site.settings :refer :all]
            [sdos-site.db :refer :all]
            [sdos-site.user :as user]
            [sdos-site.admin :as admin]
            [compojure.core :refer :all]
            [compojure.route :refer (not-found) :as route]
            [clj-time.core :refer (date-time)]
            [clj-time.format :as tf]
            [clj-time.coerce :refer (to-date)]
            [clj-rss.core :as rss]
            [immutant.web :refer (wrap-resource) :as web]
            [compojure.handler :refer (site)]
            [sdos-site.layout :refer (main-template)]))

(def world (atom {}))

(defn page
  [category]
  (fn [req]
    (let [db (:db req)
          articles (find-category-articles db category)
          settings (get-settings req)]
      (main-template settings articles))))

(defn rss-item
  [{:keys [id title author date content]}]
  (let [link (str base-url "/article/" id)]
    {:guid link
     :title title
     :description content
     :link link
     :pubDate (to-date date)}))

(defn rss
  [req]
  (let [db (:db req)
        articles (find-category-articles db "home")]
    (apply rss/channel-xml
           {:title "Sauerbraten Day of Sobriety"
            :link base-url
            :description "A Sauerbraten Tournament"}
           (map rss-item articles))))

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

(defroutes admin-routes
  (GET "/" [] admin/show-articles-summary)
  (GET "/articles/create" [] admin/create-article-page)
  (POST "/articles" [] admin/do-create-article)
  (GET "/articles/:id" [] admin/edit-article-page)
  (POST "/articles/:id" [] admin/do-edit-article)
  (POST "/articles/:id/delete" [] admin/do-delete-article)
  (GET "/users" [] admin/show-users)
  (GET "/users/new" [] admin/add-user-page)
  (POST "/users" [] admin/do-add-user)
  (GET "/users/:id" [] admin/show-user)
  (POST "/users/:id" [] admin/do-edit-user)
  (POST "/users/:id/delete" [] admin/do-delete-user)
  (GET "/tournaments" [] admin/show-tournaments)
  (GET "/tournaments/new" [] admin/add-tournament-page)
  (POST "/tournaments" [] admin/do-add-tournament)
  (GET "/tournaments/:id" [] admin/show-tournament)
  (POST "/tournaments/:id" [] admin/do-edit-tournament))

(defroutes user-routes
  (GET "/" [] user/profile-page)
  (GET "/login" [] user/login-page)
  (POST "/login" [] user/do-login)
  (GET "/logout" [] user/do-logout)
  (GET "/register" [] user/registration-page)
  (POST "/register" [] user/do-registration)
  (GET "/password" [] user/password-page)
  (POST "/password" [] user/do-password)
  (GET "/validate/resend" [] user/resend-validation)
  (GET "/validate/:validation-key" [] user/validate-email)
  (GET "/authkey" [] user/authkey-page)
  (POST "/authkey" [] user/do-authkey)
  (GET "/signup" [] user/signup-page)
  (POST "/signup" [] user/do-signup)
  (GET "/signup/:id" [] user/show-signup)
  (POST "/signup/:id" [] user/do-edit-signup))

(defroutes app-routes
  (GET "/" [] (page "home"))
  (GET "/about" [] (page "about"))
  (GET "/events" [] (page "events"))
  (GET "/article/:id" [] show-article)
  (GET "/rss" [] rss)
  (context "/admin" [] (admin/wrap-require-admin admin-routes))
  (context "/user" [] user-routes)
  (not-found "Sorry buddy, page not found!"))

(defn wrap-db
  [handler db-con]
  (fn [req]
    (-> req
        (assoc :db db-con)
        handler)))

(defn wrap-db-missing
  [handler]
  (fn [req]
    (if (:db req)
      (handler req)
      {:status 500
       :headers {}
       :body "Unrecoverable Database error. Specifically, the database is missing."})))

(def app (-> app-routes
             site
             wrap-db-missing
             (wrap-resource "public")))

(defn start []
  (let [db (create-db "resources/db/main")]
    (do
      (web/start (wrap-db app db))
      (swap! world assoc :db db))))
