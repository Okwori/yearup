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
    [yearup.db.queries :as db]))

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
                         :description "https://docs.google.com/document/d/1N05yZhl6mOhh6zyFliscv8pniz0gB5vv4oii3jj-p0s/edit?usp=sharing"}}}

    ["/swagger.json"
     {:get (swagger/create-swagger-handler)}]

    ["/api-docs/*"
     {:get (swagger-ui/create-swagger-ui-handler
             {:url    "/api/v1/swagger.json"
              :config {:validator-url nil}})}]

    ["/graphql" {:post (fn [req] (ok (graphql/execute-request (-> req :body slurp))))}]]

   ["/question"
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

   ["/submit"
    {:post {:summary    "accepts a user and a collection options selected, returns message based on options"
            :parameters {:body {:userId int? :selections (vector? int?)}}
            :responses  {200 {:body map?}}
            :handler    (fn [{{{:keys [userId selections]} :body} :parameters}]
                          {:status 200
                           :body   (db/submit-answers userId selections)})}}]

   ["/options"
    {:post {:summary    "returns the options for a particular question"
            :parameters {:body {:questionId int?}}
            :responses  {200 {:body {:data vector?}}}
            :handler    (fn [{{{:keys [questionId]} :body} :parameters}]
                          {:status 200
                           :body   (db/get-options-question questionId)})}}]

   ["/quiz"
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

   ["/video"
    {:get {:summary    "returns the URL of the demo video on one of the slides"
           :parameters nil
           :responses  {200 {:body {:data {:video map?}}}}
           :handler    (fn [{{{:keys []} :query} :parameters}]
                         {:status 200
                          :body   {:data {:video (db/get-video-url)}}})}}]

   ["/cities"
    {:get {:summary    "returns the list of cities on the YearUp programme"
           :parameters nil
           :responses  {200 {:body {:data {:cities vector?}}}}
           :handler    (fn [{{{:keys []} :query} :parameters}]
                         {:status 200
                          :body   {:data {:cities (db/get-cities)}}})}}]

   ["/setting"
    {:get {:summary    "returns a particular setting with the name specified"
           :parameters {:body {:name string?}}
           :responses  {200 {:body {:data map?}}}
           :handler    (fn [{{{:keys [name]} :body} :parameters}]
                         {:status 200
                          :body   {:data (db/get-setting name)}})}}]])
