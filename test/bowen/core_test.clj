(ns bowen.core-test
  (:require [clojure.pprint :refer [pprint]]
            [midje.sweet :refer :all]
            [bowen.core :refer :all]))

(defprotocol Talky
  (sayhello [this]
            [this that])
  (echo [this s]))

(defprotocol Goodbye
  (goodbye [this]))

(deftype TalkyImpl []
  Talky
  (sayhello [this] "hello")
  (sayhello [this that] (str "hello " that))
  (echo [this s] s))

(fact "expected-sigs works"
      (expected-sigs Talky)
      => (contains [{:count 1 :name 'sayhello :arglist [(symbol 'this)]}
                    {:count 2 :name 'sayhello :arglist [(symbol 'this) (symbol 'that)]}
                    {:count 2 :name 'echo :arglist [(symbol 'this) (symbol 's)]}]
                   :in-any-order))

(fact "actual-sigs works"
      (actual-sigs '((sayhello [this] "hello") (sayhello [this that] "hello") (echo [this s] s)))
      => (contains [{:count 1 :name 'sayhello}
                    {:count 2 :name 'sayhello}
                    {:count 2 :name 'echo}]
                   :in-any-order))

(fact "missing-methods works"
      (missing-methods Talky '((sayhello [this] "hello")))
      => (contains [(contains {:count 2 :name 'sayhello})
                    (contains {:count 2 :name 'echo})]
                   :in-any-order))

(fact "missing-methods works with no overloads"
      (missing-methods Talky '())
      => (contains [(contains {:count 1 :name 'sayhello})
                    (contains {:count 2 :name 'sayhello})
                    (contains {:count 2 :name 'echo})]
                   :in-any-order))

(fact "generate-decorators works"
      (generate-decorators 'decorated-symbol Talky '((sayhello [this] "hello")))
      => ['(echo [this s] (echo decorated-symbol s))
          '(sayhello [this that] (sayhello decorated-symbol that))])

(defmacro is-protocol-test-macro [& forms]
  (filter is-protocol? forms))

(fact "detecting protocols works inside a macro"
      (macroexpand '(is-protocol-test-macro Talky "not a protocol" 1))
      => ['Talky])

(fact "intertwingle-decorators works"
      (intertwingle-decorators 'decorated-symbol ['Talky '(sayhello [this] "hello")])
      => ['Talky
          '(sayhello [this] "hello")
          '(echo [this s] (echo decorated-symbol s))
          '(sayhello [this that] (sayhello decorated-symbol that))])

(fact "intertwingle-decorators works with no overloads"
      (intertwingle-decorators 'decorated-symbol ['Talky])
      => ['Talky
          '(echo [this s] (echo decorated-symbol s))
          '(sayhello [this] (sayhello decorated-symbol))
          '(sayhello [this that] (sayhello decorated-symbol that))])

(fact "intertwingle-decorators works with multiple protocols"
      (intertwingle-decorators 'decorated-symbol ['Talky
                                                  '(sayhello [this] "hello")
                                                  'Goodbye
                                                  '(goodbye [this] "overloaded")])
      => ['Talky
          '(sayhello [this] "hello")
          '(echo [this s] (echo decorated-symbol s))
          '(sayhello [this that] (sayhello decorated-symbol that))
          'Goodbye
          '(goodbye [this] "overloaded")])

(fact "intertwingle-decorators works with multiple protocols and no overloads"
      (intertwingle-decorators 'decorated-symbol ['Talky 'Goodbye])
      => ['Talky
          '(echo [this s] (echo decorated-symbol s))
          '(sayhello [this] (sayhello decorated-symbol))
          '(sayhello [this that] (sayhello decorated-symbol that))
          'Goodbye
          '(goodbye [this] (goodbye decorated-symbol))])

(fact "decorate works with overloads"
      (let [to-decorate (TalkyImpl.)
            decorated (decorate to-decorate
                                Talky
                                (sayhello [this] (str "*ahem* " (sayhello to-decorate))))]
        (sayhello decorated) => "*ahem* hello"
        (sayhello decorated "world") => "hello world"
        (echo decorated "echo") => "echo"))

(deftype MultipleImpl []
  Talky
  (sayhello [this] "hello")
  (sayhello [this that] (str "hello " that))
  (echo [this s] s)

  Goodbye
  (goodbye [this] "goodbye"))

(fact "decorate works with overloads of multiple protocols"
      (let [to-decorate (MultipleImpl.)
            decorated (decorate to-decorate

                                Talky
                                (sayhello [this] (str "*ahem* " (sayhello to-decorate)))
                                (echo [this s] (str "did you say '" s "'?"))

                                Goodbye
                                (goodbye [this] (str "well " (goodbye to-decorate) " I guess")))]

        (sayhello decorated) => "*ahem* hello"
        (sayhello decorated "world") => "hello world"
        (echo decorated "echo") => "did you say 'echo'?"
        (goodbye decorated) => "well goodbye I guess"))