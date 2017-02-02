(ns status-im.test.contacts
  (:require [status-im.test.appium :refer [appium-test]]
            [status-im.test.actions :as act]
            [status-im.test.assertions :as assert]))

(def contact1 "Contact 1")
(def contact2 "Contact 2")
(def contact3 "Contact 3")

(appium-test create-two-accounts
  (act/create-two-accounts contact1 contact2)
  (assert/screen-contains-text contact1)
  (assert/screen-contains-text contact2))

;; Base sequence, no assertions (Add contacts)
;; Create account A.
;; Create account B and get its public key.
;; Log with account A.
;; Add contact B.
;; Switch to account B.
;; Add contact A.
;; Log out.

;; Case: Add contact
;; Run base sequence.
;; Log with account A.
;; Assert that B is on chat list.
;; Switch to account B.
;; Assert that A is on chat list.

;; Case: Remove contact
;; Run base sequence.
;; Log with account A.
;; Remove account B.
;; Assert that B is not on chat list.
;; Switch to account B.
;; Assert that A is no longer a contact (?)

;; Case: Send / Receive ETH.
;; Run base sequence.
;; Log with account A.
;; Get some ETH in the faucet.
;; Take note of the ETH balance in the wallet.
;; Use the send command to B, with some ETH.
;; Go through the transaction.
;; Assert that the ETH balance changed correctly.
;; Use the request command to B, with some ETH.
;; Switch to account B.
;; Take note of the ETH balance in the wallet.
;; Respond to the request command from A.
;; Go through the transaction.
;; Assert that the ETH balance changed correctly.

;; Case: Group chat
;; Run base sequence.
;; Create account C.
;; Take note of account C public key.
;; Switch to account A.
;; Add contact C.
;; Create a group chat with B and C.
;; Send message X.
;; Remove contact C from chat.
;; Send message Y.
;; Switch to account B.
;; Assert that group chat has messages X and Y.
;; Switch to account C.
;; Assert that group chat has message X, but not Y.
