(ns yearup.test.handler
  ;(:use midje.sweet)
  (:require
    [clojure.test :refer :all]
    [ring.mock.request :refer :all]
    [yearup.handler :refer :all]
    [yearup.middleware.formats :as formats]
    [muuntaja.core :as m]
    [mount.core :as mount]
    [midje.sweet :refer :all]
    ))

(set! *warn-on-reflection* true)

(defn parse-json [body]
  (m/decode formats/instance "application/json" body))

(use-fixtures
  :once
  (fn [f]
    (mount/start #'yearup.config/env
                 #'yearup.handler/app-routes)
    (f)))
(deftest test-app
  (fact "main route"
        (let [response ((app) (request :get "/"))]
          (fact (:status response) => 200)))
  (fact "not-found route"
        (let [response ((app) (request :get "/invalid"))]
          (fact (:status response) => 404)))

  (fact "Services"
        (fact "Questions: /question"
              (fact "Success"
                (let [response ((app) (-> (request :post "/api/v1/question")
                                              (json-body {:quizId 1, :questionId 8})))]
                      (fact (:status response) => 200)
                      (fact (m/decode-response-body response) => {:data
                                                                  {:question
                                                                   {:id 8,
                                                                    :detail "After those 6 months of training, you’ll be ready for a corporate internship.",
                                                                    :options [{:id 20, :name "Not sure"}
                                                                              {:id 18, :name "Yes, I like how that sounds"}
                                                                              {:id 19, :name "Nope, not me"}]}}} ))))
        (fact "Questions: /list"
              (fact "successfully listed questions"
                    (let [response ((app) (-> (request :post "/api/v1/question/list")
                                              (json-body {:quizId 1})))]
                      (fact (:status response) => 200)
                      (fact (m/decode-response-body response) => {:data {:quizId 1,
                                                                         :description "YU DS Program ",
                                                                         :questions [{:id 5,
                                                                                      :detail "You’ll learn practical skills, like workplace communication, problem solving, and time management.",
                                                                                      :options [{:id 9, :name "YES, that sounds fun"}
                                                                                                {:id 11, :name "Not sure"}
                                                                                                {:id 10, :name "NO thanks"}]}
                                                                                     {:id 6,
                                                                                      :detail "You’ll be in class at Year Up 5 days a week for 6 months.",
                                                                                      :options [{:id 13, :name "NO, not for me"}
                                                                                                {:id 12, :name "YES, I’m ready to learn"}
                                                                                                {:id 14, :name "Not sure"}]}
                                                                                     {:id 7,
                                                                                      :detail "You’ll be expected to arrive on time and professionally dressed, every weekday.",
                                                                                      :options [{:id 15, :name "YES, I’m ready to learn"}
                                                                                                {:id 16, :name "NO, not for me"}
                                                                                                {:id 17, :name "Not sure"}]}
                                                                                     {:id 1,
                                                                                      :detail "If you’ve made it here, you’re probably wondering whether Year Up is a good fit for you!",
                                                                                      :options [{:id 1, :name "Select the CITY closest to you"}]}
                                                                                     {:id 2,
                                                                                      :detail "Find out if Year Up might be right for you In 3 minutes by responding to questions about the program.",
                                                                                      :options [{:id 2, :name "START"}]}
                                                                                     {:id 3,
                                                                                      :detail "Are you looking to start, advance, or change your career?",
                                                                                      :options [{:id 5, :name "Not sure"} {:id 3, :name "YES"} {:id 4, :name "NO"}]}
                                                                                     {:id 12,
                                                                                      :detail "You’ll commute to ",
                                                                                      :options [{:id 32, :name "Not sure"}
                                                                                                {:id 30, :name "Yes, that can work for me"}
                                                                                                {:id 31, :name "Nope, not me"}]}
                                                                                     {:id 13, :detail "Before we get started, what is your e-mail address?", :options []}
                                                                                     {:id 8,
                                                                                      :detail "After those 6 months of training, you’ll be ready for a corporate internship.",
                                                                                      :options [{:id 20, :name "Not sure"}
                                                                                                {:id 18, :name "Yes, I like how that sounds"}
                                                                                                {:id 19, :name "Nope, not me"}]}
                                                                                     {:id 9,
                                                                                      :detail "6 months of successful career training leads to a 6 month corporate internship",
                                                                                      :options [{:id 21, :name "Yes, I like how that sounds"}
                                                                                                {:id 23, :name "Not sure"}
                                                                                                {:id 22, :name "Nope, not me"}]}
                                                                                     {:id 10,
                                                                                      :detail "You’ll be learning with the support of a Year Up community of your peers, professional coaches, mentors, and social workers.",
                                                                                      :options [{:id 24, :name "YES, that sounds helpful"}
                                                                                                {:id 26, :name "Not sure"}
                                                                                                {:id 25, :name "NO, not for me"}]}
                                                                                     {:id 11,
                                                                                      :detail "You’ll earn a stipend, but you’ll need to rely on savings, family support, and evening/weekend jobs.",
                                                                                      :options [{:id 29, :name "Not sure"}
                                                                                                {:id 27, :name "YES, I can make that work"}
                                                                                                {:id 28, :name "NO, not possible"}]}
                                                                                     {:id 4,
                                                                                      :detail "Are you willing to step outside of your comfort zone?",
                                                                                      :options [{:id 8, :name "Not sure"}
                                                                                                {:id 6, :name "Yes, I like how that sounds"}
                                                                                                {:id 7, :name "Nope, not me"}]}]}}))))

        (fact "Answers: /submit"
              (fact "Successfully submitted answers"
                    (let [response ((app) (-> (request :post "/api/v1/submit")
                                              (json-body {:userId 6 :selections [5 6 8 10 12 7]})))
                          response2 ((app) (-> (request :post "/api/v1/submit")
                                               (json-body {:userId 6 :selections [10 13 16 19]})))]
                      (fact (:status response) => 200)(fact (:status response2) => 200)
                      (fact (m/decode-response-body response) => {:data {:message "Seems like Year Up could be a great fit for you.", :code 1}})
                      (fact (m/decode-response-body response2) => {:data {:message "Ok, now might not be the best time for you to start.", :code 2}}))))

        (fact "Options: /options"
              (fact "Successfully submitted options"
                    (let [response ((app) (-> (request :post "/api/v1/options")
                                              (json-body {:questionId 6})))]
                      (fact (:status response) => 200)
                      (fact (m/decode-response-body response) => {:data [{:id 13, :name "NO, not for me"} {:id 12, :name "YES, I’m ready to learn"} {:id 14, :name "Not sure"}]}
                            ))))

        (fact "Quiz: /quiz"
              (fact "Success"
                    (let [response ((app) (-> (request :post "/api/v1/quiz")
                                              (json-body {:id 1})))]
                      (fact (:status response) => 200)
                      (fact (m/decode-response-body response) => {:data {:quizId 1, :description "YU DS Program ", :status "ACTIVE"}}))))

        (fact "Quiz: /quiz/list"
              (fact "Success"
                    (let [response ((app) (-> (request :get "/api/v1/quiz/list")))]
                      (fact (:status response) => 200)
                      (fact (m/decode-response-body response) => {:data [{:quizId 1, :description "YU DS Program ", :status "ACTIVE"}
                                                                         {:quizId 2, :description "YU DS Program 2", :status "INACTIVE"}]}))))

        (fact "Quiz: /user"
              (fact "Success"
                    (let [response ((app) (-> (request :get "/api/v1/user" {:email "simon@gmail.com"})
                                              ))]
                      (fact (:status response) => 200)
                      (fact (m/decode-response-body response) => {:data {:first_name "Simon", :last_name "Okwori", :email "simon@gmail.com"}}))))

        (fact "Quiz: /user"
              (fact "Success"
                    (let [response ((app) (-> (request :post "/api/v1/user")
                                              (json-body {:userId 6})))]
                      (fact (:status response) => 200)
                      (fact (m/decode-response-body response) => {:data {:first_name "Simon", :last_name "Okwori", :email "simon@gmail.com"}}))))

        (fact "Quiz: /video"
              (fact "Success"
                    (let [response ((app) (-> (request :get "/api/v1/video")))]
                      (fact (:status response) => 200)
                      (fact (m/decode-response-body response) => {:data {:video {:value "https://www.youtube.com/watch?v=D43z7kYi55I"}}}))))

        (fact "Quiz: /cities"
              (fact "Success"
                    (let [response ((app) (-> (request :get "/api/v1/cities")))]
                      (fact (:status response) => 200)
                      (fact (m/decode-response-body response) => {:data {:cities [{:id 3, :name "Charlotte"} {:id 1, :name "Los Angeles"} {:id 2, :name "New York"}]} }))))

        (fact "Quiz: /setting"
              (fact "Success"
                    (let [response ((app) (-> (request :get "/api/v1/setting")
                                              (json-body {:name "RATIO"})))]
                      (fact (:status response) => 200)
                      (fact (m/decode-response-body response) => {:data {:value "60"}}))))))
