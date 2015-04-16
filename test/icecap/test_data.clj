(ns icecap.test-data)

(defn success-step
  [name]
  {:type :succeed
   :name name})

(def simple-http-step
  {:type :http
   :method :GET
   :url "http://example.test"})

(def simple-https-step
  {:type :http
   :method :GET
   :url "https://example.test"})

(def simple-ftp-step
  {:type :ftp
   :loc "ftp://example.test"})
