(ns status-im.components.toolbar.actions
  (:require [status-im.components.toolbar.styles :as st]))

(def nothing
  {:image {:source nil
           :style  st/action-search}})

(defn hamburger [handler]
  {:image               {:source {:uri :icon_hamburger}
                         :style  st/action-hamburger}
   :handler             handler
   :accessibility-label :toolbar-hamburguer-button})

(defn add [handler]
  {:image               {:source {:uri :icon_add}
                         :style  st/action-add}
   :handler             handler
   :accessibility-label :toolbar-add-button})

(defn search [handler]
  {:image               {:source {:uri :icon_search}
                         :style  st/action-search}
   :handler             handler
   :accessibility-label :toolbar-search-button})

(defn back [handler]
  {:image               {:source {:uri :icon_back}
                         :style  st/action-back}
   :handler             handler
   :accessibility-label :toolbar-back-button})

(defn back-white [handler]
  {:image               {:source {:uri :icon_back_white}
                         :style  st/action-back}
   :handler             handler
   :accessibility-label :toolbar-back-button})
