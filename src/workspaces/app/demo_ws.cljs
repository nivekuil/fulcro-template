(ns app.demo-ws
  (:require [com.fulcrologic.fulcro.components :as fp]
            [nubank.workspaces.core :as ws]
            [nubank.workspaces.card-types.fulcro3 :as ct.fulcro]
            [com.fulcrologic.fulcro.mutations :as fm]
            [com.fulcrologic.fulcro.dom :as dom]
            [com.fulcrologic.fulcro.dom.events :as evt]))

(fp/defsc Form
  [this {:keys [input]}]
  {:ident         [:form/by-id :form/id]
   :query         [:input :form/id]
   :initial-state {:input "foo"}}
  (dom/form
   {:onSubmit (fn [e] (evt/prevent-default! e))}
   (dom/input {:value    input
               :onChange #(fm/set-value!! this :input (-> % .-target .-value))})))
(def ui-form (fp/factory Form))

(fp/defsc FulcroDemo
  [this {:keys [form]}]
  {:initial-state (fn [_] {:form (fp/get-initial-state Form)})
   :ident         (fn [] [::id "singleton"])
   :query         [:form]}
  (ui-form (assoc form :form/id :my-form)))

(ws/defcard fulcro-demo-card
  (ct.fulcro/fulcro-card
   {::ct.fulcro/root       FulcroDemo
    ::ct.fulcro/wrap-root? true}))
