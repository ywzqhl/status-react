(ns status-im.chat.models.input
  (:require [clojure.string :as str]
            [status-im.chat.constants :as const]
            [taoensso.timbre :as log]))

(defn content-by-message-id [db chat-id message-id]
  (get-in db [:chats chat-id]))

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

(defn selected-chat-command [{:keys [current-chat-id] :as db} chat-id]
  (let [chat-id          (or chat-id current-chat-id)
        input-text       (get-in db [:chats chat-id :input-text])
        input-metadata   (get-in db [:chats chat-id :input-metadata])
        possible-actions (possible-chat-actions db chat-id)
        command-args     (str/split input-text const/spacing-char)
        command-name     (first command-args)]
    (when (.startsWith (or command-name "") const/command-char)
      (when-let [command (-> (filter (fn [[{:keys [name]} message-id]]
                                       (and (= name (subs command-name 1))
                                            (= message-id (or (:to-message-id input-metadata)
                                                              :any))))
                                     possible-actions)
                             (ffirst))]
        {:command       command
         :metadata      input-metadata
         :args          (remove empty? (rest command-args))}))))

(defn current-chat-argument-position
  [command input-text]
  (if command
    (let [current (-> (:args command) (count))]
      (if (= (last input-text) const/spacing-char)
        current
        (dec current)))
    -1))

(defn argument-position [{:keys [current-chat-id] :as db} chat-id]
  (let [chat-id          (or chat-id current-chat-id)
        input-text       (get-in db [:chats chat-id :input-text])
        chat-command     (selected-chat-command db chat-id)]
    (current-chat-argument-position chat-command input-text)))

(defn command-complete?
  ([{:keys [current-chat-id] :as db} chat-id]
   (let [chat-id          (or chat-id current-chat-id)
         input-text       (get-in db [:chats chat-id :input-text])
         chat-command     (selected-chat-command db chat-id)]
     (command-complete? chat-command)))
  ([chat-command]
   (and chat-command
        (= (count (:args chat-command))
           (count (get-in chat-command [:command :params]))))))

(defn args->params [{:keys [command args]}]
  (let [params (:params command)]
    (->> args
         (map-indexed (fn [i value]
                        (vector (get-in params [i :name]) value)))
         (into {}))))