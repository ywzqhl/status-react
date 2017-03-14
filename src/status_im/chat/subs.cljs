(ns status-im.chat.subs
  (:require-macros [reagent.ratom :refer [reaction]])
  (:require [re-frame.core :refer [register-sub dispatch subscribe path]]
            [status-im.data-store.chats :as chats]
            [status-im.chat.constants :as const]
            [status-im.chat.utils :as chat-utils]
            [status-im.constants :refer [response-suggesstion-resize-duration
                                         content-type-status
                                         console-chat-id]]
            [status-im.models.commands :as commands]
            [status-im.utils.platform :refer [platform-specific ios?]]
            [taoensso.timbre :as log]
            [clojure.string :as str]))

(register-sub
  :chat-properties
  (fn [db [_ properties]]
    (->> properties
         (map (fn [k]
                [k (-> @db
                       (get-in [:chats (:current-chat-id @db) k])
                       (reaction))]))
         (into {}))))


(register-sub
  :chat-ui-props
  (fn [db [_ ui-element]]
    (reaction (get-in @db [:chat-ui-props ui-element]))))

(register-sub
  :chat-input-margin
  (fn []
    (let [kb-height (subscribe [:get :keyboard-height])]
      (reaction
        (if ios? @kb-height 0)))))

(register-sub
  :chat
  (fn [db [_ k chat-id]]
    (-> @db
        (get-in [:chats (or chat-id (:current-chat-id @db)) k])
        (reaction))))

(register-sub
  :get-current-chat-id
  (fn [db _]
    (reaction (:current-chat-id @db))))

(register-sub
  :get-chat-by-id
  (fn [_ [_ chat-id]]
    (reaction (chats/get-by-id chat-id))))

(register-sub
  :get-commands
  (fn [db [_ chat-id]]
    (let [current-chat (or chat-id (@db :current-chat-id))]
      (reaction (or (get-in @db [:chats current-chat :commands]) {})))))

(register-sub
  :get-responses
  (fn [db [_ chat-id]]
    (let [current-chat (or chat-id (@db :current-chat-id))]
      (reaction (or (get-in @db [:chats current-chat :responses]) {})))))

(register-sub
  :possible-chat-actions
  (fn [{:keys [current-chat-id] :as db} [_ chat-id]]
    "Returns a vector of [command message-id] values. `message-id` can be `:any`.
     Example: [[browse-command :any] [debug-command :any] [phone-command '1489161286111-58a2cd...']]"
    (let [current-chat-id (or chat-id (@db :current-chat-id))
          requests (subscribe [:chat :requests])
          commands (subscribe [:chat :commands])]
      (reaction
        (let [commands  (mapv (fn [[_ command]]
                                (vector command :any))
                              @commands)
              responses (mapv (fn [{:keys [message-id type]}]
                                (vector
                                  (get-in @db [:chats current-chat-id :responses type])
                                  message-id))
                              @requests)]
          (into commands responses))))))

(register-sub
  :selected-chat-command
  (fn [{:keys [current-chat-id] :as db} [_ chat-id]]
    (let [current-chat-id  (or chat-id (@db :current-chat-id))
          possible-actions (subscribe [:possible-chat-actions current-chat-id])]
      (reaction
        (let [command-args (-> (get-in @db [:chats current-chat-id :input-text])
                               (str/split const/spacing-char))
              command-name (first command-args)]
          (when (.startsWith command-name const/command-char)
            (when-let [command (-> (filter (fn [[{:keys [name]} message-id]]
                                             (and (= name (subs command-name 1))
                                                  (= message-id :any)))
                                           @possible-actions)
                                   (ffirst))]
              {:command command
               :args    (rest command-args)})))))))











(register-sub :valid-plain-message?
  (fn [_ _]
    (let [input-message (subscribe [:chat :input-text])]
      (reaction
        (and (pos? (count @input-message))
             (not= const/command-char @input-message))))))

(register-sub :valid-command?
  (fn [_ [_ validator]]
    (let [input (subscribe [:get-chat-command-content])]
      (reaction (chat-utils/command-valid? @input validator)))))

(register-sub :get-chat-command
  (fn [db _]
    (reaction (commands/get-chat-command @db))))

(register-sub :get-command-parameter
  (fn [db]
    (let [command (subscribe [:get-chat-command])
          chat-id (subscribe [:get-current-chat-id])]
      (reaction
        (let [parameter-index (commands/get-command-parameter-index @db @chat-id)]
          (when parameter-index (nth (:params @command) parameter-index)))))))

(register-sub :get-chat-command-content
  (fn [db _]
    (reaction (commands/get-chat-command-content @db))))

(register-sub :get-chat-command-to-message-id
  (fn [db _]
    (reaction (commands/get-chat-command-to-message-id @db))))

(register-sub :chat-command-request
  (fn [db _]
    (reaction (commands/get-chat-command-request @db))))

(register-sub :get-current-chat
  (fn [db _]
    (let [current-chat-id (:current-chat-id @db)]
      (reaction (get-in @db [:chats current-chat-id])))))

(register-sub :get-chat
  (fn [db [_ chat-id]]
    (reaction (get-in @db [:chats chat-id]))))

