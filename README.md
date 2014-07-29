# bowen

Implements the [decorator pattern](http://en.wikipedia.org/wiki/Decorator_pattern) for clojure protocols.

You can think of it as middleware for protocols.

When I have a protocol with multiple methods, often I want to wrap an existing implementation,
overiding some but not all methods, like this:


```clj
(defprotocol Sheep
   (baa [this])
   (eat [this])
   (walk-around [this]))

(deftype LoudSheep [decorated-sheep]
   Sheep
   (baa [this] 
      (clojure.string/upper-case (baa decorated-sheep))

   ; Just delegate to the decorated instance
   (eat [this] 
      (eat decorated-sheep))
   (walk-around [this]
      (walk-around decorated-sheep)))
```

Providing explicit implementations of `eat` and `walk-around` is tedious. I want to be able to do this instead:

```clj
(deftype-decorator LoudSheep [decorated-sheep]
   Sheep
   (baa [this] 
      (clojure.string/upper-case (baa decorated-sheep)))

```

Bowen provides `deftype-decorator`, `defrecord-decorator` and `reify-decorator`, which allow you to do just this.

### Most Recent Release

With Leiningen:

``` clj
[savagematt/bowen "2.0"]
```

### Usage

Given these protocols:

```clj
(defprotocol Talky
  (sayhello [this])
  (echo [this s]))

(defprotocol Goodbye
  (goodbye [this]))
```

And this type that we want to decorate:

```clj
(deftype Talker []
  Talky
  (sayhello [this] "hello")
  (echo [this s] s)

  Goodbye
  (goodbye [this] "goodbye from the decorated talker"))
```

We can do this:

```clj
(deftype-decorator PoliteTalker [decorated-talker]
              Talky
              ; Delegate to the decorated instance and add our own behaviour
              (sayhello [this] (str "*ahem* " (sayhello decorated-talker)))

              ; Don't call the decorated instance at all
              (echo [this s] 
                  (str "excuse me, did you say '" s "'?"))

              Goodbye
              ; Just delegates to decorated-talker
              )

(let [talker (PoliteTalker. (Talker.)]
     (sayhello talker) 
     ;=> "*ahem* hello"
     
     (echo talker "cheese") 
     ;=> "excuse me, did you say 'cheese'?"
     
     (goodbye talker) 
     ;=> "goodbye from the decorated talker"
)
              
```

`defrecord-decorator` works in the same way.

To use `reify-decorator`, provide the instance to decorate as the first parameter:

```clj
(let [talker (Talker.)]
(reify-decorator talker
   Talky
   (sayhello [this] (str "*ahem* " (talker)))

   Goodbye))
```

See [core_test.clj](test/bowen/core_test.clj) for detailed usage examples

### Limitations

+ Only works on protocols, not interfaces
+ Will produce completely inscrutable error messages if you get the syntax wrong

### License

Distributed under the Eclipse Public License either version 1.0 or (at
your option) any later version.
