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

(deftype Talker []
  Talky
  (sayhello [this] "hello")
  (sayhello [this that] (str "hello " that))
  (echo [this s] s)

  Goodbye
  (goodbye [this] "goodbye"))

(clojure.core/deftype
  DefTypeDecorator
  [to-decorate hello-prefix]
  Talky
  (sayhello [this] (str hello-prefix (sayhello to-decorate)))
  (echo [this s] (str "did you say '" s "'?"))
  (sayhello [this that] (sayhello to-decorate that))
  Goodbye
  (goodbye [this] (str "well " (goodbye to-decorate) " I guess")))

(fact "reify-decorator works with overloads of multiple protocols"
  (let [to-decorate (Talker.)
        decorated (decorate to-decorate
                    (reify

                      Talky
                      (sayhello [this] (str "*ahem* " (sayhello to-decorate)))
                      (echo [this s] (str "did you say '" s "'?"))

                      Goodbye
                      (goodbye [this] (str "well " (goodbye to-decorate) " I guess"))))]

    (sayhello decorated) => "*ahem* hello"
    (sayhello decorated "world") => "hello world"
    (echo decorated "echo") => "did you say 'echo'?"
    (goodbye decorated) => "well goodbye I guess"))

(decorate
  (deftype DefTypeDecorator [to-decorate hello-prefix]
    Talky
    (sayhello [this] (str hello-prefix (sayhello to-decorate)))
    (echo [this s] (str "did you say '" s "'?"))

    Goodbye
    (goodbye [this] (str "well " (goodbye to-decorate) " I guess"))))

(fact "deftype-decorator deftypes a class rather than using reify"
  (let [decorated (DefTypeDecorator. (Talker.) "*ahem* ")]

    (sayhello decorated) => "*ahem* hello"
    (sayhello decorated "world") => "hello world"
    (echo decorated "echo") => "did you say 'echo'?"
    (goodbye decorated) => "well goodbye I guess"))

(decorate
  (defrecord DefRecordDecorator [to-decorate hello-prefix]
    Talky
    (sayhello [this] (str hello-prefix (sayhello to-decorate)))
    (echo [this s] (str "did you say '" s "'?"))

    Goodbye
    (goodbye [this] (str "well " (goodbye to-decorate) " I guess"))))

(fact "defrecord-decorator defrecords a class rather than using reify"
  (let [decorated (DefRecordDecorator. (Talker.) "*ahem* ")]

    (sayhello decorated) => "*ahem* hello"
    (sayhello decorated "world") => "hello world"
    (echo decorated "echo") => "did you say 'echo'?"
    (goodbye decorated) => "well goodbye I guess"))


(decorate
  (deftype TypeHintedDefType [to-decorate ^String hello-prefix]
                     Talky
                     (sayhello [this] (str hello-prefix (sayhello to-decorate)))))

(fact "deftype-decorator works with type hints"
  (let [decorated (TypeHintedDefType. (Talker.) "*ahem* ")]

    (sayhello decorated) => "*ahem* hello"))

(decorate
  (defrecord TypeHintedDefRecord [to-decorate ^String hello-prefix]
    Talky
    (sayhello [this] (str hello-prefix (sayhello to-decorate)))))

(fact "defrecord-decorator works with type hints"
  (let [decorated (TypeHintedDefRecord. (Talker.) "*ahem* ")]

    (sayhello decorated) => "*ahem* hello"))
