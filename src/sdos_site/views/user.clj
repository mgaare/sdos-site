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

(html/defsnippet login-page "templates/user.html"
  [:div#user-login]
  [& [error]]

  [:form] (html/set-attr :action "/user/login")

  [:p.error] (when error
               (html/content error)))

(html/defsnippet registration-page "templates/user.html"
  [:div#user-form]
  [& [{:keys [error username password password-confirm email]}]]

  [:p.error] (when error
               (if (coll? error)
                 (html/clone-for [e error]
                            (html/content e))
                 (html/content error)))

  [:#username] (if username
                 (html/set-attr :value username)
                 identity)

  [:#password] (if password
                 (html/set-attr :value password)
                 identity)

  [:#password-confirm] (if password-confirm
                         (html/set-attr :value password-confirm)
                         identity)

  [:#email] (if email
              (html/set-attr :value email)
              identity))

(html/defsnippet registration-thanks "templates/user.html"
  [:#registration-thanks]
  [])

(html/defsnippet password-page "templates/user.html"
  [:div#user-form]
  [& [{:keys [error username password password-confirm email]}]]

  [:p.error] (when error
               (if (coll? error)
                 (html/clone-for [e error]
                            (html/content e))
                 (html/content error)))

  [:#username] nil

  [:#password] (if password
                 (html/set-attr :value password)
                 identity)

  [:#password-confirm] (if password-confirm
                         (html/set-attr :value password-confirm)
                         identity)

  [:#email] nil

  )

(html/defsnippet success-page "templates/user.html"
  [:div#success]
  [& [message]]

  [:p] (if message
         (html/content message)
         identity))
