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
    [tick.alpha.api :as t]
    [tick.locale-en-us]
    [yearup.ajax :as ajax]
    [yearup.events])
  (:import goog.History))

(s/def ::email (s/and string? (partial re-matches #"^[a-zA-Z0-9._%+-]+@[a-zA-Z0-9.-]+\.[a-zA-Z]{2,63}$")))
(s/def ::r-ratio (s/and integer? pos? #(<= % 100)))

(defn parse-date [tagged]
  (t/format (t/formatter "HH:mm MM/dd/yyyy ") (t/parse (.-rep tagged))))

(defn nav-link [uri title page]
  [:a.navbar-item
   {:href   uri
    :class (when (= page @(rf/subscribe [:common/page])) :is-active)}
   title])

(defn navbar []
  (r/with-let [expanded? (r/atom false)]
              [:nav.navbar.is-info>div.container
               [:div.navbar-brand
                [:a.navbar-item {:href "/" :style {:font-weight :bold}} "yearup"]
                [:span.navbar-burger.burger
                 {:data-target :nav-menu
                  :on-click #(swap! expanded? not)
                  :class (when @expanded? :is-active)}
                 [:span][:span][:span]]]
               [:div#nav-menu.navbar-menu
                {:class (when @expanded? :is-active)}
                [:div.navbar-start
                 [nav-link "#/" "Home" :home]
                 [nav-link "#/about" "About" :about]]]]))

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
                           [:select
                            {:name "city-drop-down" :id "city-drop-down"
                             :on-change #(rf/dispatch [:submit {:city-id (js/parseInt (-> % .-target .-value))}])}
                            [:option "Select the CITY closest to you"]
                           (for [city cities]
                               ^{:key (:id city)} [:option {:value (:id city)} (:name city)])]

                         (= order 2)
                          [:li [:a {:style {:background-color "#AAA4A2"}
                                    :on-click #(rf/dispatch [:submit])} (:name (first (:options question)))]]

                         (= order 3)
                           (let [vals-email (r/atom {:email ""})]
                             [:li [:div.fields
                                  [:div.field.half
                                   [:input#user-email
                                    {:type        "email" :name "user-email" :required "required"
                                     :placeholder "email@example.com"
                                     :on-key-down #(if (= (.-key %) "Enter") ;; TODO remove side pass
                                                     (if (or (s/valid? ::email (:email @vals-email))
                                                             (= "yu"
                                                                (string/lower-case (string/trim (:email @vals-email))))
                                                             (= "ibuwembo@yearup.org"
                                                                (string/lower-case
                                                                  (string/trim (:email @vals-email)))))
                                                       (rf/dispatch [:submit-user @vals-email])
                                                       (rf/dispatch [:common/set-error "Enter a valid email address"])))
                                     :on-change   #(swap! vals-email assoc :email (-> % .-target .-value))}]]
                                  [:a.button.primary
                                   {:on-click #(if (or (s/valid? ::email (:email @vals-email))
                                                       (= "yu"
                                                          (string/lower-case (string/trim (:email @vals-email))))
                                                       (= "ibuwembo@yearup.org"
                                                          (string/lower-case
                                                            (string/trim (:email @vals-email)))))
                                                 (rf/dispatch [:submit-user @vals-email])
                                                 (rf/dispatch [:common/set-error "Enter a valid email address"]))}
                                   "Submit"]]])

                         (->> (into [] (range 4 14))  (some #(= order %)))
                           (for [option (:options question)]
                             ^{:key (:id option)}
                             [:li [:a.button
                                   {:style    (cond (= (:sentiment option) 0)
                                                      {:background-color "#E11D0D"}
                                                    (string/includes? (:name option) "Not sure")
                                                      {:background-color "#AAA4A2"}
                                                    (= option (first (:options question)))
                                                      {:background-color "#27760D"})
                                    :on-click (if (= order 13 )
                                                #(rf/dispatch [:submit-answers option])
                                                #(rf/dispatch [:submit option]))}
                                   (:name option)]])

                         (= order 0)
                           [:li [:a.button {:href "https://www.yearup.org/seize-opportunity/"
                                            :style {:background-color "#AAA4A2"}}
                                 (:name (first (:options question)))]])]]]
             (if (and (= order 0) (string/includes? (:detail question) "Ok, now might"))
               [:a {:href "https://www.yearup.org/seize-opportunity/"}
                "Wait, I still want to learn more about Year Up"])
             [:footer#footer
              [:p.copyright "© 2020 YearUp"]]]
       [:div#bg {:style {:background-position "center"
                         :background-size "cover"
                         :background-repeat "no-repeat"
                         :z-index "1"
                         :background-image (if (= order 13)
                                             (do (when-let [city-id (get-in answer [:city-id])]
                                                   (let [city (filter #(= (:id %) city-id) cities)]
                                                     (str "url('/api/v1/files/download/"
                                                          (:id (first city))"')"))))
                                             (str "url('img/" (:backgroundImage question) "')"))}}]])))

(defn admin-page []
  (when-let [report @(rf/subscribe [:page/report])]
    [:div.container.body
     [:div.main_container
      [:div.col-md-3.left_col
       [:div.left_col.scroll-view
        [:div.navbar.nav_title {:style {:border "0"}}
         [:a.site_title {:href "#"} [:span "YearUp Admin"]]]
        [:div.clearfix]
        [:br]
        [:div#sidebar-menu.main_menu_side.hidden-print.main_menu
         [:div.menu_section
          [:ul.nav.side-menu
           [:li [:a {:href "#"} [:i.fa.fa-dashboard] "Dashboard"]]]]]]]
      [:div.top_nav
       [:div.nav_menu
        [:div.nav.toggle
         [:a#menu_toggle [:i.fa.fa-bars]]]
        [:nav.nav.navbar-nav]]]
      [:div.right_col {:role "main"}
       [:div.row {:style {:display "inline-block"}}
        [:div.animated.flipInY.col-lg-3.col-md-3.col-sm-6
         [:div.tile-stats
          [:div.icon [:i.fa.fa-user.blue]]
          [:div.count (:totalNo report)]
          [:h3 "Total"]
          [:p "Number of all the users that responded to the quiz"]]]
        [:div.animated.flipInY.col-lg-3.col-md-3.col-sm-6
         [:div.tile-stats
          [:div.icon [:i.fa.fa-check.green]]
          [:div.count.green (:positiveNo report)]
          [:h3 "Positive"]
          [:p "Number of responses that are positive based on ratio"]]]
        [:div.animated.flipInY.col-lg-3.col-md-3.col-sm-6
         [:div.tile-stats
          [:div.icon [:i.fa.fa-thumbs-down.red]]
          [:div.count (:negativeNo report)]
          [:h3 "Negative"]
          [:p "Number of responses that aren't favorable based on ratio"]]]
        [:div.animated.flipInY.col-lg-3.col-md-3.col-sm-6
         [:div.tile-stats
          [:div.icon [:i.fa.fa-shield]]
          [:div.count (str (:ratio report) "%")]
          [:h3 "Ratio"]
          [:p "A percentage of user's response to be deemed positive"]]]]
       [:div.row
        [:div.col-md-12.col-sm-12
         [:div.col-md-8.col-sm-8
          [:div.x_panel
           [:div.x_title
            [:h2 "Quiz Responses"]
            [:ul.nav.navbar-right.panel_toolbox
             [:li [:a.collapse-link [:i.fa.fa-chevron-up]]]
             [:li [:a.close-link [:i.fa.fa-close]]]]
            [:div.clearfix]]
           [:div.x_content
            [:div.row
             [:div.col-sm-12
              [:div.card-box.table-responsive
               [:p.text-muted.font-13.m-b-30
                "This table show all the responses to the Quiz ordered the latest responses"]
               [:table#datatable-buttons.table.table-striped.table-bordered {:style {:width "100%"}}
                [:thead
                 [:tr
                  [:th "Email"]
                  [:th "City"]
                  [:th "Time"]
                  [:th "Accepted Responses?"]]]
                [:tbody
                 (for [response (:response report)]
                   ^{:key (parse-date (:date response)) }
                   [:tr
                    [:td [:a {:href "#" :data-toggle "modal" :data-target ".bs-example-modal-lg"
                              :on-click #(rf/dispatch [:question-response (:responses response)])} (:email response)]]
                    [:td (:city response)]
                    [:td (parse-date (:date response))]
                    (cond (= (:accepted response) "Yes") [:td [:span.badge.badge-success "Yes"]]
                          (= (:accepted response) "No") [:td [:span.badge.badge-danger "No"]])])]]
               [:div.modal.fade.bs-example-modal-lg {:tabIndex "-1" :role "dialog" :aria-hidden "true"}
                [:div.modal-dialog.modal-lg
                 [:div.modal-content
                  [:div.modal-header
                   [:h4#myModalLabel.modal-title "Response"]
                   [:button.close {:type "button" :data-dismiss "modal"} [:span {:aria-hidden "true"} "×"]]]
                  [:div.modal-body
                   [:table#datatable-buttons2.table.table-striped.table-bordered {:style {:width "100%"}}
                    [:thead
                     [:tr
                      [:th "Question"]
                      [:th "Option Selected"]]]
                    [:tbody
                     (when-let [current @(rf/subscribe [:get-current-response])]
                       (for [question-response current]
                         ^{:key question-response}[:tr
                                                   [:td (:question question-response)]
                                                   [:td (:option question-response)]]))]]]
                  [:div.modal-footer
                   [:button.btn.btn-secondary {:type "button" :data-dismiss "modal"
                                               :on-click #(rf/dispatch [:page/dispose] )} "Close"]]]]]]]]]]]
         [:div.col-md-4.col-sm-4
          ;[:div.x_panel.tile.fixed_height_320.overflow_hidden
          ; [:div.x_title
          ;  [:h2 "Responses By City"]
          ;  [:ul.nav.navbar-right.panel_toolbox
          ;   [:li [:a.collapse-link [:i.fa.fa-chevron-up]]]
          ;   [:li [:a.close-link [:i.fa.fa-close]]]]
          ;  [:div.clearfix]]
          ; [:div.x_content
          ;  [:table {:style {:width "100%"}}
          ;   [:thead [:tr
          ;            [:th {:style {:width "37%"}}
          ;             [:p " "]]
          ;            [:th
          ;             [:div.col-lg-7.col-md-7.col-sm-7
          ;              [:p "City"]]
          ;             [:div.col-lg-5.col-md-5.col-sm-5
          ;              [:p "% age"]]]]]
          ;   [:tbody [:tr
          ;            [:td
          ;             [:canvas.canvasDoughnut {:height "140" :width "140" :style {:margin "15px 10px 10px 0"}}]]
          ;            [:td
          ;             [:table.tile_info
          ;              [:tbody [:tr
          ;                       [:td
          ;                        [:p [:i.fa.fa-square.blue] "Los Angeles"]]
          ;                       [:td "35%"]]
          ;               [:tr
          ;                [:td
          ;                 [:p [:i.fa.fa-square.green] "New York"]]
          ;                [:td "45%"]]
          ;               [:tr
          ;                [:td
          ;                 [:p [:i.fa.fa-square.purple] "Charlotte"]]
          ;                [:td "20%"]]]]]]]]]]
          (let [vals-ratio (r/atom {:r-ratio (:ratio report)})]
            [:div.x_panel.tile.fixed_height_320.overflow_hidden
               [:div.x_title
                [:h2 "Settings"]
                [:ul.nav.navbar-right.panel_toolbox
                 [:li [:a.collapse-link [:i.fa.fa-chevron-up]]]
                 [:li [:a.close-link [:i.fa.fa-close]]]]
                [:div.clearfix]]
               [:div.x_content
                [:p.text-muted.font-13.m-b-30 "Adjust system settings"]
                (when-let [error-message @(rf/subscribe [:common/error])]
                  [:p {:style {:color "red"}} error-message])
                [:form#demo-form2.form-horizontal.form-label-left
                 {:action      "#" :data-parsley-validate "true"
                  :on-key-down #(if (= (.-key %) "Enter")
                                  (if (not (s/valid? ::r-ratio (js/parseInt (:r-ratio @vals-ratio))))
                                    (do (rf/dispatch [:common/set-error "Enter a valid number"]))
                                    (do (rf/dispatch [:clear-exceptions])
                                        (rf/dispatch [:submit-ratio @vals-ratio]))))}
                 [:div.item.form-group
                  [:label.col-form-label.col-md-3.col-sm-3.label-align {:for "ratio-percent"} "Ratio %"]
                  [:div.col-md-6.col-sm-6
                   [:input#ratio-percent.form-control {:type        "text"
                                                       :placeholder (:ratio report)
                                                       :on-change   #(swap! vals-ratio assoc :r-ratio
                                                                            (-> % .-target .-value))}]]]
                 [:div.item.form-group
                  [:div.col-md-6.col-sm-6.offset-md-3
                   [:button.btn.btn-success
                    {:type "button"
                     :on-click #(if (not (s/valid? ::r-ratio (js/parseInt (:r-ratio @vals-ratio))))
                                  (do (rf/dispatch [:common/set-error "Enter a valid number"]))
                                  (do (rf/dispatch [:clear-exceptions])
                                      (rf/dispatch [:submit-ratio @vals-ratio])))} "Adjust"]]]]]])

          (let [vals-city (r/atom {:r-city ""})]
            [:div.x_panel.tile.fixed_height_320.overflow_hidden
             [:div.x_title
              [:h2 "Cities"]
              [:ul.nav.navbar-right.panel_toolbox
               [:li [:a.collapse-link [:i.fa.fa-chevron-up]]]
               [:li [:a.close-link [:i.fa.fa-close]]]]
              [:div.clearfix]]
             [:div.x_content
              [:p.text-muted.font-13.m-b-30 "Add more cities to the system"]
              [:a {:href "#" :data-toggle "modal" :data-target ".bs-example-modal-sm"} "view cities"]
              (when-let [error-message @(rf/subscribe [:common/error])]
                [:p {:style {:color "red"}} error-message])
              [:form#demo-form3.form-horizontal.form-label-left
               {:action      "#" :data-parsley-validate "true"
                :on-key-down #(if (= (.-key %) "Enter")
                                (if (not (s/valid? ::r-city (:r-city (string/trim @vals-city))))
                                  (do (rf/dispatch [:common/set-error "Enter a city name"]))
                                  (do (rf/dispatch [:clear-exceptions])
                                      (rf/dispatch [:submit-city @vals-city]))))}
               [:div.item.form-group
                [:label.col-form-label.col-md-3.col-sm-3.label-align {:for "city-name"} "Name"]
                [:div.col-md-6.col-sm-6
                 [:input#city-name.form-control {:type        "text"
                                                 :placeholder "Enter city name"
                                                 :on-change   #(swap! vals-city assoc :r-city
                                                                      (-> % .-target .-value))}]]]
               [:div.item.form-group
                [:div.col-md-6.col-sm-6.offset-md-3
                 [:button.btn.btn-success
                  {:type "button"
                   :on-click #(if (= (.-key %) "Enter")
                                (if (not (s/valid? ::r-city (:r-city (string/trim @vals-city))))
                                  (do (rf/dispatch [:common/set-error "Enter a city name"]))
                                  (do (rf/dispatch [:clear-exceptions])
                                      (rf/dispatch [:submit-city @vals-city]))))} "Submit"]]]]]])]]]
       [:br]]
      [:footer
       [:div.pull-right "© 2020 YearUp"]
       [:div.clearfix]]]]) )

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
           :controllers [{:start (fn [_] (rf/dispatch [:page/init-home]))}]}]
     ["/admin" {:name        :admin
           :view        #'admin-page
           :controllers [{:start (fn [_] (rf/dispatch [:page/init-admin]))}]}]]))

(defn start-router! []
  (rfe/start!
    router
    navigate!
    {:use-fragment false}))

;; -------------------------
;; Initialize app
(defn mount-components []
  (rf/clear-subscription-cache!)
  (rdom/render [#'page] (.getElementById js/document "app")))

(defn init! []
  (start-router!)
  (ajax/load-interceptors!)
  (mount-components))
