(ns yearup.core
  (:require
    [cljs.spec.alpha :as s]
    [clojure.string :as string]
    [day8.re-frame.http-fx]
    [goog.events :as events]
    [goog.history.EventType :as HistoryEventType]
    [markdown.core :refer [md->html]]
    [reagent.core :as r]
    [reagent.dom :as rdom]
    [reitit.core :as reitit]
    [reitit.frontend.easy :as rfe]
    [reitit.coercion.spec :as rss]
    [re-frame.core :as rf]
    [yearup.ajax :as ajax]
    [yearup.events])
  (:import goog.History))

(s/def ::email (s/and string? (partial re-matches #"^[a-zA-Z0-9._%+-]+@[a-zA-Z0-9.-]+\.[a-zA-Z]{2,63}$")))

(defn home-page []
  (when-let [quiz @(rf/subscribe [:quiz])]
    (let [question (first (get-in quiz [:data :questions]))
          cities (get-in quiz [:data :cities])
          order (:order question)
          answer @(rf/subscribe [:answers])]
      [:div [:div#wrapper [:img.yearuplogo {:src "img/YU_goldwhiteCC.png" :alt "logo"}]
            [:header#header
             [:div.logo
              [:img.logo.icon {:src "img/Y-only-gold.png" :alt "logo-icon"}]]
             [:div.content
              [:div.inner
               (if (= order 13)
                 (do (when-let [city-id (get-in answer [:city-id])]
                       (let [city (filter #(= (:id %) city-id) cities)]
                         [:h1 [:strong (:question (first city))]])))
                 [:h1 [:strong (:detail question)]])
               (if (or (= order 12) (= order 13)) (do [:br][:br]) (do [:br][:br][:br]))
               (if (not= order 1)
                 [:h3 (:subNote question)])]
              (when-let [error @(rf/subscribe [:common/error])]
                [:h4 {:style {:color "red"}} (str error)])]
             [:nav
              [:ul (cond (= order 1)
                         [:select {:name "city-drop-down" :id "city-drop-down"
                                               :on-change #(rf/dispatch [:submit {:city-id (js/parseInt (-> % .-target .-value))}])}
                                      [:option "Select the CITY closest to you"]
                         (for [city cities]
                             ^{:key (:id city)} [:option {:value (:id city)} (:name city)])]

                         (= order 2)
                         [:li [:a {:style {:background-color "#AAA4A2"} :on-click #(rf/dispatch [:submit])} (:name (first (:options question)))]]

                         (= order 3)
                         (let [vals-email (r/atom {:email ""})]
                           [:li [:div.fields
                                [:div.field.half
                                 [:input#user-email {:type        "email" :name "user-email" :required "required"
                                                     :placeholder "email@example.com"
                                                     :on-key-down #(if (= (.-key %) "Enter") ;; TODO remove side pass
                                                                     (if (or (s/valid? ::email (:email @vals-email))
                                                                             (= "yu" (string/lower-case (string/trim (:email @vals-email))))
                                                                             (= "ibuwembo@yearup.org" (string/lower-case (string/trim (:email @vals-email)))))
                                                                       (rf/dispatch [:submit-user @vals-email])
                                                                       (rf/dispatch [:common/set-error "Enter a valid email address"])))
                                                     :on-change   #(swap! vals-email assoc :email (-> % .-target .-value))}]]
                                [:a.button.primary {:on-click #(if (or (s/valid? ::email (:email @vals-email))
                                                                       (= "yu" (string/lower-case (string/trim (:email @vals-email))))
                                                                       (= "ibuwembo@yearup.org" (string/lower-case (string/trim (:email @vals-email)))))
                                                                 (rf/dispatch [:submit-user @vals-email])
                                                                 (rf/dispatch [:common/set-error "Enter a valid email address"]))} "Submit"]]])

                         (->> (into [] (range 4 14))  (some #(= order %)))
                         (for [option (:options question)]
                           ^{:key (:id option)}[:li [:a.button {:style    (cond (= (:sentiment option) 0) {:background-color "#E11D0D"}
                                                            (string/includes? (:name option) "Not sure") {:background-color "#AAA4A2"}
                                                            (= option (first (:options question))) {:background-color "#27760D"})
                                            :on-click (if (= order 13 )
                                                        #(rf/dispatch [:submit-answers option])
                                                        #(rf/dispatch [:submit option]))}
                                 (:name option)]])

                         (= order 0) [:li [:a.button {:href "https://www.yearup.org/seize-opportunity/"
                                                      :style {:background-color "#AAA4A2"}}
                                           (:name (first (:options question)))]])]]]
             (if (and (= order 0) (string/includes? (:detail question) "Ok, now might")) [:a {:href "https://www.yearup.org/seize-opportunity/"} "Wait, I still want to learn more about Year Up"])
             [:footer#footer
              [:p.copyright "© 2020 YearUp"]]]
       [:div#bg {:style {:background-position "center" :background-size "cover" :background-repeat "no-repeat" :z-index "1"
                         :background-image
                                                   (if (= order 13)
                                                     (do (when-let [city-id (get-in answer [:city-id])]
                                                           (let [city (filter #(= (:id %) city-id) cities)]
                                                             (str "url('/api/v1/files/download/"
                                                                  (:city-id (first city))"')"))))
                                                     (str "url('img/" (:backgroundImage question) "')"))}}]])))

(defn page []
  (if-let [page @(rf/subscribe [:common/page])]
    [:div
     [page]]))

(defn navigate! [match _]
  (rf/dispatch [:common/navigate match]))

(def router
  (reitit/router
    [["/" {:name        :home
           :view        #'home-page
           :controllers [{:start (fn [_] (rf/dispatch [:page/init-home]))}]}]]))

(defn start-router! []
  (rfe/start!
    router
    navigate!
    {}))

;; -------------------------
;; Initialize app
(defn mount-components []
  (rf/clear-subscription-cache!)
  (r/render [#'page] (.getElementById js/document "app")))

(defn init! []
  (start-router!)
  (ajax/load-interceptors!)
  (mount-components))
