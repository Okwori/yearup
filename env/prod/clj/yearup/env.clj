(ns yearup.env
  (:require [clojure.tools.logging :as log]))

(def defaults
  {:init
               (fn []
                 (log/info "\n-=[yearup started successfully]=-"))
   :stop
               (fn []
                 (log/info "\n-=[yearup has shut down successfully]=-"))
   :middleware identity})
