(ns status-im.transactions.views.transaction-page
  (:require-macros [status-im.utils.views :refer [defview]])
  (:require [re-frame.core :refer [subscribe dispatch]]
            [status-im.components.react :refer [view
                                                text
                                                image
                                                icon
                                                scroll-view
                                                touchable-highlight
                                                touchable-opacity]]
            [status-im.components.styles :refer [icon-ok
                                                 icon-close]]
            [status-im.transactions.styles :as st]
            [status-im.i18n :refer [label label-pluralize]]
            [status-im.utils.random :as random]))

(defn title-bar [title]
  [view st/title-bar
   [text {:style st/title-bar-text} title]
   [image {:source {:uri :icon_close_gray}
           :style  st/icon-close}]])

(defview transaction-page [index transaction]
  []
  (let [title "0.3242 ETH to Jack"
        transactions-info [[(label :t/status) (label :t/pending-confirmation)]
                           [(label :t/recipient) "Alex"]
                           [(label :t/one-more-item) (label :t/value)]
                           [(label :t/fee) "0.3232424"]
                           [(label :t/one-more-item) (label :t/value)]
                           [(label :t/one-more-item) (label :t/value)]
                           [(label :t/one-more-item) (label :t/value)]]]
    [view {:style st/transaction-page
           :key   index}
     [title-bar title]
     [view st/scroll-view-container
      [scroll-view {:style                        st/scroll-view
                    :contentContainerStyle        st/scroll-view-content
                    :showsVerticalScrollIndicator true
                    :scrollEnabled                true}
       (for [index (range (count transactions-info))]
         [view {:style st/transaction-info-item
                :key   index}
          [view {:style st/transaction-info-row}
           [view st/transaction-info-column-title
            [text {:style st/transaction-info-title} (first (nth transactions-info index))]]
           [view st/transaction-info-column-value
            [text {:style st/transaction-info-value} (last (nth transactions-info index))]]]])]]]))
