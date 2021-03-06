(ns sauerworld.sdos.core
  (:require [sauerworld.sdos.settings :refer :all]
            [sauerworld.sdos.db :refer (create-db)]
            [sauerworld.sdos.page :as page]
            [sauerworld.sdos.tournament :as t]
            [sauerworld.sdos.user :as user]
            [sauerworld.sdos.admin :as admin]
            [sauerworld.sdos.rss :refer (rss)]
            [environ.core :refer (env)]
            [compojure.core :refer :all]
            [compojure.route :refer (not-found) :as route]
            [immutant.web :refer (wrap-resource) :as web]
            [immutant.util :refer (at-exit)]
            [immutant.messaging :as msg]
            [compojure.handler :refer (site)]))

(def world (atom {}))

(defn test-request
  [req]
  (str req))

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
  (GET "/" [] (page/page "home"))
  (GET "/about" [] (page/page "about"))
  (GET "/events" [] (page/page "events"))
  (GET "/article/:id" [] page/show-article)
  (GET "/rss" [] rss)
  (GET "/tournaments/:id" []  t/show-tournament)
  (GET "/test" [] test-request)
  (context "/admin" [] (admin/wrap-require-admin admin-routes))
  (context "/user" [] user-routes)
  (not-found "Sorry buddy, page not found!"))

(defn wrap-storage-api
  [handler api-queue]
  (fn [req]
    (assoc req :storage-api
           (fn [action & params]
             (msg/request api-queue {:action action
                                     :params params})))))

(defn wrap-smtp-server
  "Adds smtp server params into the request map."
  [handler smtp-server]
  (fn [req]
    (-> req
        (assoc :smtp-server smtp-server)
        handler)))

(def app (-> app-routes
             site
             (wrap-resource "public")))

(defn stop []
  nil)

(defn start []
  (let [smtp-host (:mg-smtp-host env)
        smtp-login (:mg-smtp-login env)
        smtp-password (:mg-smtp-password env)
        smtp-params {:host smtp-host
                     :user smtp-login
                     :pass smtp-password
                     :tls true
                     :port 587}
        smtp-wrap-fn (if (and smtp-host smtp-login smtp-password)
                       (fn [h]
                         (wrap-smtp-server h smtp-params))
                       identity)
        api-queue-name "queue/storage"
        api-handle (msg/start api-queue-name)
        ]
    (do
      (-> app
          (wrap-storage-api api-queue-name)
          smtp-wrap-fn
          web/start)
      (swap! world assoc :api-handle api-handle))))

(defn initialize []
  (start))
