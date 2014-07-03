# bowen

Implements the [decorator pattern](http://en.wikipedia.org/wiki/Decorator_pattern) for clojure protocols.

### Usage

See [last test](test/bowen/core-test.clj) for detailed usage example

### Limitations

+ Uses `reify`, which isn't very efficient
+ Only works on protocols, not interfaces
+ Will produce completely inscrutable error messages if you get the syntax wrong


### The Most Recent Release

With Leiningen:

``` clj
[savagematt/bowen "1.0"]
```

With Maven:

``` xml
<dependency>
  <groupId>savagematt</groupId>
  <artifactId>bowen</artifactId>
  <version>1.0</version>
</dependency>
```

### License

Distributed under the Eclipse Public License either version 1.0 or (at
your option) any later version.
