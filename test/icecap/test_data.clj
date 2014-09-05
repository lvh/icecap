(ns icecap.test-data)

(defn success-step [name] {:type :succeed :name name})
(def simple-http-step {:type :http :url "http://example.test"})
(def simple-https-step {:type :http :url "https://example.test"})
(def simple-ftp-step {:type :ftp :loc "ftp://example.test"})
