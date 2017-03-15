(ns status-im.chat.handlers.input
  (:require [re-frame.core :refer [enrich after dispatch]]
            [taoensso.timbre :as log]
            [status-im.chat.constants :as const]
            [status-im.chat.models.input :as input-model]
            [status-im.chat.suggestions :as suggestions]
            [status-im.components.status :as status]
            [status-im.utils.handlers :as handlers]
            [clojure.string :as str]))

(handlers/register-handler
  :set-chat-input-text
  (fn [{:keys [current-chat-id] :as db} [_ text chat-id]]
    (let [chat-id (or chat-id current-chat-id)]
      (dispatch [:update-suggestions chat-id text])
      (assoc-in db [:chats chat-id :input-text] text))))

(handlers/register-handler
  :select-chat-input-command
  (handlers/side-effect!
    (fn [{:keys [current-chat-id] :as db} [_ {:keys [name] :as command}]]
      (dispatch [:set-chat-input-text (str const/command-char name const/spacing-char)])
      (dispatch [:set-chat-ui-props :show-suggestions? false])
      (dispatch [:load-chat-parameter-box command 0]))))

(handlers/register-handler
  :set-command-argument
  (handlers/side-effect!
    (fn [{:keys [current-chat-id] :as db} [_ [index arg]]]
      (let [command      (-> (get-in db [:chats current-chat-id :input-text])
                             (str/split const/spacing-char))
            command-name (first command)
            command-args (into [] (rest command))
            command-args (if (< index (count command-args))
                           (assoc command-args index arg)
                           (conj command-args arg))]
        (dispatch [:set-chat-input-text (str command-name
                                             const/spacing-char
                                             (str/join const/spacing-char command-args)
                                             const/spacing-char)])))))

(handlers/register-handler
  :update-suggestions
  (fn [{:keys [current-chat-id] :as db} [_ chat-id text]]
    (let [chat-id     (or chat-id current-chat-id)
          chat-text   (or text (get-in db [:chats chat-id :input-text]) "")
          suggestions (suggestions/get-suggestions db chat-text)
          {:keys [dapp?]} (get-in db [:contacts chat-id])]
      (assoc-in db [:chats chat-id :command-suggestions] suggestions))))

(handlers/register-handler
  :load-chat-parameter-box
  (handlers/side-effect!
    (fn [{:keys [current-chat-id] :as db} [_ {:keys [name type] :as command}]]
      (let [parameter-index (input-model/argument-position db)]
        (when (and command (> parameter-index -1))
          (let [data   (get-in db [:local-storage current-chat-id])
                path   [(if (= :command type) :commands :responses)
                        name
                        :params
                        parameter-index
                        :suggestions]
                args   (-> (get-in db [:chats current-chat-id :input-text])
                           (str/split const/spacing-char)
                           (rest))
                params {:parameters {:args args}
                        :context    {:data data}}]
            (status/call-jail current-chat-id
                              path
                              params
                              #(dispatch [:suggestions-handler
                                          {:chat-id         current-chat-id
                                           :command         command
                                           :parameter-index parameter-index
                                           :result          %}]))))))))

(handlers/register-handler
  ::send-message
  (handlers/side-effect!
    (fn [{:keys [current-chat-id current-public-key current-account-id] :as db} [_ command-message chat-id]]
      (let [chat-id (or chat-id current-chat-id)
            text    (get-in db [:chats chat-id :input-text])
            data    {:message  text
                     :command  command-message
                     :chat-id  chat-id
                     :identity current-public-key
                     :address  current-account-id}]
        (dispatch [:set-chat-input-text nil chat-id])
        (cond
          command-message
          (dispatch [:check-commands-handlers! data])
          (not (str/blank? text))
          (dispatch [:prepare-message data]))))))

(handlers/register-handler
  ::send-command
  (handlers/side-effect!
    (fn [db [_ command]]
      (log/debug "ALWX >> send-command" command))))

#_(handlers/register-handler
  :send-command!
  (handlers/side-effect!
    (fn [{:keys [current-chat-id current-account-id] :as db}]
      (let [{:keys [params] :as command} (commands/get-chat-command db)
            {:keys [parameter-idx]} (commands/get-command-input db)

            last-parameter? (= (inc parameter-idx) (count params))

            parameters      {:command command :input command-input}

            {:keys [command content]} (command-input db)
            content'        (content-by-command command content)]
        (dispatch [:set-command-parameter
                   {:value     content'
                    :parameter (para s parameter-idx)}])
        (if last-parameter?
          (dispatch [:check-suggestions-trigger! parameters])
          (dispatch [::start-command-validation!
                     {:chat-id current-chat-id
                      :address current-account-id
                      :handler #(dispatch [:next-command-parameter])}]))))))

(handlers/register-handler
  :send-current-message
  (handlers/side-effect!
    (fn [{:keys [current-chat-id] :as db} [_ chat-id]]
      (let [chat-id          (or chat-id current-chat-id)
            possible-actions (input-model/possible-chat-actions db chat-id)
            input-text       (get-in db [:chats chat-id :input-text])
            chat-command     (input-model/selected-chat-command input-text possible-actions)]
        (if chat-command
          (when (input-model/command-complete? chat-command)
            (dispatch [::send-command chat-command]))
          (dispatch [::send-message nil chat-id]))))))