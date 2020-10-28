(def shared-compiler-settings '{:main togglmetrics.core
                                :npm-deps {:express "4.16.2"
                                           :path "0.12.7"
                                           :ws "3.2.0"
                                           :toggl-api "1.0.1"
                                           :jade "~1.11.0"
                                           }
                                :install-deps true
                                :infer-externs true
                                :target :nodejs})

(defproject togglmetrics "0.1.0-SNAPSHOT"
  :description ""
  :license {:name "Mozilla Public License 2.0"
            :url "https://www.mozilla.org/en-US/MPL/2.0/"}
  :dependencies [[org.clojure/clojure "1.9.0"]
                 [org.clojure/core.async "0.3.443"]
                 [org.clojure/clojurescript "1.9.946" :scope "provided"]]

  :plugins [[lein-cljsbuild "1.1.7"]]

  :min-lein-version "2.6.1"

  :clean-targets ^{:protect false} [:target-path :compile-path "dev" "out"]

  :cljsbuild {:builds
              [{:id "dev"
                :source-paths ["src"]

                :figwheel {:on-jsload "togglmetrics.core/restart!"}

                :compiler ~(merge
                            {:output-to "dev/togglmetrics.js"
                             :output-dir "dev"}
                            shared-compiler-settings)}

               {:id "prod"
                :source-paths ["src"]
                :compiler ~(merge
                            {:output-to "togglmetrics.js"
                             :output-dir "out"
                             :optimizations :simple
                             :closure-defines '{goog.DEBUG false}
                             :pretty-print false}
                            shared-compiler-settings)}]}

  :figwheel {:server-logfile "log/figwheel.log"
             :server-port 3450}

  :profiles {:dev
             {:dependencies [[figwheel "0.5.16-SNAPSHOT"]
                             [figwheel-sidecar "0.5.16-SNAPSHOT"]
                             [com.cemerick/piggieback "0.2.2"]]

              :plugins [[lein-figwheel "0.5.16-SNAPSHOT"]]

              :repl-options {:nrepl-middleware [cemerick.piggieback/wrap-cljs-repl]}}})
