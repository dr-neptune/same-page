(ns samepage.main
    (:require [samepage.server.system :as system]))

(defn -main []
      (system/start-system))
