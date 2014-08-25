(ns icecap.schema
  "The schemata for icecap plans."
  (:require [schema.core :as s]
            [icecap.execute :refer [supported-scheme?]]))

(def Step
  "The schema for the description of a single step.

  An step is a small, atomic part of a capability. It includes things
  like HTTP requests or delays."
  {:target (s/both s/Str
                   (s/pred supported-scheme? "supported-scheme?"))})

(defn with-one-or-more
  "Constrain a seq schema to require one or more items.

  Specifically, this checks that both the given schema is matched
  *and* there is at least one item in it."
  [schema]
  (s/both schema
          (s/pred seq "collection of one or more plans")))

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
     vector? (with-one-or-more [Plan])
     set? (with-one-or-more #{Plan}))))
