(ns sauerworld.sdos.views.tournament
  (:require [net.cgrand.enlive-html :as html]
            [sauerworld.sdos.layout :as layout]))

(html/defsnippet signups "templates/tournament.html"
  [:div#signups]
  [{signups :signups}]

  [:div.mode]
  (html/clone-for [signup signups]
             [:h3] identity
                         )
  )
