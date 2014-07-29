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

(fact "reify-decorator works with overloads of multiple protocols"
  (let [to-decorate (Talker.)
        decorated (reify-decorator to-decorate

                    Talky
                    (sayhello [this] (str "*ahem* " (sayhello to-decorate)))
                    (echo [this s] (str "did you say '" s "'?"))

                    Goodbye
                    (goodbye [this] (str "well " (goodbye to-decorate) " I guess")))]

    (sayhello decorated) => "*ahem* hello"
    (sayhello decorated "world") => "hello world"
    (echo decorated "echo") => "did you say 'echo'?"
    (goodbye decorated) => "well goodbye I guess"))

(deftype-decorator DefTypeDecorator [to-decorate hello-prefix]
  Talky
  (sayhello [this] (str hello-prefix (sayhello to-decorate)))
  (echo [this s] (str "did you say '" s "'?"))

  Goodbye
  (goodbye [this] (str "well " (goodbye to-decorate) " I guess")))

(fact "deftype-decorator deftypes a class rather than using reify"
  (let [decorated (DefTypeDecorator. (Talker.) "*ahem* ")]

    (sayhello decorated) => "*ahem* hello"
    (sayhello decorated "world") => "hello world"
    (echo decorated "echo") => "did you say 'echo'?"
    (goodbye decorated) => "well goodbye I guess"))

(defrecord-decorator DefRecordDecorator [to-decorate hello-prefix]
  Talky
  (sayhello [this] (str hello-prefix (sayhello to-decorate)))
  (echo [this s] (str "did you say '" s "'?"))

  Goodbye
  (goodbye [this] (str "well " (goodbye to-decorate) " I guess")))

(fact "defrecord-decorator defrecords a class rather than using reify"
  (let [decorated (DefRecordDecorator. (Talker.) "*ahem* ")]

    (sayhello decorated) => "*ahem* hello"
    (sayhello decorated "world") => "hello world"
    (echo decorated "echo") => "did you say 'echo'?"
    (goodbye decorated) => "well goodbye I guess"))
