(ns status-im.chat.views.input.input
  (:require-macros [status-im.utils.views :refer [defview]])
  (:require [reagent.core :as r]
            [re-frame.core :refer [subscribe dispatch]]
            [status-im
             [accessibility-ids :as id]
             [components.react :refer [view
                                       text-input]]
             [chat.styles.input.input :as style]]))

(defview input-view []
  [default-value [:get-chat-input-text]]
  [view style/input-root
   [text-input {:style               style/input-view
                :accessibility-label id/chat-message-input
                :default-value       default-value
                :on-change-text      #(dispatch [:set-chat-input-text %])
                :on-submit-editing   #(dispatch [:send-chat-message])}]])

(defn container []
  (let []
    (r/create-class
      {:reagent-render
       (fn []
         [view style/root
          [view style/container
           [input-view]]])})))