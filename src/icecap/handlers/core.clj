(ns icecap.handlers.core)

(defmulti get-schema :type)
(defmulti execute :type)

(defmacro defstep
  "Define a step implementation.

  The schema does not have to include the step's `:type`; it is
  implicitly added.
  "
  [type schema & forms]
  (let [full-schema (assoc schema :type type)]
    `(do
       (defmethod get-schema ~type
         [~'step]
         ~full-schema)
       (defmethod execute ~type
         [~'step]
         ~@forms))))
