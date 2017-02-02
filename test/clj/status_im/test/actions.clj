(ns status-im.test.actions
  (:require [status-im.test.appium :as ap :refer [defaction]]))

(defaction set-input-value [id value]
  (ap/clear id)
  (ap/write id value))

(defaction send-command []
  (ap/click :send-command))

(defaction respond-to-request [request value]
  (ap/click (keyword (str "request-" (name request))))
  (ap/write :input value)
  (send-command))

(defaction confirm-password [value]
  (ap/write :input value)
  (send-command))

(defaction change-username [username]
  (ap/click :toolbar-hamburguer-button)
  (ap/click :drawer-profile-button)
  (ap/click :profile-edit-button)
  (set-input-value :profile-username-input username)
  (ap/click :profile-save-button) ;; Click to un-focus input
  (ap/click :profile-save-button)
  (ap/click :profile-back-button))

(defaction create-account [username]
  (respond-to-request :password "password")
  (confirm-password "password")
  (ap/get-element-by-text "Find a bug or have a suggestion? Just shake your phone!")
  (ap/click :navigate-back)
  (change-username username))

(defaction logout []
  (ap/click :toolbar-hamburguer-button)
  (ap/click :switch-user-button))

(defaction create-two-accounts [contact1 contact2]
  (create-account contact1)
  (logout)
  (ap/click :create-account)
  (create-account contact2)
  (logout))

(defaction create-two-linked-accounts [contact1 contact1]
  (create-two-accounts contact1 contact2))
