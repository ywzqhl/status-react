(ns status-im.contacts
  (:require [clojure.test :refer :all]
            [status-im.appium :refer :all]
            [status-im.console :refer [create-account]]))

; add new contact

(def invalid-contact
  "Please enter a valid address or scan a QR code")

(def unknown-address
  "Unknown address")

(defaction test-input-validation
  [input-id input-text expected-text]
  (write input-id input-text)
  (expect-text expected-text))

; TODO: test known address; test scan QR(how??)
(defaction add-new-contact-test
  []
  ; test invalid contact identity
  (write :contact-input-field "123")
  (expect-text invalid-contact)
  ; add contact button should be disabled
  (click :toolbar-right-action)
  (expect-text "Add new contact")
  ; test unknown address
  (write :contact-input-field "1234567890123456789012345678901234567890")
  (click :toolbar-right-action)
  (expect-text unknown-address)
  ; add contact button should be disabled
  (click :toolbar-right-action)
  (expect-text "Add new contact")
  ; test valid-length public key
  (write :contact-input-field "1234567890123456789012345678901234567890123456789012345678901234567890123456789012345678901234567890123456789012345678901234567890")
  (click :toolbar-right-action)
  (expect-text "Unknown")
  (click :navigate-back)
  (click :contact-list-tab))

; TODO: implement tests once this functionality is implemented
(defaction search-contacts-test
  []
  (comment))

; TODO: implement tests once this functionality is implemented
(defaction remove-contact-test
  []
  (comment))

(appium-test contacts-test
  (create-account)
  (click :contact-list-tab)
  (expect-text "Contacts")
  (click-by-text "+")
  (click :contact-add-button)
  (expect-text "Add new contact")
  (add-new-contact-test)
  (search-contacts-test))