(ns status-im.commands.send
  (:require [clojure.test :refer :all]
            [status-im.appium :refer :all]
            [status-im.console :refer [create-account
                                       password]]
            [status-im.recover :refer [recover-test-account]]
            [status-im.contacts :refer [add-contact]]))

(appium-test send-command-test
  (create-account)
  (recover-test-account)
  (add-contact "0x04d4b4b2685092f4408bf82917daed6381cdaff04cf5d16aa59a8070a91f2e952210522178adb52b69b8daacc5aaf250bd92bcf22f2af5a077bc09bd7e50817a46")

  ; test empty amount
  (write :input "!s")
  (expect-text "!send")
  (click :send-message)
  (expect-element :missing-amount-error)
  ; test balance not enough
  (write :input "!s                                         1000000")
  (expect-text "!send")
  (click :send-message)
  (expect-element :balance-not-enough-error)
  ; test invalid amount
  (write :input "!s                                            aaa")
  (expect-text "!send")
  (click :send-message)
  (expect-element :invalid-amount-error)

  (write :input "!s                                             0.01")
  (click :send-message)
  (expect-element :staged-command-message-send)
  (click :send-message)
  (expect-text "Confirm transaction")

  (write :password-input-field password)
  (click :toolbar-right-action)
  (expect-element :command-message-send))