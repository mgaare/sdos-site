(ns sdos-site.core
  (:require [sdos-site.article :refer :all]
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

(def links
  [])

(def base-url
  "http://dos.sauerworld.org")

(def home-articles
  [{:id 10
    :title "Announcing Sauerbraten Day of Sobriety, a New Tournament"
    :author "mefisto"
    :date (date-time 2013 9 17 20 30)
    :content "
Feeling down because there's no Sauerbraten tournament for you to play
in? Singing the blues because a certain other event that shared the same
initials as our tournament put a stop to Sauerbraten tournament fun?

Well dry your tears because there's a new tournament in town! Prepare for
a day of very serious Sauerbraten competition. Prepare for... a
**Day of Sobriety**.

![Day of Sobriety Drunken Sadness](/img/coming-soon.jpg)

## Coming Soon!

We are still putting together all the plans and schemes and empty liquor
bottles (well, they're still full for now)... but for now we want to know
who is interested in playing. If you could please
**sign up with your email address in that form over to the left**
we will be able to keep in touch with updates!

### So just who are you people?

This tournament is being run by echo-echo, mefisto, misc with awesome special
assistance from notas, bbk, and others! (Special thanks to bbk for the great
logos and images)

### Why are you doing this? WHY?

Everyone involved in the Sauerbraten DoS (the Day of Sobriety DoS, not the
other one with those other people that we will not speak of anymore until
the next chance to bring it up) believes that Sauerbraten needs some good
competition! We've all been around this community for years, and we sincerely
want everyone to have a good time and see Sauerbraten succeed.

### Why should we trust you guys, huh, huh?

We want to address your concerns. Please ask echo-echo any questions and he
will be able to make you feel better. Also, he is an engineer, he knows about
electric circuits, and you have delicate body parts. Just saying.

### When is this damn thing gonna be?

Soon, ok? We got a few things to line up, gotta talk to some people, got some
splines to reticulate, you know?

Seriously, we hope to have an announcement of the time of the first event real
soon now.

### Do you need some help, eh? How can I contact you?

Yes! If you want to help out, you can:
* Send an email to mefisto. The address is his nick at sauerworld.org
* Come see us on IRC. The channel #sdos on the Gamesurge network

### Did I hear that there might be cash prizes???

As a matter of fact, we are currently discussing the possibility of offering
cash prizes for winners. Stay tuned!"}])

(def layout-settings
  {:email-title "Interested?"
   :email-subtitle "Sign up for email updates!"})

(defn home-page
  [req]
  (let [content-params
        (merge layout-settings
               {:articles home-articles})]
    (main-template content-params)))

(defn page
  [category]
  (fn [req]
    (let [db (:db req)
          content-params
          (merge layout-settings
                 {:articles (find-category-articles db category)})]
      (main-template content-params))))

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
        articles (art/find-all-articles db)])
  (apply rss/channel-xml
         {:title "Sauerbraten Day of Sobriety"
          :link base-url
          :description "A Sauerbraten Tournament"}
         (map rss-item articles)))

(defn show-article
  [req]
  (let [id (some->
            (get-in req [:route-params :id])
            (Integer/parseInt))
        db (:db req)]
    (if-let [article (find-article db id)]
      (let [content-params
            (merge layout-settings
                   {:articles [article]})]
        (main-template content-params))
      {:status 404 :headers {} :body "Sorry, article not found."})))

(defroutes app-routes
  (GET "/" [] (page "home"))
  (GET "/article/:id" [] show-article)
  (GET "/rss" [] rss)
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
  (let [db (create-db "resources/db/articles")]
    (do
      (web/start (wrap-db app db))
      (swap! world assoc :db db))))
