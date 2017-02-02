(ns status-im.test.assertions
  (:require [status-im.test.appium :as ap]
            [clojure.test :refer [is]]))

(defn screen-contains-text [driver text]
  (is (pos? (->> (ap/xpath-by-text text)
                 (ap/elements-by-xpath driver)
                 (.size)))
      (format "Text \"%s\" was not found on screen." text)))
