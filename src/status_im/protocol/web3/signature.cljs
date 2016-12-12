(ns status-im.protocol.web3.signature)

(def elliptic-ec (.-ec (js/require "elliptic")))
(def secp (new elliptic-ec "secp256k1"))

(defn sign [{:keys [web3 address message callback]}]
  (when address
    (let [to-sign         (->> (.toString message)
                               (.sha3 web3))
          unlock-callback (fn [error res]
                            (if res
                              (.sign (.-eth web3) address to-sign callback)
                              (callback :unlock-error nil)))]
      (.unlockAccount (.-personal web3) address "testpass" 1000 unlock-callback))))

(defn signature-valid? [message public-key signature]
  (.log js/console "ALWX SIGN!" (count public-key) #_(.keyFromPublic secp public-key "hex"))
  true)
