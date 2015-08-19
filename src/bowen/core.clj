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

(defn provided-specs [forms]
  (map (fn [f]
         {:count (count (second f))
          :name  (first f)})
       forms))

(defn missing-sigs [protocol forms]
  (let [provided (set (provided-specs forms))]
    (filter (fn [expected-sig]
              (not (provided (dissoc expected-sig :arglist))))
            (expected-sigs protocol))))

(defn is-protocol? [form]
  (when (symbol? form)
    (#'clojure.core/protocol? (deref (resolve form)))))

(defn ->spec [sig & body]
  (concat (list (:name sig) (:arglist sig))
          body))

(defn ->sig->call-decorated
  [decorated-sym]
  (fn [sig]
    (concat (list (:name sig) decorated-sym) (drop 1 (:arglist sig)))))

(defn add-missing-specs
  "Given:
  (defprotocol X
    (a [this])
    (b [this)))

  (add-missing-specs (generate-call-to-decorated 'decorated) '(X (a [this] \"a\")))

  Will return:

  '(X
     (a [this] \"a\")
     (b [this] (b decorated)))"
  [sig->spec-body protocols-and-impls]
  (loop [protocols-and-impls protocols-and-impls
         result              []]
    (if (empty? protocols-and-impls)
      result
      (let [protocol-sym      (first protocols-and-impls)
            provided-fn-specs (take-while (comp not is-protocol?)
                                          (next protocols-and-impls))
            missing-sigs     (missing-sigs (deref (resolve protocol-sym)) provided-fn-specs)]
        (recur (drop (+ 1 (count provided-fn-specs)) protocols-and-impls)
               (doall (concat result
                              [protocol-sym]
                              provided-fn-specs
                              (map #(->spec % (sig->spec-body %)) missing-sigs))))))))
(defmacro reify-decorator
  "It is suggested you use decorate instead"
  [decorated & opts+specs]
  (let [full-opts+specs (add-missing-specs (->sig->call-decorated decorated) opts+specs)]
    `(reify ~@full-opts+specs)))

(defmacro deftype-decorator
  "It is suggested you use decorate instead"
  [type-sym args & opts+specs]
  (let [decorated       (first args)
        full-opts+specs (add-missing-specs (->sig->call-decorated decorated) opts+specs)]
    `(deftype ~type-sym ~args
       ~@full-opts+specs)))

(defmacro defrecord-decorator
  "It is suggested you use decorate instead"
  [type-sym args & opts+specs]
  (let [decorated       (first args)
        full-opts+specs (add-missing-specs (->sig->call-decorated decorated) opts+specs)]
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
