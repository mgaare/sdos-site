(ns sdos-site.views.admin
  (:require [net.cgrand.enlive-html :as html]
            [sdos-site.layout :as layout]))

(html/defsnippet articles-summary "templates/admin.html"
  [:div#admin-articles-summary]
  [articles]

  [:tbody :tr]
  (html/clone-for [article articles]

             [[:td (html/nth-of-type 1)]]
             (html/content
              (html/html [:a
                          {:href (str "/admin/articles/" (:id article))}
                          (str (:id article))]))

             [[:td (html/nth-of-type 2)]]
             (html/content (layout/format-date (:date article)))

             [[:td (html/nth-of-type 3)]]
             (html/content (:title article))

             [[:td (html/nth-of-type 4)]]
             (html/content (:author article))

             [[:td (html/nth-of-type 5)]]
             (html/content (:category article))))

(html/defsnippet delete-button "templates/admin.html"
  [:div#delete-button]
  [action]

  [:form] (html/set-attr :action action))

(html/defsnippet article "templates/admin.html"
  [:div#article-page]
  [article edit?]

  [:form] (if (:id article)
            (html/set-attr :action (str "/admin/articles/" (:id article)))
            (html/set-attr :action "/admin/articles"))

  [:#title] (if (:title article)
              (html/set-attr :value (:title article))
              identity)

  [:#author] (if (:author article)
               (html/set-attr :value (:title article))
               identity)

  [:#category] (if (:category article)
                 (html/set-attr :value (:category article))
                 identity)

  [:#content] (if (:content article)
                (html/content (:content article))
                identity)

  [:form] (if (and edit? (:id article))
            (html/after
             (delete-button
              (str "/admin/articles/" (:id article) "/delete")))
            identity))

(defn article-new
  []
  (article nil false))
