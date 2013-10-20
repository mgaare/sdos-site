(ns user
  (:require [clojure.java.io :as io]
            [clojure.string :as str]
            [clojure.set :as set]
            [clojure.pprint :refer (pprint)]
            [clojure.test :as test]
            [clj-time.core :refer (date-time)]
            [clj-time.format :as tf]
            [clj-time.coerce :as tc]
            [compojure.handler :refer (site)]
            [compojure.response :refer (render)]
            [postal.core :as mail]
            [environ.core :refer (env)]
            [korma.core :as k]
            [korma.db :as kdb]
            [sauerworld.sdos.core :as core]
            [sauerworld.sdos.db :as db]
            [sauerworld.sdos.layout :as layout]
            [clojure.tools.namespace.repl :refer (refresh refresh-all)]
            [net.cgrand.enlive-html :as html]
            [ring.mock.request :as mr]))

;; mailgun smtp with ssl is port 587 - like amazon apparently


(def db-path
  "resources/db/main")

(defn test-snippet
  [snippet & args]
  (->> (apply snippet args)
      (html/emit*)
      (apply str)))
