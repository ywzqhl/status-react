(ns status-im.chat.handlers.input
  (:require [re-frame.core :refer [enrich after dispatch]]
            [taoensso.timbre :as log]
            [status-im.chat.constants :as const]
            [status-im.chat.suggestions :as suggestions]
            [status-im.utils.handlers :as handlers]
            [status-im.components.status :as status]))

(handlers/register-handler
  :set-chat-input-text
  (fn [{:keys [current-chat-id] :as db} [_ text]]
    (dispatch [:update-suggestions current-chat-id text])
    (assoc-in db [:chats current-chat-id :input-text] text)))

(handlers/register-handler
  :select-chat-input-command
  (handlers/side-effect!
    (fn [{:keys [current-chat-id] :as db} [_ text]]
      (dispatch [:set-chat-input-text (str const/command-char text const/spacing-char)])
      (dispatch [:set-chat-ui-props :show-suggestions? false]))))

(handlers/register-handler
  :update-suggestions
  (fn [{:keys [current-chat-id] :as db} [_ chat-id text]]
    (let [chat-id     (or chat-id current-chat-id)
          chat-text   (or text (get-in db [:chats chat-id :input-text]) "")
          suggestions (suggestions/get-suggestions db chat-text)
          {:keys [dapp?]} (get-in db [:contacts chat-id])]
      (assoc-in db [:chats chat-id :command-suggestions] suggestions))))