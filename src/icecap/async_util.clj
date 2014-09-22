(ns icecap.async-util
  (:require [clojure.core.async :as a]))

(defmacro ^:private throwing-op
  "Like `op`, except throws exceptions from the chan."
  [op c]
  `(let [v# (~op ~c)]
     (if (instance? Throwable v#)
       (throw v#)
       v#)))

(defmacro <!!?
  "Like `<!!`, except raises exceptions from the chan."
  [c]
  `(throwing-op a/<!! ~c))

(defmacro <!?
  "Like `<!`, except raises exceptions from the chan."
  [c]
  `(throwing-op a/<! ~c))

(defmacro go-catch
  "Like `go`, except catches exceptions and places them on the chan."
  [& body]
  `(a/go (try ~@body
              (catch Throwable e#
                e#))))
