(ns status-im.chat.styles.input.input
  (:require [status-im.components.styles :as common]))

(def color-input "#EDF1F3")

(def root
  {:flex-direction :column})

(def container
  {:flex-direction   :row
   :align-items      :center
   :justify-content  :center
   :height           40
   :background-color common/color-white
   :margin           16})

(def input-root
  {:flex             1
   :padding          0
   :font-size        14
   :line-height      20
   :background-color color-input})

(def input-view
  {})
