(ns status-im.contacts.views.group-contacts-list
  (:require-macros [status-im.utils.views :refer [defview]])
  (:require [re-frame.core :refer [dispatch]]
            [status-im.contacts.screen :refer [contact-options contact-on-press]]
            [status-im.components.react :refer [view text
                                                image
                                                icon
                                                touchable-highlight
                                                list-view
                                                list-item]]
            [status-im.components.contact.contact :refer [contact-view]]
            [status-im.components.contact.styles :as cvs]
            [status-im.components.text-field.view :refer [text-field]]
            [status-im.components.status-bar :refer [status-bar]]
            [status-im.components.toolbar-new.view :refer [toolbar-with-search toolbar]]
            [status-im.components.toolbar-new.actions :as act]
            [status-im.components.drawer.view :refer [drawer-view open-drawer]]
            [status-im.contacts.styles :as st]
            [status-im.utils.listview :as lw]
            [status-im.i18n :refer [label]]))

(defn contact-list-entry [{:keys [click-handler icon icon-style label]}]
  [touchable-highlight
   {:on-press click-handler}
   [view cvs/contact-container
    [view cvs/contact-inner-container
     [image {:source {:uri icon}
             :style  icon-style}]
     [view cvs/info-container
      [text {:style           cvs/name-text
             :number-of-lines 1}
       label]]]]])

(defn modal-view [action click-handler]
  [view
   [contact-list-entry {:click-handler #(do
                                          (dispatch [:send-to-webview-bridge
                                                     {:event (name :webview-send-transaction)}])
                                          (dispatch [:navigate-back]))
                        :icon          :icon_enter_address
                        :icon-style    st/enter-address-icon
                        :label         (label :t/enter-address)}]
   [contact-list-entry {:click-handler #(click-handler :qr-scan action)
                        :icon          :icon_scan_q_r
                        :icon-style    st/scan-qr-icon
                        :label         (label (if (= :request action)
                                                :t/show-qr
                                                :t/scan-qr))}]])

(defview contact-list-toolbar-edit [group]
  [toolbar {:nav-action     (act/back #(dispatch [:set-in [:contact-list-ui-props :edit?] false]))
            :actions        [{:image :blank}]
            :title          (if-not group
                              (label :t/contacts)
                              (or (:name group) (label :t/contacts-group-new-chat)))}])

(defview contact-list-toolbar [group]
  [modal       [:get :modal]
   show-search [:get-in [:toolbar-search :show]]
   search-text [:get-in [:toolbar-search :text]]]
  (toolbar-with-search
    {:show-search?       (= show-search :contact-list)
     :search-text        search-text
     :search-key         :contact-list
     :title              (if-not group
                                 (label :t/contacts)
                                 (or (:name group) (label :t/contacts-group-new-chat)))
     :search-placeholder (label :t/search-contacts)
     :actions            (if modal
                           (act/back #(dispatch [:navigate-back]))
                           [(act/opts [{:text (label :t/edit)
                                        :value #(dispatch [:set-in [:contact-list-ui-props :edit?] true])}])])}))

(defn render-separator [_ row-id _]
  (list-item ^{:key row-id}
             [view st/contact-item-separator-wrapper
              [view st/contact-item-separator]]))

(defn render-row [click-handler action params group edit?]
  (fn [row _ _]
    (list-item
      ^{:key row}
      [contact-view {:contact        row
                     :extended?      edit?
                     :extend-options (contact-options row group)
                     :on-press       (if click-handler
                                       #(click-handler row action params)
                                       contact-on-press)}])))

(defview contacts-list-view [group click-handler action edit?]
  [contacts [:all-added-group-contacts-filtered (:group-id group)]
   params [:get :contacts-click-params]
    (when contacts
      [list-view {:dataSource                (lw/to-datasource contacts)
                  :enableEmptySections       true
                  :renderRow                 (render-row click-handler action params group edit?)
                  :bounces                   false
                  :keyboardShouldPersistTaps true
                  :renderHeader              #(list-item [view [view st/contact-list-spacing]])
                  :renderFooter              #(list-item [view st/contact-list-spacing])
                  :renderSeparator           render-separator
                  :style                     st/contacts-list}])])

(defview group-contacts-list []
  [action [:get :contacts-click-action]
   modal [:get :modal]
   edit? [:get-in [:contact-list-ui-props :edit?]]
   click-handler [:get :contacts-click-handler]
   group [:get :contacts-group]
   type [:get :group-type]]
  [drawer-view
   [view {:flex 1}
    [view
     [status-bar]
     (if edit?
       [contact-list-toolbar-edit group]
       [contact-list-toolbar group])]
    ;; todo add stub
    (when modal
      [modal-view action click-handler])
    [contacts-list-view group click-handler action edit?]]])
