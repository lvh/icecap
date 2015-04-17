(ns icecap.handlers.rax-test
  (:require [icecap.handlers.rax :refer :all]
            [clojure.test :refer :all]))

(def auth-resp
  "An auth response, carefully extracted from mimic."
  (let [base "http://localhost:8900/mimicking"
        tenant "250510600011776"
        url (partial str base)]
    {"access"
     {"serviceCatalog"
      [{"name" "cloudLoadBalancers"
        "type" "rax:load-balancer"
        "endpoints" [{"publicURL" (url "/LoadBalancerApi-92422e/ORD/v2/" tenant)
                      "region" "ORD"
                      "tenantId" tenant}]}
       {"name" "cloudMonitoring"
        "type" "rax: monitor"
        "endpoints" [{"publicURL" (url "/MaasApi-c4f28a/ORD/v1.0/" tenant)
                      "region" "ORD"
                      "tenantId" tenant}]}
       {"name" "cloudServersOpenStack"
        "type" "compute"
        "endpoints" [{"publicURL" (url "/NovaApi-6bcfc3/ORD/v2/" tenant)
                      "region" "ORD"
                      "tenantId" tenant}]}
       {"name" "cloudQueues"
        "type" "rax:queues"
        "endpoints" (into []
                          (for [region ["ORD" "DFW" "IAD"]]
                            {"publicURL" (url "/QueueApi-0cb2c5/"
                                              region "/v1/" tenant)
                             "region" region
                             "tenantId" tenant}))}
       {"name" "rackconnect"
        "type" "rax:rackconnect"
        "endpoints" [{"publicURL" (url "/RackConnectV3-77a9a0/ORD/v3/" tenant)
                      "region" "ORD"
                      "tenantId" tenant}]}
       {"name" "cloudFiles"
        "type" "object-store"
        "endpoints" (let [tenant (str "MossoCloudFS_"
                                      "c5706c5c-4bda-5d72-8bf6-4c565f4a7361")]
                      [{"publicURL" (url "/SwiftMock-3b8c2b/ORD/v1/" tenant)
                        "region" "ORD"
                        "tenantId" tenant}])}]
      "token" {"RAX-AUTH:authenticatedBy" ["PASSWORD"]
               "expires" "2015-04-18T16:45:45.999-05:00"
               "id" "token_478e47a5-d448-45a9-a542-76d2bd9cd949"
               "tenant" {"id" tenant
                         "name" tenant}}
      "user" {"id" "-2476143622379761678"
              "name" "mimic"
              "roles" [{"description" "User Admin Role."
                        "id" "3"
                        "name" "identity:user-admin"}]}}}))

(deftest endpoints-test
  (testing "find all endpoints"
    (let [catalog (get-in auth-resp ["access" "serviceCatalog"])
          many-eps? (fn [entry]
                      (let [eps (get entry "endpoints")]
                        (> (count eps) 1)))
          multi-ep-service (first (filter many-eps? catalog))
          {eps "endpoints" type "type"} multi-ep-service]
      (is (= (multi-ep-service "name") "cloudQueues"))
      (is (= (endpoints auth-resp type) eps)))))
