(ns app.application
  (:require
    #?(:cljs [com.fulcrologic.fulcro.networking.http-remote :as net]
       :clj  [app.server-components.pathom :as parser])
    [com.fulcrologic.fulcro.application :as app]
    [com.fulcrologic.fulcro.algorithms.tx-processing :as txn]
    [edn-query-language.core :as eql]
    [taoensso.timbre :as log]))

#?(:cljs
   (def secured-request-middleware
     ;; The CSRF token is embedded via server_components/html.clj
     (->
       (net/wrap-csrf-token (or js/fulcro_network_csrf_token "TOKEN-NOT-IN-HTML!"))
       (net/wrap-fulcro-request))))

#?(:clj (def ssr-remote
          (let [parser parser/parser]
            {:transmit! (fn transmit! [_ {:keys [::txn/ast ::txn/result-handler ::txn/update-handler]}]
                          (let [edn           (eql/ast->query ast)
                                ok-handler    (fn [result]
                                                (try
                                                  (result-handler result)
                                                  (catch :default e
                                                    (log/error e "Result handler failed with an exception."))))
                                error-handler (fn [error-result]
                                                (try
                                                  (result-handler (merge error-result {:status-code 500}))
                                                  (catch :default e
                                                    (log/error e "Error handler failed with an exception."))))]
                            (let [result {}]
                              (ok-handler {:transaction edn
                                           :body        result
                                           :status-code 200}))))})))

(defonce SPA (app/fulcro-app
               #?(:clj  {:remotes {:remote ssr-remote}}
                  :cljs {;; This ensures your client can talk to a CSRF-protected server.
                         ;; See middleware.clj to see how the token is embedded into the HTML
                         :remotes {:remote (net/fulcro-http-remote
                                             {:url                "/api"
                                              :request-middleware secured-request-middleware})}})))

(comment
  (-> SPA (::app/runtime-atom) deref ::app/indexes))
