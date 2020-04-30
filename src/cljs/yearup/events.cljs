(ns yearup.events
  (:require
    [re-frame.core :as rf]
    [ajax.core :as ajax]
    [reitit.frontend.easy :as rfe]
    [reitit.frontend.controllers :as rfc]))

;;dispatchers

(rf/reg-event-db
  :navigate
  (fn [db [_ match]]
    (let [old-match (:common/route db)
          new-match (assoc match :controllers
                                 (rfc/apply-controllers (:controllers old-match) match))]
      (assoc db :route new-match))))

(rf/reg-fx
  :navigate-fx!
  (fn [[k & [params query]]]
    (rfe/push-state k params query)))

(rf/reg-event-fx
  :navigate!
  (fn [_ [_ url-key params query]]
    {:navigate-fx! [url-key params query]}))

(rf/reg-event-db
  :set-quiz
  (fn [db [_ docs]]
    (assoc db :quiz docs)))

(rf/reg-event-fx
  :slide-one
  (fn [db _] (assoc db :hey (inc 1)))
  ;(fn [{:keys [db]} [_ id]]                                            ;(assoc db :hey id)
  ;  {:db (assoc db :hey id)})
  )

(rf/reg-event-fx
  :fetch-quiz
  (fn [_ _]
    {:http-xhrio {:method          :post
                  :uri             "/api/v1/question/list"
                  :params           {:quizId 1}
                  :format          (ajax/json-request-format)
                  :response-format (ajax/transit-response-format)
                  :on-success      [:set-quiz]}}))

;(rf/reg-event-fx
;  :fetch-cities
;  (fn [_ _]
;    {:http-xhrio {:method          :get
;                  :uri             "/api/v1/cities"
;                  :format          (ajax/json-request-format)
;                  :response-format (ajax/transit-response-format)
;                  :on-success      [:set-cities]}}))

(rf/reg-event-db
  :common/set-error
  (fn [db [_ error]]
    (assoc db :common/error error)))

;(rf/reg-event-fx
;  :quiz/start
;  (fn [_ _] ))

(rf/reg-event-fx
  :page/init-home
  (fn [_ _]
    {:dispatch [:fetch-quiz]}))

;;subscriptions

(rf/reg-sub
  :route
  (fn [db _]
    (-> db :route)))

(rf/reg-sub
  :page-id
  :<- [:route]
  (fn [route _]
    (-> route :data :name)))

(rf/reg-sub
  :page
  :<- [:route]
  (fn [route _]
    (-> route :data :view)))

(rf/reg-sub
  :quiz
  (fn [db _]
    (:quiz db)))

(rf/reg-sub
  :hey
  (fn [db _]
    (:hey db)))

(rf/reg-sub
  :common/error
  (fn [db _]
    (:common/error db)))
