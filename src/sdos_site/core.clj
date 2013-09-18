(ns sdos-site.core
  (:require [compojure.core :refer :all]
            [compojure.route :refer (not-found) :as route]
            [immutant.web :refer (wrap-resource) :as web]
            [compojure.handler :refer (site)]
            [sdos-site.layout :refer (main-template)]
            [markdown.core :refer (md-to-html-string)]))

(def links
  [])

(def home-articles
  [{:title "Announcing Sauerbraten Day of Sobriety, a New Tournament"
    :link nil
    :author "mefisto"
    :date "September 17, 2013"
    :content (md-to-html-string
              "
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
who is interested in playing. If you could please **sign up with your
email address in that form over to the left** we will be able to keep in
touch with updates!

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

We are in favor of all that is good, and opposed to all that is bad.

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

Yes. There might be... stay tuned for a final answer.")}])


(defn home-page
  [req]
  (let [content-params
        {:email-title "Interested?"
         :email-subtitle "Sign up for email updates!"
         :articles home-articles}]
    (main-template content-params)))

(defroutes app-routes
  (GET "/" [] home-page)
  (not-found "Sorry buddy, page not found!"))

(def app (wrap-resource (site app-routes) "public"))

(defn start [] (web/start app))
