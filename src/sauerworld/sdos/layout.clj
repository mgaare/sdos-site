(ns sauerworld.sdos.layout
    (:require [net.cgrand.enlive-html :as html]
              [net.cgrand.reload :refer (auto-reload)]
              [clj-time.format :refer (unparse formatter)]
              [clj-time.coerce :refer (from-date)]
              [clojure.string :as str]
              [markdown.core :refer (md-to-html-string)]))

(auto-reload *ns*)

(def base-url
  "http://dos.sauerworld.org")

(def verbose-date-format
  (formatter "MMMM dd, yyyy"))

(defn format-date
  [date]
  (some->> date
           from-date
           (unparse verbose-date-format)))

(defn article-author
  [author date]
  (let [date (format-date date)]
    (cond
     (and author date) (str "By " author " on " date ".")
     author (str "By " author ".")
     date (str "Posted " date ".")
     :else nil)))

(html/defsnippet menu "templates/snippets.html"
  [:.links-menu :> html/any-node]
  [items & [title]]

  [:ul [:li html/first-of-type]]
  (html/clone-for [[text url] items]
                  [:li :a] (html/content text)
                  [:li :a] (html/set-attr :href url))

  [:h5] (when title (html/content title)))

(defn main-menu
  [settings]
  (menu (:menu-items settings) (:menu-title settings)))

(defn user-menu
  [settings]
  (when (:logged-in settings)
    (menu (:user-menu-items settings) (:user-menu-title settings))))

(defn admin-menu
  [settings]
  (when (:admin settings)
    (menu (:admin-menu-items settings) (:admin-menu-title settings))))

(html/defsnippet bottom-menu "templates/snippets.html"
  [:div.bottom-menu :> html/any-node]
  [{:keys [menu-items]}]

  [:ul [:li html/first-of-type]]
  (html/clone-for [[text url] menu-items]
                  [:li :a] (html/content text)
                  [:li :a] (html/set-attr :href url)))

(html/defsnippet email-signup "templates/snippets.html"
  [:#email-signup :> html/any-node]
  [{:keys [email-title email-subtitle]}]

  [:h4] (some-> email-title (html/content))
  [:p] (some-> email-subtitle (html/content)))

(html/defsnippet article "templates/snippets.html"
  [:article]
  [{:keys [id title author date content]}]

  [:h2 :a] (let [link (str base-url "/article/" id)]
             (html/do->
              (html/set-attr :href link)
              (html/content title)))

  [:h6] (html/content (article-author author date))

  [:div.content] (html/html-content (md-to-html-string content)))

(html/deftemplate main-template "templates/layout.html"
  [settings articles]

  [:#side-menu] (html/content (main-menu settings))

  [:#user-menu] (html/content (user-menu settings))

  [:#admin-menu] (html/content (admin-menu settings))

  [:#email-signup] (html/content (email-signup settings))

  [:#content-main] (html/content (map article articles))

  [:.bottom-menu] (html/content (bottom-menu settings)))

(html/deftemplate error-template "templates/layout.html"
  [settings error]

  [:#side-menu] (html/content (main-menu settings))

  [:#user-menu] (html/content (user-menu settings))

  [:#admin-menu] (html/content (admin-menu settings))

  [:#content-main] (html/html-content error)

  [:.bottom-menu] (html/content (bottom-menu settings)))

(defn wrap-error-template
  [settings]
  (fn [error]
    (error-template settings error)))

(html/deftemplate app-page "templates/layout.html"
  [settings content]

  [:#side-menu] (html/content (main-menu settings))

  [:#user-menu] (html/content (user-menu settings))

  [:#admin-menu] (html/content (admin-menu settings))

  [:.bottom-menu] (html/content (bottom-menu settings))

  [:#content-main] (html/content content))
