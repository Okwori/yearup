(ns yearup.routes.services
  (:require
    [reitit.swagger :as swagger]
    [reitit.swagger-ui :as swagger-ui]
    [reitit.ring.coercion :as coercion]
    [reitit.coercion.spec :as spec-coercion]
    [reitit.ring.middleware.muuntaja :as muuntaja]
    [reitit.ring.middleware.multipart :as multipart]
    [reitit.ring.middleware.parameters :as parameters]
    [yearup.routes.services.graphql :as graphql]
    [yearup.middleware.formats :as formats]
    [yearup.middleware.exception :as exception]
    [ring.util.http-response :refer :all]
    [clojure.java.io :as io]
    [yearup.db.queries :as db])
  (:import (java.io ByteArrayInputStream)))

(defn service-routes []
  ["/api/v1"
   {:coercion   spec-coercion/coercion
    :muuntaja   formats/instance
    :swagger    {:id ::api}
    :middleware [;; query-params & form-params
                 parameters/parameters-middleware
                 ;; content-negotiation
                 muuntaja/format-negotiate-middleware
                 ;; encoding response body
                 muuntaja/format-response-middleware
                 ;; exception handling
                 exception/exception-middleware
                 ;; decoding request body
                 muuntaja/format-request-middleware
                 ;; coercing response bodys
                 coercion/coerce-response-middleware
                 ;; coercing request parameters
                 coercion/coerce-request-middleware
                 ;; multipart
                 multipart/multipart-middleware]}

   ;; swagger documentation
   ["" {:no-doc  true
        :swagger {:info {:title       "YearUp API"
                         :description "YearUp Direct Service Programme 2020"}}}

    ["/swagger.json"
     {:get (swagger/create-swagger-handler)}]

    ["/api-docs/*"
     {:get (swagger-ui/create-swagger-ui-handler
             {:url    "/api/v1/swagger.json"
              :config {:validator-url nil}})}]

    ["/graphql" {:no-doc  true
                 :post (fn [req] (ok (graphql/execute-request (-> req :body slurp))))}]]

   ["/question"
    {:swagger {:tags ["question"]}}
    [""
     {:post {:summary    "return a question and its options"
             :parameters {:body {:quizId int? :questionId int?}}
             :responses  {200 {:body map?}}
             :handler    (fn [{{{:keys [quizId questionId]} :body} :parameters}]
                           {:status 200
                            :body   {:data {:question (db/get-question quizId questionId)}}})}}]

    ["/list"
     {:post {:summary    "returns the questions belonging to a quiz"
             :parameters {:body {:quizId int?}}
             :responses  {200 {:body map?}}
             :handler    (fn [{{{:keys [quizId]} :body} :parameters}]
                           {:status 200
                            :body   (db/get-questions quizId)})}}] ]

   ["/report"
    {:swagger {:tags ["report"]}}
    ["/"
     {:post {:summary    "returns all the data needs for the dashboard"
             :parameters nil
             :responses  {200 {:body map?}}
             :handler    (fn [{{{:keys []} :body} :parameters}]
                           {:status 200
                            :body   (db/report)})}}]

    ["/selection"
     {:post {:summary    "returns the answer for a particular user given the email and quiz id"
             :parameters {:body {:quizId int? :email string?}}
             :responses  {200 {:body {:data coll?}}}
             :handler    (fn [{{{:keys [quizId email]} :body} :parameters}]
                           {:status 200
                            :body   (db/get-answer-by-user quizId email)})}}]

    ["/response"
     {:get {:summary    "returns all response data needs for the dashboard"
             :parameters nil
             :responses  {200 {:body coll?}}
             :handler    (fn [{{{:keys []} :body} :parameters}]
                           {:status 200
                            :body   (:response (db/report))})}}]

    ["/list"
     {:get {:summary    "returns the full report of responses to the quiz"
             :parameters nil
             :responses  {200 {:body coll?}}
             :handler    (fn [{{{:keys []} :body} :parameters}]
                           {:status 200
                            :body   (db/get-full-report)})}}]]

   ["/submit"
    {:no-doc  true
     :post {:summary    "accepts a user and a collection options selected, returns message based on options"
            :parameters {:body {:userId int? :selections (vector? int?) :cityId int?}}
            :responses  {200 {:body map?}}
            :handler    (fn [{{{:keys [userId selections cityId]} :body} :parameters}]
                          {:status 200
                           :body   (db/submit-answers userId selections cityId)})}}]

   ["/options"
    {:post {:summary    "returns the options for a particular question"
            :parameters {:body {:questionId int?}}
            :responses  {200 {:body {:data vector?}}}
            :handler    (fn [{{{:keys [questionId]} :body} :parameters}]
                          {:status 200
                           :body   (db/get-options-question questionId)})}}]

   ["/quiz"
    {:swagger {:tags ["quiz"]}}
    [""
     {:post {:summary    "returns the quiz specified by id in the system"
             :parameters {:body {:id int?}}
             :responses  {200 {:body map?}}
             :handler    (fn [{{{:keys [id]} :body} :parameters}]
                           {:status 200
                            :body   {:data (db/get-quiz id)}})}}]

    ["/list"
     {:get {:summary    "returns the quizzes specified in the system"
            :parameters nil
            :responses  {200 {:body {:data vector?}}}
            :handler    (fn [{{{:keys []} :body} :parameters}]
                          {:status 200
                           :body   {:data (db/get-quiz)}})}}]]

   ["/user"
    {:swagger {:tags ["user"]}}
    [""
     {:get  {:summary    "returns the user with the specified email"
             :parameters {:query {:email string?}}
             :responses  {200 {:body {:data map?}}}
             :handler    (fn [{{{:keys [email]} :query} :parameters}]
                           {:status 200
                            :body   {:data (db/get-user-by-email email)}})}

      :post {:summary    "returns the user with the specified Id"
             :parameters {:body {:userId int?}}
             :responses  {200 {:body {:data map?}}}
             :handler    (fn [{{{:keys [userId]} :body} :parameters}]
                           {:status 200
                            :body   {:data (db/get-user-by-id userId)}})}}]
    ["/create"
     {:no-doc  true
      :post {:summary    "create a user with the specified email"
             :parameters {:body {:email string?}}
             :responses  {200 {:body {:id int?}}}
             :handler    (fn [{{{:keys [email]} :body} :parameters}]
                           {:status 200
                            :body   (db/create-user-by-email email)})}}] ]

   ["/video"
    {:get {:summary    "returns the URL of the demo video on one of the slides"
           :parameters nil
           :responses  {200 {:body {:data {:video map?}}}}
           :handler    (fn [{{{:keys []} :query} :parameters}]
                         {:status 200
                          :body   {:data {:video (db/get-video-url)}}})}}]

   ["/city"
    ["/list"
     {:get {:summary    "returns the list of cities on the YearUp programme"
            :parameters nil
            :responses  {200 {:body {:data {:cities vector?}}}}
            :handler    (fn [{{{:keys []} :query} :parameters}]
                          {:status 200
                           :body   {:data {:cities (db/get-cities)}}})}}]
    ;["/create"
    ; {:post {:summary    "create a new city record and returns 1"
    ;         :parameters {:body {:name string?}}
    ;         :responses  {200 {:body {:id int?}}}
    ;         :handler    (fn [{{{:keys [name]} :body} :parameters}]
    ;                       {:status 200
    ;                        :body   (db/create-city name)})}}]
    ]
   ["/files"
    {:swagger {:tags ["files"]}}

    ["/download/:id"
     {:get {:summary    "downloads a file"
            :parameters {:path {:id int?}}
            :swagger    {:produces ["image/png"]}
            :handler    (fn [{{{:keys [id]} :path} :parameters}]
                          (let [image-cities (db/get-city id)
                                image (-> image-cities
                                          :data
                                          (ByteArrayInputStream.))]
                            {:status  200
                             :headers {"Content-Type" (:content-type image-cities)}
                             :body    image}))}}]]

   ["/setting"
    {:get {:summary    "returns a particular setting with the name specified"
           :parameters {:body {:name string?}}
           :responses  {200 {:body {:data map?}}}
           :handler    (fn [{{{:keys [name]} :body} :parameters}]
                         {:status 200
                          :body   {:data (db/get-setting name)}})}}]

   ["/update"
    ["/ratio"
     {:post {:summary    "adjust system ratio"
            :parameters {:body {:r-ratio int?}}
            :responses  {200 {:body {:data int?}}}
            :handler    (fn [{{{:keys [r-ratio]} :body} :parameters}]
                          {:status 200
                           :body   {:data (db/update-ratio r-ratio)}})}}]]])
