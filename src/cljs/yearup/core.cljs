(ns yearup.core
  (:require
    [day8.re-frame.http-fx]
    [reagent.core :as r]
    [re-frame.core :as rf]
    [goog.events :as events]
    [goog.history.EventType :as HistoryEventType]
    [markdown.core :refer [md->html]]
    [yearup.ajax :as ajax]
    [yearup.events]
    [reitit.core :as reitit]
    [reitit.frontend.easy :as rfe]
    [clojure.string :as string])
  (:import goog.History))

;(defn nav-link [uri title page]
;  [:a.navbar-item
;   {:href  uri
;    :class (when (= page @(rf/subscribe [:page])) :is-active)}
;   title])

;(defn navbar []
;  (r/with-let [expanded? (r/atom false)]
;              [:nav.navbar.is-info>div.container
;               [:div.navbar-brand
;                [:a.navbar-item {:href "/" :style {:font-weight :bold}} "YearUp"]
;                [:span.navbar-burger.burger
;                 {:data-target :nav-menu
;                  :on-click    #(swap! expanded? not)
;                  :class       (when @expanded? :is-active)}
;                 [:span] [:span] [:span]]]
;               [:div#nav-menu.navbar-menu
;                {:class (when @expanded? :is-active)}
;                [:div.navbar-start
;                 [nav-link "#/" "Home" :home]
;                 [nav-link "#/about" "About" :about]]]]))

(defn about-page []
    ;[:section.section>div.container>div.content
    ; [:img {:src "/img/warning_clojure.png"}]]

  )

(defn home-page []
  ;[:section.section>div.container>div.content
  ; (when-let [docs @(rf/subscribe [:docs])]
  ;   [:div {:dangerouslySetInnerHTML {:__html (md->html docs)}}])]
  [:div.container-content
  (when-let [data @(rf/subscribe [:quiz])]
    (let [question (first (get-in data [:data :questions]))
          cities (get-in data [:data :cities])]
      [:div.outside-div
       {:style {:background-image  (str "linear-gradient(rgba(0, 0, 0, 0.55), rgba(0, 0, 0, 0.55)), url('" (str "img/" (:backgroundImage question)) "')" )
                :background-repeat "no-repeat" :background-size "cover" :background-position "50% 25%" :background-attachment "fixed"}}
       [:div.middle-div [:img.logo {:src "img/YU_goldwhiteCC.png"}]
        [:div.inside-div
         [:p.lg-text (:detail question)]
         (cond
           (= (:order question) 1) [:div.dropdown.show.btn-container-dropdown
                                    [:a#dropdownMenuLink.btn.btn-secondary.dropdown-toggle.btn-dropdown-size
                                     {:href "#" :role "button" :data-toggle "dropdown" :aria-haspopup "true"} (:name (first (:options question)))]
                                    [:div.dropdown-menu.dropdown-display {:aria-labelledby "dropdownMenuLink" }
                                     (map (fn [n]
                                            [:a.dropdown-item {:href "#" :id (str (random-uuid)) :key (:id n)  } (:name n)]) cities)
                                                 ;[:a.dropdown-item {:href "#"} ]
                                                 ;[:a#los-angeles.dropdown-item {:href "#"} "Los Angeles"]
                                                 ;[:a#new-york.dropdown-item {:href "#"} "New York"]
                                                 ;[:a#charlotte.dropdown-item {:href "#"} "Charlotte"]
                                     ]]
           (= (:order question) 2)
           "")
         ]]]))])

(defn quiz-page [])

(defn page []
  (if-let [page @(rf/subscribe [:page])]
    ;[:div
     ;[navbar]
     [page]
;]
))

(defn navigate! [match _]
  (rf/dispatch [:navigate match]))

(def router
  (reitit/router
    [["/" {:name        :home
           :view        #'home-page
           :controllers [{:start (fn [_] (rf/dispatch [:page/init-home]))}]}]
     ["/about" {:name :about
                :view #'about-page}]
     ["/quiz" {:name :quiz
               :view #'quiz-page
               :controllers [{:start (fn [_] (rf/dispatch [:quiz/start]))}] }]]))

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
