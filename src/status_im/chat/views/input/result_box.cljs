(ns status-im.chat.views.input.result-box
  (:require-macros [status-im.utils.views :refer [defview]])
  (:require [re-frame.core :refer [subscribe dispatch]]
            [status-im.components.react :refer [view
                                                scroll-view
                                                touchable-highlight
                                                text
                                                icon]]
            [status-im.chat.styles.input.parameter-box :as style]
            [status-im.i18n :refer [label]]
            [taoensso.timbre :as log]))

(defn header [title]
  [view {:style style/header-container}
   [text title]])

(defview result-box-container [markup]
  [result-box [:chat-ui-props :result-box]]
  markup)

(defview result-box-view []
  [input-height [:chat-ui-props :input-height]
   {:keys [markup title] :as result-box} [:chat-ui-props :result-box]]
  (when result-box
    [view (style/root 250 input-height)
     [header title]
     [result-box-container markup]
     [view {:flex 1}]]))