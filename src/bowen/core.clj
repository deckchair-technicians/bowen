(ns bowen.core)

(defn expected-sigs [protocol]
  (->> protocol
       :sigs
       (vals)
       (mapcat (fn [{:keys [name arglists]}]
                 (map (fn [arglist]
                        {:count   (count arglist)
                         :arglist arglist
                         :name    name})
                      arglists)))))

(defn actual-sigs [forms]
  (map (fn [f]
         {:count (count (second f))
          :name  (first f)})
       forms))

(defn missing-methods [protocol forms]
  (let [actual (set (actual-sigs forms))]
    (filter (fn [expected-sig]
              (not (actual (dissoc expected-sig :arglist))))
            (expected-sigs protocol))))

(defn is-protocol? [form]
  (when (symbol? form)
    (#'clojure.core/protocol? (deref (resolve form)))))

(defn generate-decorators [decorated-symbol protocol overloads]
  (map (fn [missing]
         (list (:name missing) (:arglist missing)
               (concat (list (:name missing) decorated-symbol) (drop 1 (:arglist missing)))))
       (missing-methods protocol overloads)))

(defn add-decoration [decorated-sym protocols-and-impls]
  (loop [protocols-and-impls protocols-and-impls
         res []]
    (if (not (empty? protocols-and-impls))
      (let [protocol (first protocols-and-impls)
            overloads (take-while (comp not is-protocol?)
                                  (next protocols-and-impls))]
        (recur (drop (+ 1 (count overloads)) protocols-and-impls)
               (doall (concat res
                              [protocol]
                              overloads
                              (generate-decorators decorated-symbol (deref (resolve protocol)) overloads)))))
      res)))

(defmacro reify-decorator
  "It is suggested you use decorate instead"
  [decorated & opts+specs]
  (let [full-opts+specs (add-decoration decorated opts+specs)]
    `(reify ~@full-opts+specs)))

(defmacro deftype-decorator
  "It is suggested you use decorate instead"
  [type-sym args & opts+specs]
  (let [decorated       (first args)
        full-opts+specs (add-decoration decorated opts+specs)]
    `(deftype ~type-sym ~args
       ~@full-opts+specs)))

(defmacro defrecord-decorator
  "It is suggested you use decorate instead"
  [type-sym args & opts+specs]
  (let [decorated       (first args)
        full-opts+specs (add-decoration decorated opts+specs)]
    `(defrecord ~type-sym ~args
       ~@full-opts+specs)))

(defmacro decorate
  "Wrap around deftype or defrecord in order to create a type/record which takes a decorated instance as its first
  constructor argument and delegates calls to any undefined functions.

  Alternatively, use (decorate x (reify SomeProtocol)) to do similar with reify forms"
  [& forms]
  (let [decoration-type (if (= 'reify (second (flatten forms)))
                          :reify
                          (if (= 'deftype (first (first forms)))
                            :type
                            (if (= 'defrecord (first (first forms)))
                              :record
                              (throw (UnsupportedOperationException. "Unsupported decorate contents")))))]
    (case decoration-type
      :reify
      `(reify-decorator ~(first forms) ~@(rest (second forms)))
      :type
      `(deftype-decorator ~@(rest (first forms)))
      :record
      `(defrecord-decorator ~@(rest (first forms))))))
