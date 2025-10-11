((nil .
      ((cider-clojure-cli-aliases . ":dev:portal")
       (eval . (defun datomic.sqlite/conn ()
  "Connect to the Datomic SQLite database in ./storage/sqlite.db"
  (interactive)
  (let ((ns (cider-current-ns)))
    (cider-nrepl-sync-request:eval
     (format "(in-ns '%s)
              (def conn (d/connect \"datomic:sql://app?jdbc:sqlite:./storage/sqlite.db\"))"
             ns))))))))

