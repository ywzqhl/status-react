(ns status-im.protocol.core
  (:require status-im.protocol.message
            [status-im.protocol.web3.utils :as u]
            [status-im.protocol.web3.filtering :as f]
            [status-im.protocol.web3.delivery :as d]
            [taoensso.timbre :refer-macros [debug]]
            [status-im.protocol.validation :refer-macros [valid?]]
            [status-im.protocol.web3.utils :as u]
            [status-im.protocol.chat :as chat]
            [status-im.protocol.group :as group]
            [status-im.protocol.web3.public-group :as public-group]
            [status-im.protocol.listeners :as l]
            [status-im.protocol.encryption :as e]
            [status-im.protocol.discoveries :as discoveries]
            [cljs.spec :as s]
            [status-im.utils.random :as random]))

;; user
(def send-message! chat/send!)
(def send-seen! chat/send-seen!)
(def send-clock-value-request! chat/send-clock-value-request!)
(def send-clock-value! chat/send-clock-value!)
(def reset-pending-messages! d/reset-pending-messages!)

;; group
(defn start-watching-group!
  [{:keys [web3 identity callback] :as options}]
  (let [topic (group/start-watching-group! options)
        listener-options {:web3     web3
                          :identity identity
                          :callback callback}
        listener (l/message-listener listener-options)]
    (f/filter-topic! web3 {:topic topic} listener)))

(def stop-watching-group! group/stop-watching-group!)
(def send-group-message! group/send!)
(def send-public-group-message! group/send-to-public-group!)
(def invite-to-group! group/invite!)
(def update-group! group/update-group!)
(def remove-from-group! group/remove-identity!)
(def add-to-group! group/add-identity!)
(def leave-group-chat! group/leave!)

;; encryption
;; todo move somewhere, encryption functions shouldn't be there
(def new-keypair! e/new-keypair!)

;; discoveries
(defn watch-user! [{:keys [web3 identity callback] :as options}]
  (let [topic (discoveries/watch-user! options)
        listener-options {:web3     web3
                          :identity identity
                          :callback callback}
        listener (l/message-listener listener-options)]
    (f/filter-topic! web3 {:topic topic} listener)))

(def stop-watching-user! discoveries/stop-watching-user!)
(def contact-request! discoveries/contact-request!)
(def broadcast-profile! discoveries/broadcast-profile!)
(def send-status! discoveries/send-status!)
(def send-discoveries-request! discoveries/send-discoveries-request!)
(def send-discoveries-response! discoveries/send-discoveries-response!)
(def update-keys! discoveries/update-keys!)

(def message-pending? d/message-pending?)

;; initialization
(s/def ::rpc-url string?)
(s/def ::identity string?)
(s/def :message/chat-id string?)
(s/def ::public? (s/and boolean? true?))
(s/def ::group-id :message/chat-id)
(s/def ::group (s/or
                 :group (s/keys :req-un [::group-id :message/keypair])
                 :public-group (s/keys :req-un [::group-id ::public?])))
(s/def ::groups (s/* ::group))
(s/def ::callback fn?)
(s/def ::contact (s/keys :req-un [::identity :message/keypair]))
(s/def ::contacts (s/* ::contact))
(s/def ::profile-keypair :message/keypair)
(s/def ::options
  (s/merge
    (s/keys :req-un [::rpc-url ::identity ::groups ::profile-keypair
                     ::callback :discoveries/hashtags ::contacts])
    ::d/delivery-options))

(defn stop-watching-all! []
  (l/clear-ignore-list!)
  (l/clear-topic->keypair)
  (f/remove-all-filters!))

(def reset-all-pending-messages! d/reset-all-pending-messages!)

(defn init-whisper!
  [{:keys [rpc-url identity groups callback
           contacts profile-keypair pending-messages]
    :as   options}]
  {:pre [(valid? ::options options)]}
  (debug :init-whisper)
  (stop-watching-all!)
  (d/reset-all-pending-messages!)
  (let [web3             (u/make-web3 rpc-url)
        listener-options {:web3     web3
                          :identity identity
                          :callback callback}
        listener         (l/message-listener listener-options)

        ;; prepare group topics
        group-topics     (for [group groups]
                           (let [options (merge listener-options group)]
                             (group/start-watching-group! options)))

        ;; prepare profile topics
        profile-topics   (for [{:keys [identity keypair]} contacts]
                           (discoveries/watch-user! {:user-identity identity
                                                     :keypair       keypair}))
        all-topics       (vec (concat group-topics profile-topics))]
    ;; start listening to user's inbox
    (f/filter-topics!
      web3
      {:to     identity
       :topics [f/status-topic]}
      listener)

    ;; start listening all other topics
    (when (seq all-topics)
      (f/filter-topics! web3 {:topics [all-topics]} listener))

    (d/set-pending-mesage-callback! callback)
    (let [online-message #(discoveries/send-online!
                            {:web3    web3
                             :message {:from       identity
                                       :message-id (random/id)
                                       :keypair    profile-keypair}})]
      (d/run-delivery-loop!
        web3
        (assoc options :online-message online-message)))
    (doseq [pending-message pending-messages]
      (d/add-prepeared-pending-message! web3 pending-message))
    web3))
