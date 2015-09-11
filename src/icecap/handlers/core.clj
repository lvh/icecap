(ns icecap.handlers.core
  "The core handler API and behavior."
  (:require [schema.core :as s]
            [clojure.core.async :as async :refer [go chan <! >!]]))

(def get-schema nil) ;; anti-defonce behaavior of get-schema
(defmulti get-schema identity)

(def execute nil) ;; anti-defonce behaavior of execute
(defmulti execute
  "Executes a plan.

  If the plan is a single step, executes it with the appropriate step
  handler (see `defstep`). If it is an unordered collection of plans,
  execute all of them in any order. If it is an ordered collection of
  plans, executes them in order.

  For more details on the structure of plans, see `icecap.schema`.

  Returns a channel that will produce all of the individual results,
  and then closes.
  "
  (fn [x] (cond (set? x) ::unordered-plans
                (vector? x) ::ordered-plans
                :else (:type x))))

(defmethod execute ::unordered-plans
  [plans]
  (async/merge (map execute plans)))

(defmethod execute ::ordered-plans
  [plans]
  (let [out (chan)]
    (go (loop [plans plans]
          (let [results (<! (async/into [] (execute (first plans))))]
            (<! (async/onto-chan out results false)))
          (if (seq (rest plans))
            (recur (rest plans))
            (async/close! out))))
    out))

(defmacro defstep
  "Define a step implementation.

  A step implementation is defined by its schema (a map), and a
  handler (a sequence of forms). The handler forms will have the step
  being handled injected into it, and should evaluate to a core.async
  chan.

  The schema does not have to include the step's `:type`; it is
  implicitly added.
  "
  [type schema & fn-tail]
  `(let [full-schema# (assoc ~schema :type (s/eq type))]
     (defmethod get-schema ~type
       [type#]
       full-schema#)
     (defmethod execute ~type
       ~@fn-tail)))

(defstep :succeed
  {(s/optional-key :name) s/Str}
  [step]
  (async/to-chan [step]))
