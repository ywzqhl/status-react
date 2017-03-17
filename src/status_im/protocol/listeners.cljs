(ns status-im.protocol.listeners
  (:require [cljs.reader :as r]
            [status-im.protocol.ack :as ack]
            [status-im.protocol.web3.utils :as u]
            [status-im.protocol.encryption :as e]
            [taoensso.timbre :refer-macros [debug]]
            [status-im.utils.hex :as i]))

(defn- parse-payload [payload]
  (debug :parse-payload)
  (try
    {:payload (r/read-string (u/to-utf8 payload))}
    (catch :default err
      (debug :parse-payload-error err)
      {:error err})))

(defn- decrypt [key content]
  (try
    {:content (r/read-string (e/decrypt key content))}
    (catch :default err
      (debug :decrypt-error err)
      {:error err})))

(defn- parse-content [key {:keys [content]} was-encrypted?]
  (debug :parse-content
         "Key exists:" (not (nil? key))
         "Content exists:" (not (nil? content)))
  (if (and (not was-encrypted?) key content)
    (decrypt key content)
    {:content content}))

(defonce topic->keypair (atom {}))

(defn clear-topic->keypair []
  (reset! topic->keypair {}))

(defn get-keypair [topic]
  (get @topic->keypair topic))

(defn add-keypair! [topic keypair]
  (swap! topic->keypair assoc topic keypair))

(defonce ignore-list (atom #{}))

(defn clear-ignore-list! []
  (reset! ignore-list #{}))

(defn ignore-topic! [topic]
  (swap! ignore-list conj topic))

(defn listen-topic! [topic]
  (swap! ignore-list disj topic))

(defn message-listener
  [{:keys [web3 identity callback]}]
  (fn [error js-message]
    ;; todo handle error
    (when error
      (debug :listener-error error))
    (when-not error
      (debug :message-received)
      (let [{:keys [from payload to] :as message}
            (js->clj js-message :keywordize-keys true)

            {{:keys [type ack? topic] :as payload'} :payload
             payload-error                          :error}
            (parse-payload payload)]
        (when (and (not (@ignore-list topic))
                   (not payload-error)
                   (or (not= (i/normalize-hex identity)
                             (i/normalize-hex from))
                       ;; allow user to receive his own discoveries
                       (= type :discover)))
          (let [keypair (get-keypair topic)
                {:keys [content error]} (parse-content (:private keypair)
                                                       payload'
                                                       (not= "0x0" to))]
            (if error
              (debug :failed-to-handle-message error)
              (let [payload'' (assoc payload' :content content)
                    message'  (assoc message :payload payload'')]
                (callback (if ack? :ack type) message')
                (ack/check-ack! web3 from payload'' identity)))))))))

