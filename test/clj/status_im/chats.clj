(ns status-im.chats
  (:require [clojure.test :refer :all]
            [status-im.appium :refer :all]
            [status-im.console :refer [create-account]]))


; add new chat

(def invalid-contact
  "Please enter a valid address or scan a QR code")

(def unknown-address
  "Unknown address")

(defaction test-input-validation
  [input-id input-text expected-text]
  (write input-id input-text)
  (expect-text expected-text))

(defaction navigate-to-new-chat
  []
  (click-by-text "+")
  (expect-text "New chat")
  (click :chat-add-button)
  (expect-text "Start new chat"))

(defaction add-new-chat-test
  []
  ; TODO: implement search test
  (click :navigate-back))

(defaction navigate-to-new-group-chat
  []
  (click-by-text "+")
  (expect-text "New group chat")
  (click :group-chat-add-button)
  (expect-text "Please enter a name"))

(defn select-new-group-contact
  [driver]
  (let [contact-list (by-class-name driver "android.widget.ScrollView")
        _ (println contact-list)
        checkboxes (elements-by-class-name contact-list "android.view.ViewGroup")]
    (.click (nth checkboxes 0))
    (.click (nth checkboxes 1))))

(defaction add-new-group-chat-test
  []
  ; add group chat button should be disabled
  (click :toolbar-right-action)
  (expect-text "New group chat")
  ; test 1 member group chat
  (write :chat-name-input "1 member group chat")
  (click :toolbar-right-action)
  (expect-text "1 member group chat")
  (expect-text "1 member, 1 active")
  (click :navigate-back)
  (navigate-to-new-group-chat)
  ; test 2 member group chat
  (write :chat-name-input "2 members group chat")
  (select-new-group-contact)
  (click :toolbar-right-action)
  (expect-text "2 members group chat")
  (expect-text "2 members, 2 active")
  (click :navigate-back))

; TODO: implement tests once this functionality is implemented
(defaction search-chats-test
  []
  (comment))


(comment
  (appium-test chats-test
    (create-account)
    (navigate-to-new-chat)
    (add-new-chat-test)
    (navigate-to-new-group-chat)
    (add-new-group-chat-test)
    (search-chats-test))
  )