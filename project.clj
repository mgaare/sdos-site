(defproject sdos-site "0.1.0-SNAPSHOT"
  :description "A website for the Sauerbraten Day of Sobriety"
  :url "dos.sauerworld.org"
  :license {:name "Eclipse Public License"
            :url "http://www.eclipse.org/legal/epl-v10.html"}
  :profiles {:dev
             {:source-paths ["dev"]
              :dependencies [[org.clojars.jcrossley3/tools.namespace "0.2.4.1"]
                             [org.immutant/immutant "1.0.1"]]}}
  :dependencies [[org.clojure/clojure "1.5.1"]
                 [clj-time "0.6.0"]
                 [clj-rss "0.1.3"]
                 [compojure "1.1.5"]
                 [enlive "1.1.4"]
                 [markdown-clj "0.9.31"]]
  :immutant {:init "sdos-site.core/start"
             :context-path "/"})
