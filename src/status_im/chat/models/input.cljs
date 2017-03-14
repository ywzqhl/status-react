(ns status-im.chat.models.input
  (:require [clojure.string :as str]
            [status-im.chat.constants :as const]))

(defn possible-chat-actions [db chat-id]
  (let [{:keys [commands requests]} (get-in db [:chats chat-id])
        commands  (mapv (fn [[_ command]]
                          (vector command :any))
                        commands)
        responses (mapv (fn [{:keys [message-id type]}]
                          (vector
                            (get-in db [:chats chat-id :responses type])
                            message-id))
                        requests)]
    (into commands responses)))

(defn selected-chat-command [input-text possible-actions]
  (let [command-args (str/split input-text const/spacing-char)
        command-name (first command-args)]
    (when (.startsWith command-name const/command-char)
      (when-let [command (-> (filter (fn [[{:keys [name]} message-id]]
                                       (and (= name (subs command-name 1))
                                            (= message-id :any)))
                                     possible-actions)
                             (ffirst))]
        {:command command
         :args    (remove empty? (rest command-args))}))))

(defn current-chat-argument-position
  [command input-text]
  (if command
    (let [current (-> (:args command) (count))]
      (if (= (last input-text) const/spacing-char)
        current
        (dec current)))
    -1))

(defn argument-position [{:keys [current-chat-id] :as db}]
  (let [possible-actions (possible-chat-actions db current-chat-id)
        input-text       (get-in db [:chats current-chat-id :input-text])
        chat-command     (selected-chat-command input-text possible-actions)]
    (current-chat-argument-position chat-command input-text)))

(defn command-complete? [{:keys [current-chat-id] :as db}]
  (let [possible-actions (possible-chat-actions db current-chat-id)
        input-text       (get-in db [:chats current-chat-id :input-text])
        chat-command     (selected-chat-command input-text possible-actions)]
    (= (count (:args chat-command))
       (count (get-in chat-command [:command :params])))))