(ns sdos-site.layout
    (:require [net.cgrand.enlive-html :as html]
              [net.cgrand.reload :refer (auto-reload)]
              [clojure.string :as str]))

(auto-reload *ns*)

(defn article-author
  [author date]
  (cond
   (and author date) (str "By " author " on " date ".")
   author (str "By " author ".")
   date (str "Posted " date ".")
   :else nil))

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
  [{:keys [title link author date content]}]

  [:h3] (if link
          (html/content (html/html [:a {:href link} title]))
          (html/content title))

  [:h6] (html/content (article-author author date))

  [:div.content] (html/html-content content))

(html/deftemplate main-template "templates/layout.html"
  [{:keys [articles] :as content}]

  [:#side-menu] (html/content (main-menu content))

  [:#email-signup] (html/content (email-signup content))

  [:#content-main] (html/content (map article articles))

  [:.bottom-menu] (html/content (bottom-menu content)))
