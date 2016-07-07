(ns status-im.transactions.handlers
  (:require [re-frame.core :refer [after dispatch debug enrich]]
            [status-im.utils.handlers :refer [register-handler]]
            [status-im.navigation.handlers :as nav]
            [status-im.utils.handlers :as u]))


(register-handler :accept-transactions
  (fn [db [_ transactions password]]
    db))

(register-handler :deny-transactions
  (fn [db [_ transactions]]
    db))