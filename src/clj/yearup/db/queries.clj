(ns yearup.db.queries
  (:require
    [yearup.db.core :as db]))

(defn get-city
  "Get a city with address specified"
  [id]
  (let [city-query (db/get-city {:id id})
        address-query (into [] (db/get-address-by-city-id {:city-id (:id city-query)})) ]
    {:id (:id city-query) :name (:name city-query) :addresses address-query}))

(defn get-cities
  "Gets the vector of the cities defined for YearUp"
  []
  (into [] (db/get-cities)))

(defn get-options-question
  "Gets the options for a question"
  [question-id]
  (let [query (db/get-option-by-question-id {:question-id question-id})]
    {:data (into [] query)}))

(defn get-question
  "Returns a question in a quiz of Id : quiz-id"
  [quiz-id id]
  (let [q (db/get-question {:quiz-id quiz-id :id id})
        q2 (db/get-option-by-question-id {:question-id id})]
    {:id id :detail (:question q) :subNote (:sub_note q) :backgroundImage (:background_image q)
         :order (:slide_order q) :options (into [] q2)}))

(defn get-questions
  "Returns a list of questions for a quiz with the specified Id"
  [quiz-id]
  (let [q (db/get-quiz {:id quiz-id})
        q2 (db/get-questions {:quiz-id quiz-id})
        q3 (flatten (map #(vals (select-keys % [:id])) q2))
        q4 (flatten (map #(vals (select-keys % [:id])) (get-cities)))
        q5 (into [] (map #(get-city %) q4))]
    {:data {:quizId      (:id q)
            :description (:desc q)
            :cities       q5
            :questions   (into [] (map #(get-question quiz-id %) q3))}}))

(defn submit-answers
  "Persists the result of the quiz for the specified user and a collection of options"
  [user-id options-coll]
  (let [q (map #(db/create-answer! {:option-id % :user-id user-id}) options-coll)
        q1 (Integer/parseInt (:value (db/get-setting {:name "RATIO"})))
        q2 (if (every? #(= % 1) q) (filter #(not (nil? %)) (map #(db/get-option-by-sentiment-not-null {:id %}) options-coll)))
        q3 (map #(:sentiment %) q2)
        q4 (* (/ (apply + q3) (count q3)) 100)]
    (cond
      (>= q4 q1) {:data {:message "Seems like Year Up could be a great fit for you." :code 1}}
      (< q1) {:data {:message "Ok, now might not be the best time for you to start." :code 2}})))

(defn get-quiz
  "Gets a quiz or a vector of quizzes"
  ([id] (let [query (db/get-quiz {:id id})
              status (:status query)]
          {:quizId      (:id query)
           :description (:desc query)
           :status      (cond
                          (= status 1) "ACTIVE"
                          (= status 2) "INACTIVE")}))
  ([] (into [] (let [query (db/get-quizzes)]
                 (map #(let [status (:status %)]
                         {:quizId      (:id %)
                          :description (:desc %)
                          :status      (cond
                                         (= status 1) "ACTIVE"
                                         (= status 2) "INACTIVE")}) query)))))

(defn get-user-by-id
  "Gets a user by Id specified"
  [id]
  (db/get-user {:id id}))

(defn get-user-by-email
  "Gets a user by email address specified"
  [email]
  (db/get-user-by-email {:email email}))

(defn get-video-url
  "Gets the Video URL"
  []
  (db/get-setting {:name "VIDEO"}))

(defn get-setting
  "Gets the value for a setting enum defined like Video URL"
  [name]
  (db/get-setting {:name name}))

(defn create-user-by-email
  "Create a new user record with the email specified"
  [email]
  (db/create-user-return-id! {:email email :first_name nil :last_name nil :pass nil}))
