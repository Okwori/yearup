(ns yearup.events
  (:require
    [re-frame.core :as rf]
    [ajax.core :as ajax]
    [reitit.frontend.easy :as rfe]
    [reitit.frontend.controllers :as rfc]))

;;dispatchers

(rf/reg-event-db
  :common/navigate
  (fn [db [_ match]]
    (let [old-match (:common/route db)
          new-match (assoc match :controllers
                                 (rfc/apply-controllers (:controllers old-match) match))]
      (assoc db :common/route new-match))))

(rf/reg-fx
  :common/navigate-fx!
  (fn [[k & [params query]]]
    (rfe/push-state k params query)))

(rf/reg-event-fx
  :common/navigate!
  (fn [_ [_ url-key params query]]
    {:common/navigate-fx! [url-key params query]}))

(rf/reg-event-db
  :common/set-error
  (fn [db [_ error]]
    (if (contains? error :status-text)
      (do (if (= (:status-text error) "Server Error")
            (assoc db :common/error "You have already used this email")
            (assoc db :common/error (:status-text error))))
      (assoc db :common/error error))))

(rf/reg-event-db
  :set-quiz
  (fn [db [_ quiz]]
    (assoc db :quiz quiz)))

(rf/reg-event-fx
  :fetch-quiz
  (fn [_ _]
    {:http-xhrio {:method          :post
                  :uri             "/api/v1/question/list"
                  :params           {:quizId 1}
                  :format          (ajax/json-request-format)
                  :response-format (ajax/transit-response-format)
                  :on-success      [:set-quiz]
                  :on-failure      [:common/set-error]}} ))

(rf/reg-event-db
  :clear-exceptions
  (fn [db _]
    (dissoc db :common/error)))

(rf/reg-event-fx
  :next-slide
  (fn [{:keys [db]} [old-question new-question]]
    (if (not= old-question new-question)
      {:db (assoc-in db [:quiz :data :questions]
                     (filterv #(not= % new-question)
                              (get-in db [:quiz :data :questions])))
       :dispatch [:clear-exceptions]})))

(rf/reg-event-fx
  :submit
  (fn [{:keys [db]} [_ input]]
    {:db (cond (nil? input)
                db
               (contains? input :city-id)
                (assoc-in db [:answers] input)
               (and (= (count input) 1) (contains? input :id))
                (assoc-in db [:answers :user] {:user-id (:id input)
                                               :email   (get-in db [:answers :user :email])})
               (and (not (nil? input)) (contains? input (and :id :name :sentiment)))
                (assoc-in db [:answers :selections]
                          (conj (get-in db [:answers :selections])
                                (:id input))))
     :dispatch [:next-slide (first (get-in db [:quiz :data :questions]))]}))

(rf/reg-event-fx
  :create-user
  (fn [{:keys [db]} [_ email]]
    {:http-xhrio {:method          :post
                  :uri             "/api/v1/user/create"
                  :params          {:email (:email email)}
                  :format          (ajax/json-request-format)
                  :response-format (ajax/transit-response-format)
                  :on-success      [:submit]
                  :on-failure      [:common/set-error]}} ))

(rf/reg-event-fx
  :submit-user
  (fn [{:keys [db]} [_ email]]
    {:db       (assoc-in db [:answers :user] email)
     :dispatch [:create-user email]}))

