(ns status-im.console
  (:require [clojure.test :refer :all]
            [status-im.appium :refer :all]))

; create password

(def short-password-text
  "Password should be not less then 6 symbols.")

(def password-missmatch-text
  "Password confirmation doesn't match password.")

(def creating-account-text
  "Gimmie a sec, I gotta do some crazy math to generate your account!")

(def created-account-text
  "Tap here to enter your phone number & I'll find your friends")

(def password
  "123456")

; verify phone

(def verify-phone-text
  (str "Thanks! We've sent you a text message with a confirmation "
       "code. Please provide that code to confirm your phone number"))

; confirmation code

(def wrong-code-text
  "Wrong confirmation code")

(def wrong-format-code-text
  "Wrong format")

(defaction create-account
  []
  (click :request-password)
  (write :input password)
  (click :send-message)
  (write :input password)
  (click :send-message)
  (click :send-message)
  (expect-text created-account-text)
  (click :navigate-back)
  (expect-text "Chats"))

(defaction send-sommand []
  (click :send-message)
  (click :send-message))

(defaction respond-to-request
  [request value]
  (click (keyword (str "request-" (name request))))
  (write :input value)
  (send-sommand))

(defaction test-command-input
  [input-id input-text action-id expected-text]
  (write input-id input-text)
  (click action-id)
  (expect-text expected-text))

(defaction create-account-test
  []
  (click :request-password)
  ; test for short password
  (test-command-input :input
                      "123"
                      :send-message
                      short-password-text)
  (write :input password)
  (click :send-message)
  ; test for password mismatch
  (test-command-input :input
                      "1234"
                      :send-message
                      password-missmatch-text)
  (write :input password)
  (click :send-message)
  (click :send-message)
  (expect-text creating-account-text)
  (expect-text created-account-text))

(defaction phone-request-test
  []
  (respond-to-request :phone "+380671111111")
  (expect-text verify-phone-text))

; TODO: test for expired and valid confirmation code(how?)
(defaction confirm-code-test
  []
  (click :request-confirmation-code)
  ; test for shorter  than 4 digits
  (test-command-input :input
                      "123"
                      :send-message
                      wrong-format-code-text)
  ; test for long code
  (test-command-input :input
                      "12345"
                      :send-message
                      wrong-format-code-text)
  ; test for non-digits code
  (test-command-input :input
                      "1aaa"
                      :send-message
                      wrong-format-code-text)
  (write :input "1234")
  (click :send-message)
  (click :send-message)
  (expect-text wrong-code-text))

; TODO: uncomment before push
(comment
  (appium-test console-test
    (create-account-test)
    (phone-request-test)
    (confirm-code-test)

    (click :navigate-back)
    (expect-text "Chats"))
  )