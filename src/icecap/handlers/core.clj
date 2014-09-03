(ns icecap.handlers.core
  "The core handler API."
  (:require [schema.core :as s]))

(defmulti get-schema :type)
(defmulti execute :type)

(defmacro defstep
  "Define a step implementation.

  A step implementation is defined by its schema (a map), and a
  handler (a sequence of forms). The handler forms will have the step
  being handled injected into it, and should evaluate to a core.async
  chan.

  The schema does not have to include the step's `:type`; it is
  implicitly added.
  "
  [type schema & forms]
  (let [full-schema (assoc schema :type (s/eq type))]
    `(do
       (defmethod get-schema ~type
         [~'step]
         ~full-schema)
       (defmethod execute ~type
         [~'step]
         ~@forms))))
