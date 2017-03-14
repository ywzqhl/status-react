(ns status-im.chat.views.input.suggestions
  (:require-macros [status-im.utils.views :refer [defview]])
  (:require [re-frame.core :refer [subscribe dispatch]]
            [status-im.components.react :refer [view
                                                scroll-view
                                                touchable-highlight
                                                text
                                                icon]]
            [status-im.chat.styles.input.suggestions :as style]
            [status-im.i18n :refer [label]]
            [taoensso.timbre :as log]))

(defn suggestion-item [{:keys [on-press name description]}]
  [touchable-highlight {:on-press on-press}
   [view (style/item-suggestion-container true)
    [view {:style style/item-suggestion-name}
     [text {:style style/item-suggestion-name-text
            :font  :roboto-mono}
      "/" name]]
    [text {:style style/item-suggestion-description}
     description]]])

(defview request-item [index {:keys [type message-id]}]
  [{:keys [name description] :as response} [:get-response type]
   {:keys [chat-id]} [:get-current-chat]]
  [suggestion-item {:on-press    #(do (dispatch [:set-response-chat-command message-id type])
                                      (dispatch [:select-chat-input-command response]))
                    :name        name
                    :description description}])

(defview command-item [index [command {:keys [name description] :as command}]]
  []
  [suggestion-item {:on-press    #(dispatch [:select-chat-input-command command])
                    :name        name
                    :description description}])

(defn item-title [top-padding? s]
  [view (style/item-title-container top-padding?)
   [text {:style style/item-title-text}
    s]])

(defn header []
  [view {:style style/header-container}
   [view style/header-icon]])

(defview suggestions-view []
  [input-height [:chat-ui-props :input-height]
   requests [:chat :requests]
   suggestions [:chat :command-suggestions]]
  [view (style/root 250 input-height)
   [header]
   [view {:flex 1}
    [scroll-view {:keyboardShouldPersistTaps true}
     (when (seq requests)
       [view
        [item-title false (label :t/suggestions-requests)]
        (for [{:keys [chat-id message-id] :as request} requests]
          ^{:key [chat-id message-id]}
          [request-item 0 request])])
     (when (seq suggestions)
       [view
        [item-title (seq requests) (label :t/suggestions-commands)]
        (for [command (remove #(nil? (:title (second %))) suggestions)]
          ^{:key (first command)}
          [command-item 0 command])])]]])