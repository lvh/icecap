(ns icecap.handlers.core
  "The core handler API and behavior."
  (:require [manifold.stream :as ms]
            [schema.core :as s]))

(def get-schema nil) ;; anti-defonce behaavior of get-schema
(defmulti get-schema
  "Return the schema for a plan type."
  identity)

(def execute* nil) ;; anti-defonce behaavior of execute
(defmulti execute*
  "Executes a plan.

  See execute for details."
  (fn [x] (cond (set? x) ::unordered-plans
                (vector? x) ::ordered-plans
                :else (:type x))))

(defmethod execute* ::unordered-plans
  [plans]
  (ms/concat (ms/map execute* plans)))

(defmethod execute* ::ordered-plans
  [plans]
  (let [out (ms/stream)]
    (ms/connect-via plans #(ms/drain-into (execute* %) out) out)
    out))

(def execute
  "Executes a plan.

  If the plan is a single step, executes it with the appropriate step
  handler (see `defstep`). If it is an unordered collection of plans,
  execute all of them in any order. If it is an ordered collection of
  plans, executes them in order.

  For more details on the structure of plans, see `icecap.schema`.

  Returns a manifold stream that will produce all of the individual
  results, and then closes.

  This is different from `execute*`: for a single step, that just
  defers to the implementation defined by `defstep`, which is allowed
  to return anything that can be coerced to a `manifold`
  source. Therefore, `execute*` might return a source, or a seq, or a
  core.async stream. This function will coerce the result to a
  `manifold` stream."
  (comp ms/->source execute*))

(defmacro defstep
  "Define a step implementation.

  A step implementation is defined by its schema (a map), and a
  handler (a sequence of forms). The handler forms will have the step
  being handled injected into it, and should evaluate to something
  that can be converted to a manifold stream.

  The schema does not have to include the step's `:type`; it is
  implicitly added.
  "
  [type schema & fn-tail]
  `(let [full-schema# (assoc ~schema :type (s/eq ~type))]
     (defmethod get-schema ~type
       [type#]
       full-schema#)
     (defmethod execute* ~type
       ~@fn-tail)))

(defstep :succeed
  {(s/optional-key :name) s/Str}
  [step]
  (ms/->source [step]))
