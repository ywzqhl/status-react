(ns status-im.recover
  (:require [clojure.test :refer :all]
            [status-im.appium :refer :all]
            [status-im.console :refer [create-account
                                       short-password-text
                                       password]]))

; add new contact

(def invalid-passphrase
  "Please enter a valid passphrase")

(defaction recover-test-account
  []
  (click :toolbar-left-action)
  (click :switch-users)
  (expect-text "Switch users")
  (click :recover-access)
  (expect-text "Recover from passphrase")
  (write :passphrase-input-field "absorb trim fever field coil speak firm soup pause skate bubble excuse")
  (write :password-input-field password)
  (click :recover-button)
  (expect-text "Switch users")
  (click-by-id "login-7c8c1f9adf9bdedc4ada462a8598e9e533dac123")
  (write :password-input-field password)
  (click :login-button)
  (expect-text "Chats"))

(defaction test-input-validation
  [input-id input-text expected-text]
  (write input-id input-text)
  (expect-text expected-text))

(defaction recover-account-test
  []
  ; test invalid passphrase
  (test-input-validation :passphrase-input-field
                         "123"
                         invalid-passphrase)
  ; recover button should be disabled
  (click :recover-button)
  (expect-text "Recover from passphrase")
  (write :passphrase-input-field "absorb trim fever field coil speak firm soup pause skate bubble excuse")
  ; test invalid password
  (test-input-validation :password-input-field
                         "123"
                         short-password-text)
  ; recover button should be disabled
  (click :recover-button)
  (expect-text "Recover from passphrase")
  (write :password-input-field password)
  (click :recover-button)
  (expect-text "Switch users"))

(comment
(appium-test contacts-test
  (create-account)
  (click :toolbar-left-action)
  (expect-text "Switch users")
  (click :switch-users)
  (click :recover-access)
  (expect-text "Recover from passphrase")
  (recover-account-test)))