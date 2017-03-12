(ns status-im.chat.old-views.command
  (:require-macros [status-im.utils.views :refer [defview]])
  (:require [re-frame.core :refer [subscribe dispatch]]
            [status-im.components.react :refer [view
                                                icon
                                                text
                                                touchable-highlight]]
            [status-im.chat.constants :as chat-consts]
            [status-im.chat.old-styles.input :as st]))

(defview command-icon [command]
  [icon-width [:get :command-icon-width]]
  [view st/command-container
   [view {:style    (st/command-text-container command)
          :onLayout (fn [event]
                      (let [width (.. event -nativeEvent -layout -width)]
                        (when (not= icon-width width)
                          (dispatch [:set :command-icon-width width]))))}
    [text {:style st/command-text} (str chat-consts/command-char (:name command))]]])
