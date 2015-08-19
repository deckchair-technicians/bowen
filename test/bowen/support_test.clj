(ns bowen.support-test
  (:require [midje.sweet :refer :all]
            [bowen.core-test :refer :all]
            [bowen.core :refer :all]))

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
      (add-decoration 'decorated-symbol ['Talky '(sayhello [this] "hello")])
      => ['Talky
          '(sayhello [this] "hello")
          '(echo [this s] (echo decorated-symbol s))
          '(sayhello [this that] (sayhello decorated-symbol that))])

(fact "intertwingle-decorators works with no overloads"
      (add-decoration 'decorated-symbol ['Talky])
      => ['Talky
          '(echo [this s] (echo decorated-symbol s))
          '(sayhello [this] (sayhello decorated-symbol))
          '(sayhello [this that] (sayhello decorated-symbol that))])

(fact "intertwingle-decorators works with multiple protocols"
      (add-decoration 'decorated-symbol ['Talky
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
      (add-decoration 'decorated-symbol ['Talky 'Goodbye])
      => ['Talky
          '(echo [this s] (echo decorated-symbol s))
          '(sayhello [this] (sayhello decorated-symbol))
          '(sayhello [this that] (sayhello decorated-symbol that))
          'Goodbye
          '(goodbye [this] (goodbye decorated-symbol))])
