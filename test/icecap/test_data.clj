(ns icecap.test-data)

(def simple-step {:type :succeed})
(def simple-http-step {:type :http :url "http://example.test"})
(def simple-https-step {:type :http :url "https://example.test"})
(def simple-ftp-step {:type :ftp :loc "ftp://example.test"})
