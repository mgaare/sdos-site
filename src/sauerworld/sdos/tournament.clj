(ns sauerworld.sdos.tournament
  (:require [sauerworld.sdos.models.tournaments
             :refer (get-tournament-by-id get-tournament-signups)]
            [sauerworld.sdos.layout :refer (error-template app-page)]
            [sauerworld.sdos.settings :refer (get-settings)]
            [sauerworld.sdos.views.tournament :as view]))

(defn show-tournament
  [req]
  (let [id (-> req :query-params :id)
        db (:db req)
        settings (get-settings req)]
    (if-let [tourney (get-tournament-by-id db id)]
      (let [signups (get-tournament-signups db id)]

        (error-template settings
                        (str  "Tournament " id " not found."))))))
