(ns icecap.util
  "Generic utilities that don't fit elsewhere.

  This module should probably be considered a transitory place for
  these things to stay."
  (:import (java.net URI)))

(defn get-scheme
  "Gets the scheme of a URL."
  [url]
  (.getScheme (URI. url)))
