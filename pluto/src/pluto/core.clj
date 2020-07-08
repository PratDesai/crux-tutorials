(ns pluto.core
  (:require [crux.api :as crux])
  (:gen-class))

(def node
  (crux/start-node
   {:crux.node/topology '[crux.standalone/topology]
    :crux.kv/db-dir "data/db-dir"}))

(def manifest
  {:crux.db/id :manifest
   :pilot-name "Johanna"
   :id/rocket "SB002-sol"
   :id/employee "22910x2"
   :badges "SETUP"
   :cargo ["stereo" "gold fish" "slippers" "secret note"]})

(defn easy-ingest
  "Uses Crux put transaction to add a vector of documents to a specified
  node"
  [node docs]
  (crux/submit-tx node (mapv (fn [doc] [:crux.tx/put doc]) docs)))


(comment
  (crux/submit-tx node [[:crux.tx/put manifest]])
  (crux/entity-history (crux/db node) :manifest :asc)
  (crux/submit-tx node
                  [[:crux.tx/put
                    {:crux.db/id :commodity/Pu
                     :common-name "Plutonium"
                     :type :element/metal
                     :density 19.816
                     :radioactive true}]

                   [:crux.tx/put
                    {:crux.db/id :commodity/N
                     :common-name "Nitrogen"
                     :type :element/gas
                     :density 1.2506
                     :radioactive false}]

                   [:crux.tx/put
                    {:crux.db/id :commodity/CH4
                     :common-name "Methane"
                     :type :molecule/gas
                     :density 0.717
                     :radioactive false}]])
  (crux/entity-history (crux/db node) :commodity/Pu :asc)
  (crux/entity-history (crux/db node) :commodity/N :asc)
  (crux/entity-history (crux/db node) :commodity/CH4 :asc)
  (crux/submit-tx node
                  [[:crux.tx/put
                    {:crux.db/id :stock/Pu
                     :commod :commodity/Pu
                     :weight-ton 21}
                    #inst "2115-02-13T18"] ;; valid-time
                   
                   [:crux.tx/put
                    {:crux.db/id :stock/Pu
                     :commod :commodity/Pu
                     :weight-ton 23}
                    #inst "2115-02-14T18"]

                   [:crux.tx/put
                    {:crux.db/id :stock/Pu
                     :commod :commodity/Pu
                     :weight-ton 22.2}
                    #inst "2115-02-15T18"]

                   [:crux.tx/put
                    {:crux.db/id :stock/Pu
                     :commod :commodity/Pu
                     :weight-ton 24}
                    #inst "2115-02-18T18"]

                   [:crux.tx/put
                    {:crux.db/id :stock/Pu
                     :commod :commodity/Pu
                     :weight-ton 24.9}
                    #inst "2115-02-19T18"]])
  (crux/submit-tx node
                  [[:crux.tx/put
                    {:crux.db/id :stock/N
                     :commod :commodity/N
                     :weight-ton 3}
                    #inst "2115-02-13T18"  ;; start valid-time
                    #inst "2115-02-19T18"] ;; end valid-time
                   
                   [:crux.tx/put
                    {:crux.db/id :stock/CH4
                     :commod :commodity/CH4
                     :weight-ton 92}
                    #inst "2115-02-15T18"
                    #inst "2115-02-19T18"]])
  (crux/entity-history (crux/db node #inst "2115-02-19") :stock/Pu :asc)
  (crux/entity (crux/db node #inst "2115-02-14") :stock/Pu)
  (crux/entity (crux/db node #inst "2115-02-18") :stock/Pu)
  
  (crux/submit-tx
   node
   [[:crux.tx/put
     (assoc manifest :badges ["SETUP" "PUT"])]])
  
  (crux/entity (crux/db node) :manifest)
  (crux/entity (crux/db node ) :manifest)
   
  ;; to keep this paren here
)