(register-sub :get-content-suggestions
  (fn [db _]
    (reaction (get-in @db [:suggestions (:current-chat-id @db)]))))

(register-sub :command?
  (fn [db]
    (->> (get-in @db [:edit-mode (:current-chat-id @db)])
         (= :command)
         (reaction))))

(register-sub :command-type
  (fn []
    (let [command (subscribe [:get-chat-command])]
      (reaction (:type @command)))))

(register-sub :messages-offset
  (fn []
    (let [command?            (subscribe [:command?])
          type                (subscribe [:command-type])
          command-suggestions (subscribe [:get-content-suggestions])]
      (reaction
        (cond (and @command? (= @type :response))
              const/request-info-height

              (and @command? (= @type :command) (seq @command-suggestions))
              const/suggestions-header-height

              :else 0)))))

(register-sub :command-icon-width
  (fn []
    (let [width (subscribe [:get :command-icon-width])
          type  (subscribe [:command-type])]
      (reaction (if (= :command @type)
                  @width
                  0)))))

(register-sub :get-requests-map
  (fn [db]
    (let [chat-id (subscribe [:get-current-chat-id])]
      (reaction (->> (get-in @db [:chats @chat-id :requests])
                     (map #(vector (:message-id %) %))
                     (into {}))))))

(register-sub :get-request
  (fn [_ [_ message-id]]
    (let [requests (subscribe [:get-requests-map])]
      (reaction (get @requests message-id)))))

(register-sub :get-current-request
  (fn []
    (let [requests   (subscribe [:get-requests-map])
          message-id (subscribe [:get-chat-command-to-message-id])]
      (reaction (@requests @message-id)))))

(register-sub :get-response
  (fn [db [_ n]]
    (let [chat-id (subscribe [:get-current-chat-id])]
      (reaction (get-in @db [:chats @chat-id :responses n])))))

(register-sub :is-request-answered?
  (fn [_ [_ message-id]]
    (let [requests (subscribe [:chat :requests])]
      (reaction (not-any? #(= message-id (:message-id %)) @requests)))))

(register-sub :validation-errors
  (fn [db]
    (let [current-chat-id (subscribe [:get-current-chat-id])]
      (reaction (get-in @db [:validation-errors @current-chat-id])))))

(register-sub :custom-validation-errors
  (fn [db]
    (let [current-chat-id (subscribe [:get-current-chat-id])]
      (reaction (get-in @db [:custom-validation-errors @current-chat-id])))))

(register-sub :unviewed-messages-count
  (fn [db [_ chat-id]]
    (reaction (get-in @db [:unviewed-messages chat-id :count]))))

(register-sub :command-suggestions-height
  (fn [db]
    (let [chat-id (subscribe [:get-current-chat-id])]
      (reaction
        (get-in @db [:animations :command-suggestions-height @chat-id])))))

(register-sub :response-height
  (fn [db [_ status-bar]]
    (let [chat-id (subscribe [:get-current-chat-id])]
      (reaction
        (min (get-in @db [:animations :to-response-height @chat-id])
             (if (pos? (:layout-height @db))
               (- (:layout-height @db)
                  (get-in platform-specific [:component-styles :status-bar status-bar :height]))
               0))))))

(register-sub :web-view-url
  (fn [db]
    (let [chat-id (subscribe [:get-current-chat-id])]
      (reaction (get-in @db [:web-view-url @chat-id])))))

(register-sub :web-view-extra-js
  (fn [db]
    (let [chat-id (subscribe [:get-current-chat-id])]
      (reaction (get-in @db [:web-view-extra-js @chat-id])))))

(register-sub :animate?
  (fn [db]
    (let [chat-id (subscribe [:get-current-chat-id])]
      (reaction (get-in @db [:animate? @chat-id])))))

(register-sub :kb-mode
  (fn [db]
    (let [chat-id (subscribe [:get-current-chat-id])]
      (reaction (get-in @db [:kb-mode @chat-id])))))

(register-sub :max-layout-height
  (fn [_ [_ status-bar]]
    (let [layout-height     (subscribe [:get :layout-height])
          input-margin      (subscribe [:input-margin])
          status-bar-height (get-in platform-specific [:component-styles :status-bar status-bar :height])]
      (reaction
        (- @layout-height @input-margin status-bar-height)))))

(register-sub :all-messages-loaded?
  (fn [db]
    (let [chat-id (subscribe [:get-current-chat-id])]
      (reaction (get-in @db [:chats @chat-id :all-loaded?])))))

(register-sub :photo-path
  (fn [_ [_ id]]
    (let [contacts (subscribe [:get :contacts])]
      (reaction (:photo-path (@contacts id))))))

(register-sub :get-last-message
  (fn [db [_ chat-id]]
    (reaction
      (let [{:keys [last-message messages]} (get-in @db [:chats chat-id])]
        (first
          (sort-by :clock-value > (conj messages last-message)))))))

(register-sub :get-last-message-short-preview
  (fn [db [_ chat-id]]
    (let [last-message (subscribe [:get-last-message chat-id])]
      (reaction
        (get-in @db [:message-data :short-preview (:message-id @last-message)])))))
