(ns status-im.protocol.web3.filtering
  (:require [status-im.protocol.web3.utils :as u]
            [cljs.spec :as s]
            [taoensso.timbre :refer-macros [debug]]))

(def status-topic "status-dapp-topic")
(defonce filters (atom {}))
(defonce all-topics (atom #{}))

(s/def ::options (s/keys :opt-un [:message/to :message/topics]))

(defn remove-filter! [web3 options]
  (when-let [filter (get-in @filters [web3 options])]
    (.stopWatching filter)
    (debug :stop-watching options)
    (swap! filters update web3 dissoc options)))

(defn filter-topics!
  [web3 {:keys [topics] :as options} callback]
  (debug :filter-topics options)
  (remove-filter! web3 options)
  (swap! all-topics into topics)
  (let [filter (.filter (u/shh web3)
                        (clj->js options)
                        callback)]
    (swap! filters assoc-in [web3 options] filter)))

(defn filter-topic!
  [web3 {:keys [topic] :as options} callback]
  (debug :add-topic options)
  (when-not (@all-topics topic)
    (swap! all-topics conj topic)
    (let [options' (-> options
                       (dissoc :topic)
                       (assoc :topics [topic]))
          filter (.filter (u/shh web3)
                          (clj->js options')
                          callback)]
      (swap! filters assoc-in [web3 options'] filter))))

(defn remove-all-filters! []
  (doseq [[web3 filters] @filters]
    (doseq [options (keys filters)]
      (remove-filter! web3 options))))
