(ns earth.core
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

(comment
  (crux/submit-tx node [[:crux.tx/put manifest]])
  (crux/entity-history (crux/db node) :manifest :asc))