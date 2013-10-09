(ns sdos-site.views.user
  (:require [net.cgrand.enlive-html :as html]
            [sdos-site.layout :as layout]))


(html/defsnippet user-profile "templates/user.html"
  [:div#user-profile]
  [{:keys [username email validated timestamp created]}]

  [:dt] (html/clone-for [[title text]
                         [["Username" username]
                          ["Password" "********"]
                          ["Email" email]
                          ["Email Validated" (str validated)]
                          ["Registration Date" (layout/verbose-date-format created)]]]
                        [:dt] (html/content title)
                        [:dt] (html/content text)))
