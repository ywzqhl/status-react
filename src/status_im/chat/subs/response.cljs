(ns status-im.chat.subs.response
  (:require-macros [reagent.ratom :refer [reaction]])
  (:require [re-frame.core :refer [register-sub dispatch subscribe path]]
            [status-im.utils.platform :refer [ios?]]
            [status-im.constants :refer [response-suggesstion-resize-duration]]
            [status-im.chat.constants :as c]
            [status-im.constants :refer [content-type-status]]
            [status-im.utils.platform :refer [platform-specific]]))

(register-sub :get-response-suggestions-height
  (fn [db _]
    (let [chat-id (subscribe [:get-current-chat-id])]
      (reaction (get-in @db [:animations @chat-id :response-suggestions-height])))))

(register-sub :get-response-height-animation
  (fn [db _]
    (let [chat-id (subscribe [:get-current-chat-id])]
      (reaction (get-in @db [:animations @chat-id :response-height-animation])))))

(register-sub :get-response-height
  (fn [db _]
    (let [chat-id (subscribe [:get-current-chat-id])]
      (reaction (get-in @db [:animations @chat-id :response-height])))))

(register-sub :get-current-response-height
  (fn [db _]
    (let [chat-id (subscribe [:get-current-chat-id])]
      (reaction (get-in @db [:animations @chat-id :current-response-height])))))

(register-sub :get-request-info-height
  (fn [db _]
    (let [chat-id (subscribe [:get-current-chat-id])]
      (reaction (get-in @db [:animations @chat-id :response-request-info-height])))))

(register-sub :get-validation-messages-height
  (fn [db _]
    (let [chat-id (subscribe [:get-current-chat-id])]
      (reaction (get-in @db [:animations @chat-id :response-validation-messages-height])))))