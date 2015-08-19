(ns bowen.support-test
  (:require [midje.sweet :refer :all]
            [bowen.core-test :refer :all]
            [bowen.core :refer :all]))

(fact "expected-sigs returns sigs from a protocol in a form we can use later to compare with name and
       parameter count of provided specs in a deftype, defrecord or reify"
      (expected-sigs Talky)
      => (contains [{:count 1 :name 'sayhello :arglist [(symbol 'this)]}
                    {:count 2 :name 'sayhello :arglist [(symbol 'this) (symbol 'that)]}
                    {:count 2 :name 'echo :arglist [(symbol 'this) (symbol 's)]}]
                   :in-any-order))

(fact "provided-specs returns name and parameter count metadata from opts+specs that we can compare with the output
       of expected-sigs to see which sigs have not been provided"
      (provided-specs '((sayhello [this] "hello") (sayhello [this that] "hello") (echo [this s] s)))
      => (contains [{:count 1 :name 'sayhello}
                    {:count 2 :name 'sayhello}
                    {:count 2 :name 'echo}]
                   :in-any-order))

(fact "missing-sigs takes a protocol and an opts+specs form and returns the missing sigs from the protcol"
      (missing-sigs Talky '((sayhello [this] "hello")))
      => (contains [(contains {:count 2 :name 'sayhello})
                    (contains {:count 2 :name 'echo})]
                   :in-any-order))

(fact "missing-sigs works with no provided specs at all"
      (missing-sigs Talky '())
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
