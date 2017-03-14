(ns status-im.chat.styles.input.input
  (:require [status-im.components.styles :as common]))

(def color-root-border "rgba(192, 198, 202, 0.28)")
(def color-input "#edf1f3")
(def color-input-helper-text "rgb(182, 189, 194)")
(def color-command "#70777d")

(def max-input-height 66)
(def min-input-height 38)
(def input-spacing-top 5)
(def input-spacing-bottom 8)

(defn root [margin-bottom]
  {:border-top-color color-root-border
   :border-top-width 1
   :flex-direction   :column
   :margin-bottom    margin-bottom})

(defn container [command?]
  {:background-color common/color-white
   :flex-direction   :column
   :padding          (if command? 8 16)})

(defn input-root [content-height command?]
  {:align-items      :center
   :background-color color-input
   :border-radius    8
   :flex-direction   :row
   :flex-grow        1
   :height           (+ (min (max min-input-height content-height) max-input-height) 0)
   :margin-top       (if command? 0 16)
   :padding-left     10
   :padding-right    10})

(def input-view
  {:flex             1
   :font-size        14
   :padding-top      input-spacing-top
   :padding-bottom   input-spacing-bottom
   :line-height      20})

(def invisible-input-text
  {:font-size        14
   :position         :absolute
   :left             0
   :background-color :transparent
   :color            :transparent})

(defn input-helper-text [left]
  {:color       color-input-helper-text
   :font-size   14
   :position    :absolute
   :height      min-input-height
   :line-height min-input-height
   :left        (+ 14 left)})

(def input-emoji-icon
  {:height 20
   :width  20})

(def commands-root
  {:flex-direction :row
   :align-items    :center})

(def commands-list-icon
  {:height        13
   :margin-left   5
   :margin-right  5
   :margin-top    4
   :margin-bottom 4
   :width         16})

(def commands
  {:flex-direction :row
   :margin-left    16})

(def command
  {:color        color-command
   :font-size    14
   :margin-right 16})
