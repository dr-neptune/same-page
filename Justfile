help:
  just --list

run:
  clojure -M -m samepage.main

format_check:
  clojure -M:format -m cljfmt.main check src

format:
  clojure -M:format -m cljfmt.main fix src

lint:
  clojure -M:lint -m clj-kondo.main --lint .
