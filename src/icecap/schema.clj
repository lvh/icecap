(ns icecap.schema
  "The schemata in icecap: request specifications and their components."
  (:require [schema.core :as s])
  (:import [java.net URI]))

(def supported-schemes
  "The currently supported schemes."
  #{"http" "https"})

(defn get-scheme
  "Gets the scheme of a URL."
  [url]
  (.getScheme (URI. url)))

(def supported-scheme?
  "Does the given URL have a supported scheme?"
  (comp supported-schemes get-scheme))

(def SimpleRequest
  "The schema for a simple request."
  {:target (s/both s/Str
                   (s/pred supported-scheme? "supported-scheme?"))})

(defn with-one-or-more
  "Constrain a seq schema to require one or more items.

  Specifically, this checks that both the given schema is matched
  *and* there is at least one item in it."
  [schema]
  (s/both schema
          (s/pred seq "collection of one or more request specs")))

(def RequestSpec
  "The schema for a request specification.

  Consists either of a `SimpleRequest`, a `RequestSpec` set, or a
  `RequestSpec` vector. (So, this schema is recursive.)

  This uses prismatic/schema's `conditional` with type dispatch,
  rather than the (perhaps more obvious) `either`. If you give it a
  bogus `SimpleRequest`, it will only tell you that it didn't satisfy
  any of the three schemas. It won't tell you that it looked a little
  like a `SimpleRequest`, but had unsupported URL scheme. Using
  `conditional` allows us to return this improved error message.
  "
  (let [RequestSpec (s/recursive #'RequestSpec)]
    (s/conditional
     map? SimpleRequest
     vector? (with-one-or-more [RequestSpec])
     set? (with-one-or-more #{RequestSpec}))))
