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
            [korma.core :as k]
            [korma.db :as kdb]
            [sdos-site.core :as core]
            [sdos-site.db :as db]
            [sdos-site.layout :as layout]
            [clojure.tools.namespace.repl :refer (refresh refresh-all)]
            [net.cgrand.enlive-html :as html]
            [ring.mock.request :as mr]))

(def db-path
  "resources/db/main")

(defn test-snippet
  [snippet & args]
  (->> (apply snippet args)
      (html/emit*)
      (apply str)))
