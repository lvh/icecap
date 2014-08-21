(ns icecap.execute
  "The meat and potatoes of executing (also called exercising) a capability."
  (:refer-clojure :exclude [merge])
  (:require [clojure.core.async :as a :refer [go chan <! >! merge pipe put!]])
  (:import [java.net URI]))

(defn ^:private http-handler
  "A request handler for HTTP and HTTPS requests."
  [request]
  request)

(def ^:private handlers
  "The default set of handlers."
  {"http" http-handler
   "https" http-handler})

(def ^:private supported-schemes
  "The currently supported schemes."
  (hash-set (keys handlers)))

(defn get-scheme
  "Gets the scheme of a URL."
  [url]
  (.getScheme (URI. url)))

(def supported-scheme?
  "Does the given URL have a supported scheme?"
  (comp supported-schemes get-scheme))

(defn execute-single-request
  "Execute a single request map.

  Returns a channel that will eventually produce the result.
  "
  [request]
  ;; let's pretend this does something useful
  (a/to-chan [request]))

(defn execute
  "Executes a request specification.

  If the request specification is a single request, executes it with
  `execute-single-request`. If it is an unordered collection of
  requests, execute all of them in any order. If it is an ordered
  collection of requests, executes them in order. For more details on
  the structure of request specs, see `icecap.schema`.

  Returns a channel that will produce all of the individual results,
  and then closes.
  "
  [request-spec]
  (let [c (chan)]
    (condp #(%1 %2) request-spec
      map? (pipe (execute-single-request request-spec) c)
      set? (pipe (merge (map execute request-spec)) c)
      ;;                       ^- recursion!!!
      vector? (go (loop [request-specs request-spec]
                    (>! c (<! (execute (first request-specs))))
                    ;;           ^- recursion!!!
                    (let [remaining (rest request-specs)]
                      (if (seq remaining)
                        (recur remaining)
                        (a/close! c))))))
    c))
