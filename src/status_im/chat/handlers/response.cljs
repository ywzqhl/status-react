(ns status-im.chat.handlers.response
  (:require [re-frame.core :refer [after dispatch subscribe debug path]]
            [status-im.utils.handlers :refer [register-handler] :as u]
            [status-im.utils.platform :refer [platform-specific]]
            [status-im.constants :refer [response-input-hiding-duration]]
            [taoensso.timbre :as log]
            [status-im.chat.constants :as c]
            [status-im.components.animation :as anim]))

(register-handler :clear-response-suggestions
  (after (fn [_ [_ chat-id]]
           (dispatch [:check-response-height-changed :response-suggestions-height 0 chat-id])))
  (fn [db [_ chat-id]]
    (-> db
        (update-in [:suggestions] dissoc chat-id)
        (update-in [:has-suggestions?] dissoc chat-id))))

(register-handler :animate-to-response-height
  (u/side-effect!
    (fn [db [_ chat-id height]]
      (let [response-height-animation (get-in db [:animations chat-id :response-height-animation])
            response-height           (get-in db [:animations chat-id :current-response-height])]
        (log/debug ">>>>>>>>>>>>>:animate-to-response-height" response-height height)
        (when (and response-height-animation
                   (not= (int response-height) (int height)))
          (anim/start (anim/timing response-height-animation {:toValue  height
                                                              :duration 300})))))))

(register-handler :set-response-height
  (after
    (fn [db [_ chat-id height]]
      (log/debug "Animating response height: " height)
      (dispatch [:animate-to-response-height chat-id height])))
  (fn [db [_ chat-id height]]
    (log/debug "Setting response height: " height)
    (assoc-in db [:animations chat-id :response-height] height)))

(register-handler :update-response-height
  (u/side-effect!
    (fn [{:keys [current-chat-id] :as db} [_ chat-id force?]]
      (let [chat-id             (or chat-id current-chat-id)
            fullscreen?         (get-in db [:chats chat-id :command-input :command :fullscreen])
            response-height     (get-in db [:animations chat-id :response-height])
            suggestions-height  (get-in db [:animations chat-id :response-suggestions-height])
            request-info-height (get-in db [:animations chat-id :response-request-info-height])
            validation-height   (get-in db [:animations chat-id :response-validation-messages-height])
            max-height          (get-in db [:layout-height])
            height              (+ suggestions-height
                                   request-info-height
                                   validation-height)
            height              (int (if fullscreen?
                                       max-height
                                       (min
                                         (+ (if (> height c/suggestions-header-height)
                                              height
                                              0)
                                            c/input-height)
                                         (/ max-height 2))))]
        (when (or force?
                  (not= response-height height))
          (dispatch [:set-response-height chat-id height]))))))

(register-handler :set-current-response-height
  (fn [db [_ chat-id height]]
    (log/debug "Setting current response height: " height)
    (assoc-in db [:animations chat-id :current-response-height] height)))

(register-handler :check-current-response-height-change
  (u/side-effect!
    (fn [db [_ chat-id height]]
      (let [current-height    (get-in db [:animations chat-id :current-response-height])
            fullscreen?       (get-in db [:chats chat-id :command-input :command :fullscreen])
            layout-height     (:layout-height db)
            status-bar-height (get-in platform-specific [:component-styles :status-bar :default :height])
            toolbar-height    (get-in platform-specific [:component-styles :toolbar-nav-action :height])
            height            (if (> layout-height 0)
                                (min height
                                     (- layout-height
                                        status-bar-height
                                        (if fullscreen?
                                          0
                                          toolbar-height)))
                                height)]
        ;(log/debug ":check-current-response-height-change " layout-height current-height height)
        (when (not= current-height height)
          (dispatch [:set-current-response-height chat-id height])
          (dispatch [:update-margin-bottom chat-id]))))))

(register-handler :create-response-height-animation
  (after
    (fn [db [_ chat-id]]
      (let [response-height (get-in db [:animations chat-id :response-height-animation])]
        (anim/remove-all-listeners response-height)
        (anim/add-listener response-height
                           (fn [value]
                             ;(log/debug ">>>>>>>>>>>>>>>>>>>>>>>>>>>>>>Response height changed: " (.-value value))
                             (dispatch [:check-current-response-height-change chat-id (Math/floor (.-value value))]))))))
  (fn [db [_ chat-id]]
    (let [response-height (get-in db [:animations chat-id :response-height-animation])]
      (if (nil? response-height)
        (assoc-in db [:animations chat-id :response-height-animation] (anim/create-value 0))
        db))))

(register-handler :response-height-changed
  (fn [{:keys [current-chat-id] :as db} [_ key-height new-height chat-id]]
    (let [chat-id (or chat-id current-chat-id)]
      (log/debug "Response height changed: " key-height new-height)
      (assoc-in db [:animations chat-id key-height] new-height))))

(register-handler :check-response-height-changed
  (u/side-effect!
    (fn [{:keys [current-chat-id] :as db} [_ key-height new-height chat-id]]
      (let [chat-id        (or chat-id current-chat-id)
            current-height (get-in db [:animations chat-id key-height])]
        (log/debug "Check height changed: " key-height current-height new-height)
        (when (not= current-height new-height)
          (dispatch [:response-height-changed key-height new-height chat-id])
          (dispatch [:update-response-height chat-id]))))))

(defn get-minimum-height
  [{:keys [current-chat-id] :as db}]
  (let [path               [:chats current-chat-id :command-input :command :type]
        type               (get-in db path)
        command?           (= :command type)
        response?          (not command?)
        errors             (get-in db [:validation-errors current-chat-id])
        validation-errors? (seq errors)
        suggestion?        (get-in db [:has-suggestions? current-chat-id])
        custom-errors      (get-in db [:custom-validation-errors current-chat-id])
        custom-errors?     (seq custom-errors)
        validation-errors? (or validation-errors? custom-errors?)]
    (cond-> 0
            validation-errors? (+ c/request-info-height)
            response? (+ c/minimum-suggestion-height)
            command? (+ c/input-height)
            (and suggestion? command?) (+ c/suggestions-header-height)
            ;custom-errors? (+ suggestions-header-height)
            (and command? validation-errors?) (+ c/suggestions-header-height))))

(register-handler :release-pan-responder
  (u/side-effect!
    (fn [{:keys [current-chat-id] :as db} [_ vy]]
      (let [current-height         (get-in db [:animations current-chat-id :current-response-height])
            moving-down?           (pos? vy)
            moving-up?             (not moving-down?)
            max-height             (- (get-in db [:layout-height])
                                      (get-in platform-specific [:component-styles :status-bar :default :height]))
            middle-height          (/ max-height 2)
            under-middle-position? (<= current-height middle-height)
            over-middle-position?  (not under-middle-position?)
            suggestions?            (get-in db [:has-suggestions? current-chat-id])
            height                 (cond
                                     (not suggestions?)
                                     (get-minimum-height db)

                                     (and under-middle-position? moving-up?)
                                     middle-height

                                     (and over-middle-position? moving-down?)
                                     middle-height

                                     (and over-middle-position? moving-up?)
                                     max-height

                                     (and under-middle-position? moving-down?)
                                     (get-minimum-height db)

                                     :else nil)]
        (log/debug ":release-pan-responder: " current-height height)
        (when height
          (dispatch [:set-response-height current-chat-id height]))))))