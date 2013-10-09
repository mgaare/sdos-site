(ns sdos-site.layout
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

(html/defsnippet main-menu "templates/snippets.html"
  [:.main-menu :> html/any-node]
  [{:keys [menu-items menu-title]}]

  [:ul [:li html/first-of-type]]
  (html/clone-for [[text url] menu-items]
                  [:li :a] (html/content text)
                  [:li :a] (html/set-attr :href url))

  [:h5] (when menu-title (html/content menu-title)))

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
  [{:keys [articles] :as content}]

  [:#side-menu] (html/content (main-menu content))

  [:#email-signup] (html/content (email-signup content))

  [:#content-main] (html/content (map article articles))

  [:.bottom-menu] (html/content (bottom-menu content)))

(html/deftemplate error-template "templates/layout.html"
  [{:keys [error] :as content}]

  [:#side-menu] (html/content (main-menu content))

  [:#content-main] (html/html-content content)

  [:.bottom-menu] (html/content (bottom-menu content)))

(defn wrap-error-template
  [settings]
  (fn [error]
    (error-template (merge settings {:error error}))))

(html/deftemplate app-page "templates/layout.html"
  [settings content]

  [:#side-menu] (html/content (main-menu content))

  [:.bottom-menu] (html/content (bottom-menu content))

  [:#content-main] (html/content content))
