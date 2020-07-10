(ns yearup.routes.home
  (:require
    [buddy.sign.jwt :as jwt]
    [clojure.java.io :as io]
    [clojure.string :as string]
    [clojure.spec.alpha :as s]
    [ring.util.http-response :as response]
    [ring.util.response]
    [yearup.config :refer [env]]
    [yearup.db.queries :as query]
    [yearup.layout :as layout]
    [yearup.db.core :as db]
    [yearup.middleware :as middleware])
  (:import [java.io ByteArrayOutputStream]))

(s/def ::ratio (s/and integer? pos? #(<= % 100)))
(s/def ::city-fields (s/and string? not-empty #(not-empty (string/trim %))))
(s/def ::city-image (s/and not-empty #(string/includes?  (:content-type %) "image")))

(defn home-page [request]
  (layout/render request "home.html"))

(defn admin-page [{:keys [flash] :as request}]
  (let [report (query/report)]
    (layout/render request
                   "home2.html"
                   (merge {:report report}
                          (select-keys flash [:errors])))))

(defn setting-page [{:keys [flash] :as request}]
  (let [report (query/get-full-report)
        cities (query/get-cities)]
    (layout/render request
                   "setting.html"
                   (merge {:report report :cities cities}
                          (select-keys flash [:errors])))))

;(defn setting-page [request]
;  (let [metabase-site-url (env :metabase-site-url)
;        metabase-secret-key (env :metabase-secret-key)
;        payload {:resource {:dashboard 1}
;                 :params   {}
;                 :exp      (+ (int (/ (System/currentTimeMillis) 1000)) (* 60 100000000))}
;        token (jwt/sign payload metabase-secret-key)
;        iframe-url (str metabase-site-url "/embed/dashboard/" token "##theme=night&bordered=true&titled=true")]
;
;    (layout/render request "setting.html" {:iframeUrl iframe-url})))

(defn adjust-ratio! [{:keys [params]}]
  (cond
    (contains? params :ratio)
      (if (not (s/valid? ::ratio (Integer/parseInt (:ratio params))))
        (do (-> (response/found "/admin")
                (assoc :flash (assoc params :errors {:message {:ratio "Enter a valid ratio %"}}))))
        (do
          (query/update-ratio (Integer/parseInt (:ratio params)))
          (response/found "/admin")))))

(defn file->bytes [file]
  (with-open [in  (io/input-stream file)
              out (ByteArrayOutputStream.)]
    (io/copy in out)
    (.toByteArray out)))

(defn add-city!
  [{{:keys [city city-question file] :as params} :params}]
  (if (and (s/valid? ::city-fields (:city params))
           (s/valid? ::city-fields (:city-question params))
           (s/valid? ::city-image file))
    (do
      (let [byte (file->bytes (:tempfile file))]
        (query/create-city city city-question (:filename file) byte (:content-type file)))
      (response/found "/setting"))
    (do (-> (response/found "/setting")
            (assoc :flash
                   (assoc params :errors {:message {:city "Enter valid values for all fields"}}))))))

(defn home-routes []
  [""
   {:middleware [middleware/wrap-csrf
                 middleware/wrap-formats]}
   ["/" {:get home-page}]
   ["/admin" {:get  admin-page
              :post adjust-ratio!}]
   ["/setting" {:get setting-page
                :post add-city!}]
   ["/graphiql" {:get (fn [request] (layout/render request "graphiql.html"))}]
   ["/docs" {:get (fn [_]
                    (-> (response/ok (-> "docs/docs.md" io/resource slurp))
                        (response/header "Content-Type" "text/plain; charset=utf-8")))}]])

