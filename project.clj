(defproject savagematt/bowen "2.1"
  :description "Implements the decorator pattern for clojure protocols."

  :url "http://github.com/savagematt/bowen"

  :license {:name "Eclipse Public License"
            :url "http://www.eclipse.org/legal/epl-v10.html"}

  :dependencies [[org.clojure/clojure "1.5.1"]]

  :profiles {:dev     {:dependencies [[midje "1.6.3"]]
                       :plugins      [[lein-midje "3.1.3"]]}})
