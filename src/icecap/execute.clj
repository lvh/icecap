(ns icecap.execute
  "Tools for executing plans, doing the actual work expressed by a
  capability."
  (:refer-clojure :exclude [merge])
  (:require [clojure.core.async :as a :refer [go chan <! >! merge pipe put!]])
  (:import [java.net URI]))

(defn ^:private http-handler
  "An step handler that performs HTTP and HTTPS requests."
  [step]
  step)

(def ^:private handlers
  "The default set of handlers."
  {"http" http-handler
   "https" http-handler})

(def ^:private supported-schemes
  "The currently supported schemes."
  (into #{} (keys handlers)))

(defn get-scheme
  "Gets the scheme of a URL."
  [url]
  (.getScheme (URI. url)))

(def supported-scheme?
  "Does the given URL have a supported scheme?"
  (comp supported-schemes get-scheme))

(defn ^:private execute-step
  "Execute a step.

  Returns a channel that will eventually produce the result.
  "
  [step]
  ;; let's pretend this does something useful
  (a/to-chan [step]))

(defn execute
  "Executes a plan.

  If the plan is a single step, executes it with `execute-step`. If it
  is an unordered collection of plans, execute all of them in any
  order. If it is an ordered collection of plans, executes them in
  order. For more details on the structure of plans, see
  `icecap.schema`.

  Returns a channel that will produce all of the individual results,
  and then closes.
  "
  [plan]
  (let [c (chan)]
    (condp #(%1 %2) plan
      map? (pipe (execute-step plan) c)
      set? (pipe (merge (map execute plan)) c)
      ;;                       ^- recursion!!!
      vector? (go (loop [sub-plans plan]
                    (>! c (<! (execute (first sub-plans))))
                    ;;           ^- recursion!!!
                    (let [remaining (rest sub-plans)]
                      (if (seq remaining)
                        (recur remaining)
                        (a/close! c))))))
    c))
