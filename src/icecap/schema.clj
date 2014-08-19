(ns icecap.schema
  (:require [schema.core :as s])
  (:import [java.net URI]))

(def supported-schemes
  "The currently supported schemes."
  #{"http" "https"})

(defn get-scheme
  "Gets the scheme of a (String) URL."
  [url]
  (.getScheme (URI. url)))

(def supported-scheme?
  "Does the given URL have a supported scheme?"
  (comp supported-schemes get-scheme))

(def SimpleRequest
  "A simple request."
  {:target (s/both s/Str
                   (s/pred supported-scheme? "supported-scheme?"))})

(def RequestSpec
  "A request specification.

  Please note that this uses prismatic/schema's `conditional` with
  type dispatch, rather than the (perhaps more obvious) `either`. The
  problem with `either` is that it didn't know which of the schemas
  *probably* should have validated: if you give it a bogus
  `SimpleRequest`, it can only tell you that it:

  - wasn't a valid `SimpleRequest`
  - wasn't an unordered collection of valid `SimpleRequests`
  - wasn't an ordered collection of valid `SimpleRequests`

  ... but it fails to tell you that it *was* something that looked
  like a SimpleRequest but didn't validate because, for example, its
  target was `ftp`, which is an unsupported URL scheme. That would've
  been far more useful. Using `conditional` allows us to return
  this (useful) error message.
  "
  (s/conditional
   map? SimpleRequest
   vector? [(s/recursive #'RequestSpec)]
   set? #{(s/recursive #'RequestSpec)}))
