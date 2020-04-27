(ns yearup.routes.services.graphql
  (:require
    [com.walmartlabs.lacinia.util :refer [attach-resolvers]]
    [com.walmartlabs.lacinia.schema :as schema]
    [com.walmartlabs.lacinia :as lacinia]
    [clojure.data.json :as json]
    [clojure.edn :as edn]
    [clojure.java.io :as io]
    [ring.util.http-response :refer :all]
    [mount.core :refer [defstate]]
    [yearup.db.core :as db]))

(defn get-user
  "Resolver for get-user graphql query"
  [context args value]
  (cond
    (= (ffirst args) :id) (db/get-user {:id (:id args)})
    (= (ffirst args) :email) (db/get-user-by-email {:email (:email args)})))

(defn get-quizzes
  "Resolver for get-quizzes graphql query"
  [context args value]
  (db/get-quizzes ))

(defn get-quiz
  "Resolver for get-quiz graphql query"
  [context args value]
  (db/get-quiz {:id (:id args)}))

(defn get-questions
  "Resolver for get-questions graphql query"
  [context args value]
  (db/get-questions {:quiz-id (:quizId args)}))

(defn get-question
  "Resolver for get-question graphql query"
  [context args dbvalue]
  (db/get-question {:quiz-id (:quizId args) :id (:questionId args)}))

(defstate compiled-schema
          :start
          (-> "graphql/schema.edn"
              io/resource
              slurp
              edn/read-string
              (attach-resolvers {:get-user get-user
                                 :get-quiz get-quiz
                                 :get-quizzes get-quizzes
                                 :get-question get-question
                                 :get-questions get-questions})
              schema/compile))

(defn format-params [query]
  (let [parsed (json/read-str query)]                       ;;-> placeholder - need to ensure query meets graphql syntax
    (str "query { hero(id: \"1000\") { name appears_in }}")))

(defn execute-request [query]
  (let [vars nil
        context nil]
    (-> (lacinia/execute compiled-schema query vars context)
        (json/write-str))))
