(ns icecap.schema
  "The schemata for icecap plans."
  (:require [schema.core :as s]
            [icecap.execute :refer [supported-scheme?]]))


(def Request
  "The schema for the description of a single request."
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

  Consists of either a single request, a set of plans, or a vector of
  plans. (This schema is recursive.)

  This uses prismatic/schema's `conditional` with type dispatch,
  rather than the (perhaps more obvious) `either`. If you give it a
  bogus request, it will only tell you that it didn't satisfy any of
  the three schemas. It won't tell you that it looked a little like a
  request, but with an unsupported URL scheme. Using `conditional`
  allows us to return this improved error message.
  "
  (let [Plan (s/recursive #'Plan)]
    (s/conditional
     map? Request
     vector? (with-one-or-more [Plan])
     set? (with-one-or-more #{Plan}))))
