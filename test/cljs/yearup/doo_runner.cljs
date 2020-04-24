(ns yearup.doo-runner
  (:require [doo.runner :refer-macros [doo-tests]]
            [yearup.core-test]))

(doo-tests 'yearup.core-test)