(rf/reg-event-fx
  :finish
  (fn[{:keys [db]} [_ result]]
    {:db       (assoc-in db [:quiz :data :questions]
                         (conj (get-in db [:quiz :data :questions])
                               {:id              0
                                :detail          (cond (= (get-in result [:data :code]) 1)
                                                       "Seems like Year Up could be a great fit for you."
                                                       (= (get-in result [:data :code]) 2)
                                                       "Ok, now might not be the best time for you to start.")
                                :subNote         (cond (= (get-in result [:data :code]) 1)
                                                       "Our next class starts this fall, and you can apply today."
                                                       (= (get-in result [:data :code]) 2)
                                                       "Someone from Year Up would be happy to follow up and
                                                       answer any of your questions.")
                                :backgroundImage "learnMore.jpg"
                                :order           0
                                :options         [{:id        0
                                                   :name      (cond (= (get-in result [:data :code]) 1) "LEARN MORE"
                                                                    (= (get-in result [:data :code]) 2) "CONTACT ME")
                                                   :sentiment nil}]}))
     :dispatch [:next-slide (first (get-in db [:quiz :data :questions]))]} ))

(rf/reg-event-fx
  :create-answer
  (fn [{:keys [db]} _]
    {:http-xhrio {:method          :post
                  :uri             "/api/v1/submit"
                  :params          {:userId (get-in db [:answers :user :user-id])
                                    :selections (get-in db [:answers :selections])
                                    :cityId (get-in db [:answers :city-id])}
                  :format          (ajax/json-request-format)
                  :response-format (ajax/transit-response-format)
                  :on-success      [:finish]
                  :on-failure      [:common/set-error]}}))

(rf/reg-event-fx
  :push-answer
  (fn [_ _]
    {:dispatch [:create-answer]}))

(rf/reg-event-fx
  :submit-answers
  (fn [{:keys [db]} [_ option]]
    {:db (assoc-in db [:answers :selections] (conj (get-in db [:answers :selections]) (:id option)))
     :dispatch [:push-answer]}))

(rf/reg-event-fx
  :adjust-ratio
  (fn [_ [_ ratio]]
    {:http-xhrio {:method          :post
                  :uri             "/api/v1/update/ratio"
                  :params          {:r-ratio (js/parseInt (:r-ratio ratio))}
                  :format          (ajax/json-request-format)
                  :response-format (ajax/json-response-format)
                  :on-success      [:fetch-report]
                  :on-failure      [:common/set-error]}} ))

(rf/reg-event-fx
  :submit-ratio
  (fn [_ [_ ratio]]
    {:dispatch [:adjust-ratio ratio]}))

(rf/reg-event-db
  :update-city
  (fn [db [_ cities]]
    (assoc db :cities (get-in cities [:data :cities]))))

(rf/reg-event-fx
  :get-cities
  (fn [_ _]
    {:http-xhrio {:method          :get
                  :uri             "/api/v1/city/list"
                  :params          nil
                  :format          (ajax/json-request-format)
                  :response-format (ajax/transit-response-format)
                  :on-success      [:update-city]
                  :on-failure      [:common/set-error]}} ))

(rf/reg-event-fx
  :create-city
  (fn [_ [_ city]]
    {:http-xhrio {:method          :post
                  :uri             "/api/v1/city/create"
                  :params          {:name city}
                  :format          (ajax/json-request-format)
                  :response-format (ajax/json-response-format)
                  :on-success      [:get-cities]
                  :on-failure      [:common/set-error]}} ))

(rf/reg-event-fx
  :submit-city
  (fn [_ [_ city]]
    {:dispatch [:create-city (:r-city city)]}))

(rf/reg-event-db
  :question-response
  (fn [db [_ responses]]
    (assoc db :current-responses responses)))

(rf/reg-event-db
  :set-report
  (fn [db [_ report]]
    (assoc db :report report)))

(rf/reg-event-fx
  :fetch-report
  (fn [_ _]
    {:http-xhrio {:method          :post
                  :uri             "/api/v1/report"
                  :params          nil
                  :format          (ajax/json-request-format)
                  :response-format (ajax/transit-response-format)
                  :on-success      [:set-report]
                  :on-failure      [:common/set-error]}}))

(rf/reg-event-fx
  :page/init-home
  (fn [_ _]
      {:dispatch [:fetch-quiz]}))

(rf/reg-event-db
  :page/dispose
  (fn [db _]
    (dissoc db :current-responses)))

(rf/reg-event-fx
  :page/remove-home
  (fn [db _]
    {:db (dissoc db :common/route)}))

;(rf/reg-event-db
;  :common/set-error
;  (fn [db [_ error]]
;    (assoc db :common/error error)))

(rf/reg-event-fx
  :page/init-admin
  (fn [_ _]
    {:dispatch [:fetch-report]}))

;;subscriptions

(rf/reg-sub
  :common/route
  (fn [db _]
    (-> db :common/route)))

(rf/reg-sub
  :common/page-id
  :<- [:common/route]
  (fn [route _]
    (-> route :data :name)))

(rf/reg-sub
  :common/page
  :<- [:common/route]
  (fn [route _]
    (-> route :data :view)))

(rf/reg-sub
  :page/get-cities
  (fn [db _]
    (:cities db)))

(rf/reg-sub
  :answers
  (fn [db _]
    (:answers db)))

(rf/reg-sub
  :get-current-response
  (fn [db _]
    (:current-responses db)))

(rf/reg-sub
  :page/report
  (fn [db _]
    (:report db)))

(rf/reg-sub
  :quiz
  (fn [db _]
    (:quiz db)))

(rf/reg-sub
  :common/error
  (fn [db _]
    (:common/error db)))
