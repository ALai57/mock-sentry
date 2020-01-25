(ns mock-sentry.core
  (:gen-class)
  (:require [mount.core :as mount]
            [compojure.core :refer :all]
            [cheshire.core :as json]
            [org.httpkit.server :as server]
            [taoensso.timbre :as timbre]))

(defroutes handler
  (GET "/" [] {:status 200 :body "Hello"})
  (context "/api/v1" {:keys [remote-addr uri body server-name] :as req}
    (POST "/log" []
      (when body
        (timbre/infof "[%s] [%s] %s"
                      remote-addr
                      uri
                      (-> body
                          slurp
                          (json/parse-string keyword))))
      {:status 200 :body "Success"})))

(mount/defstate server
  :start
  (do (println "Starting server...")
      (server/run-server handler {:port 5000}))

  :stop
  (do (println "Stopping server")
      (server)))

(defn -main [& args]
  (mount/start))
