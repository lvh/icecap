(ns icecap.handlers.delay-test
  (:require [schema.core :as s]
            [icecap.handlers.core :refer [execute get-schema]]
            [clojure.core.async :refer [take! chan close!]]
            [icecap.handlers.delay :refer :all]
            [clojure.test :refer :all])
  (:import (java.lang Thread)))

(defn fake-clock
  "Creates a fake clock, allowing you to add waiter functions (which
  will be called when the clock time is higher than the trigger time
  for the waiter), and advance the clock."
  []
  (let [clock (atom 0)
        waiters (atom (sorted-map))]
    {:clock clock
     :advance (fn advance
                [interval]
                (let [current-time (swap! clock + interval)
                      events (subseq @waiters <= current-time)]
                  (doseq [[time f] events]
                    (swap! waiters dissoc time)
                    (f))))
     :add-waiter (fn add-waiter
                   [time absolute? f]
                   (let [trigger (if absolute?
                                   time
                                   (+ @clock time))]
                     (if (<= @clock trigger)
                       (swap! waiters assoc trigger f)
                       (f))))}))

(defn fake-timeout
  "Create a test double for core.async/timeout."
  []
  (let [{advance :advance add-waiter :add-waiter} (fake-clock)
        timeout (fn [^long ms]
                  (let [c (chan)]
                    (add-waiter ms false (fn []
                                           (close! c)))
                    c))]
    [advance {#'clojure.core.async/timeout timeout}]))

(deftest delay-schema-tests
  (testing "allowable delay ranges"
    (let [schema (get-schema :delay)]
      (are [amount] (nil? (s/check schema {:type :delay :amount amount}))
        1
        10
        30))))

(deftest delay-tests
  (testing "delay functions correctly"
    (is (nil? (let [step {:type :delay :amount 10}
                    state (atom true)
                    still-open? (fn [] @state)
                    [advance redefs] (fake-timeout)]
                (with-redefs-fn redefs
                  (fn []
                    (let [c (execute step)]
                      (assert (still-open?) "before adding callback")
                      (take! c (fn [_] (reset! state false)))
                      (assert (still-open?) "after adding callback")
                      (advance 5)
                      (assert (still-open?) "before trigger")
                      (advance 10)
                      (while (still-open?)
                        ;; The fn attached with take! will fire
                        ;; asynchronously, outside of our control.
                        (Thread/sleep 50))))))))))
