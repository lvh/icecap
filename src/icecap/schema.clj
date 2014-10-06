(ns icecap.schema
  "The schemata for icecap plans."
  (:require [schema.core :as s]
            [icecap.handlers.http]
            [icecap.handlers.delay]
            [icecap.handlers.core :refer [get-schema]]))

(def Step
  "The schema for the description of a single step.

  An step is a small, atomic part of a capability. It includes things
  like HTTP requests or delays."
  (let [all-methods (methods get-schema)
        all-types (keys all-methods)
        preds-and-schemas (for [[type schema-fn] all-methods]
                            [#(= (:type %) type) (schema-fn type)])]
    (apply s/conditional
           (concat (flatten preds-and-schemas)
                   [:else {:type (apply s/enum all-types)
                           s/Any s/Any}]))))

(defn with-two-or-more
  "Constrain a seq schema to require two or more items.

  Specifically, this checks that both the given schema is matched
  *and* there are at least two items in it."
  [schema]
  (s/both schema
          (s/pred #(>= (count %) 2) "collection of two or more plans")))

(def Plan
  "The schema for a plan.

  Consists of either a single step, a set of plans, or a vector of
  plans. (This schema is recursive.)

  This uses prismatic/schema's `conditional` with type dispatch,
  rather than the (perhaps more obvious) `either`. If you give it a
  bogus step, it will only tell you that it didn't satisfy any of
  the three schemas. It won't tell you that it was an invalid step,
  let alone what was wrong with it. Using `conditional` allows us to
  return this improved error message.
  "
  (let [Plan (s/recursive #'Plan)]
    (s/conditional
     map? Step
     vector? (with-two-or-more [Plan])
     set? (with-two-or-more #{Plan}))))
