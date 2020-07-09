(ns mercury.core
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

(def pluto-data
  [{:crux.db/id :commodity/Pu
    :common-name "Plutonium"
    :type :element/metal
    :density 19.816
    :radioactive true}

   {:crux.db/id :commodity/N
    :common-name "Nitrogen"
    :type :element/gas
    :density 1.2506
    :radioactive false}

   {:crux.db/id :commodity/CH4
    :common-name "Methane"
    :type :molecule/gas
    :density 0.717
    :radioactive false}

   {:crux.db/id :commodity/Au
    :common-name "Gold"
    :type :element/metal
    :density 19.300
    :radioactive false}

   {:crux.db/id :commodity/C
    :common-name "Carbon"
    :type :element/non-metal
    :density 2.267
    :radioactive false}

   {:crux.db/id :commodity/borax
    :common-name "Borax"
    :IUPAC-name "Sodium tetraborate decahydrate"
    :other-names ["Borax decahydrate" "sodium borate"
                  "sodium tetraborate" "disodium tetraborate"]
    :type :mineral/solid
    :appearance "white solid"
    :density 1.73
    :radioactive false}])

(defn easy-ingest
  "Uses Crux put transaction to add a vector of documents to a specified
  node"
  [node docs]
  (crux/submit-tx node (mapv (fn [doc] [:crux.tx/put doc]) docs)))

(defn filter-type
  [type]
  (crux/q (crux/db node)
          {:find '[name]
           :where '[[e :type t]
                    [e :common-name name]]
           :args [{'t type}]}))

(defn filter-appearance
  [description]
  (crux/q (crux/db node)
          {:find '[name IUPAC]
           :where '[[e :common-name name]
                    [e :IUPAC-name IUPAC]
                    [e :appearance appearance]]
           :args [{'appearance description}]}))


(comment
  (do
    (crux/submit-tx node [[:crux.tx/put manifest]])
    (crux/entity-history (crux/db node) :manifest :asc)
    (crux/entity (crux/db node) :manifest)
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
    (crux/entity (crux/db node) :manifest)

    (easy-ingest node pluto-data)

    (crux/q (crux/db node)
            '{:find [element]
              :where [[element :type :element/metal]]})
  ;; => #{[:commodity/Pu] [:commodity/Au]}

    (crux/q (crux/db node)
            '{:find [element]
              :where [[element :type :element/gas]]})

    (crux/q (crux/db node)
            '{:find [name]
              :where [[e :type :element/metal]
                      [e :common-name name]]})

    (crux/q (crux/db node)
            '{:find [name rho]
              :where [[e :density rho]
                      [e :common-name name]]})
    (crux/q (crux/db node)
            {:find '[name]
             :where '[[e :type t]
                      [e :common-name name]]
             :args [{'t :element/metal}]})

    (crux/q (crux/db node) {:find '[type]
                            :where '[[e :type type]]})
    (filter-type :element/gas)
    (crux/q (crux/db node) {:find '[description]
                            :where '[[e :appearance description]]})
    (filter-appearance "white solid")
    (crux/submit-tx
     node [[:crux.tx/put (assoc manifest
                                :badges ["SETUP" "PUT" "DATALOG-QUERIES"])]])
    ;; to keep this paren here
    )
  ;; to keep this paren here
)

