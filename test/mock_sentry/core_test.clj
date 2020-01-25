(ns mock-sentry.core-test
  (:require [clojure.test :refer :all]
            [mock-sentry.core :refer :all]
            [ring.mock.request :as mock]
            [taoensso.timbre :as timbre]))

(deftest server-test
  (testing "Basic ping"
    (is (= {:status 200 :body "Hello" :headers {}}
           (handler (mock/request :get "/")))))
  (testing "Logging endpoint"
    (is (= {:status 200 :body 1 :headers {}}
           (handler (mock/request :post "/api/v1/log"))))))

(defmacro with-captured-logging
  "Captures logs and binds them to symbol `sym` so
  you can assert against them later"
  [sym & body]
  `(let [~sym (atom [])]
     (timbre/with-merged-config
       {:appenders {:println {:fn #(swap! ~sym
                                          conj
                                          (force (:msg_ %)))}}}
       ~@body)))

(deftest log-test-2
  (testing "Logging actually logs"
    (with-captured-logging captured-logs
      (handler (-> (mock/request :post "/api/v1/log")
                   (mock/json-body {:level :fatal
                                    :message "Terrible!"})))
      (println @captured-logs)
      (is (= "[127.0.0.1] [/api/v1/log] {:level \"fatal\", :message \"Terrible!\"}"
             (first @captured-logs))) )))
