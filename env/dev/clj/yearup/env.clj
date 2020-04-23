(ns yearup.env
  (:require
    [selmer.parser :as parser]
    [clojure.tools.logging :as log]
    [yearup.dev-middleware :refer [wrap-dev]]))

(def defaults
  {:init
               (fn []
                 (parser/cache-off!)
                 (log/info "\n-=[yearup started successfully using the development profile]=-"))
   :stop
               (fn []
                 (log/info "\n-=[yearup has shut down successfully]=-"))
   :middleware wrap-dev})
