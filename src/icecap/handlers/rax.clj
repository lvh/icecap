(ns icecap.handlers.rax
  (:require [aleph.http :as http]
            [icecap.handlers.core :refer [defstep]]
            [schema.core :as s]))

(defn api-key-creds
  "Creates auth-params as supported by `authenticate`."
  [user api-key]
  {"RAX-KSKEY:apiKeyCredentials"
   {"username" user
    "apiKey" api-key}})

(defn authenticate
  "Authenticate against an endpoint.

  `url` is the URL of the identity endpoint.

  `auth-params` is a map with the authentication params as supported
  by the identity endpoint. See `api-key-creds`."
  [url auth-params]
  (let [auth-req {"auth" auth-params}]
    (http/post {:body nil})))

(defn endpoints
  "Finds the endpoints for a service, given the auth reponse."
  [auth-resp service-type]
  (let [catalog (get-in auth-resp ["access" "serviceCatalog"])
        with-type (fn [entry]
                    (= (get entry "type")
                       service-type))
        entry (first (filter with-type catalog))]
    (get entry "endpoints")))

(def create-server-schema
  "The schema for a server creation request."
  {:flavor s/Any})

(defn create-server
  "Creates a server."
  [auth-resp request])

(def delete-server-schema
  {:server-id s/Any})

(defn delete-server
  "Deletes a server."
  [auth-resp request])

(defstep :create-nova-server
  create-server-schema
  [step]
  nil)

(defstep :delete-nova-server
  delete-server-schema
  [step]
  nil)
