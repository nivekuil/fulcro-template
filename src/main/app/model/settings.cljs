(ns app.model.settings
  (:require
    [com.fulcrologic.fulcro.mutations :refer [defmutation]]))

(defmutation save-settings [_]
  (remote [_] true))
