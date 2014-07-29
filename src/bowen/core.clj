(ns bowen.core)

(defn expected-sigs [protocol]
  (mapcat (fn [sig] (map (fn [arglist] {:count   (count arglist)
                                        :arglist arglist
                                        :name    (:name sig)})
                         (:arglists sig)))
          (vals (:sigs protocol))))

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

(defn intertwingle-decorators [decorated-symbol protocols-and-impls]
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

(defmacro reify-decorator [decorated & opts+specs]
  (let [full-opts+specs (intertwingle-decorators decorated opts+specs)]
    `(reify ~@full-opts+specs)))

(defmacro deftype-decorator [type-sym args & opts+specs]
  (let [decorated (first args)
        full-opts+specs (intertwingle-decorators decorated opts+specs)]
    `(deftype ~type-sym ~args
       ~@full-opts+specs)))

(defmacro defrecord-decorator [type-sym args & opts+specs]
  (let [decorated (first args)
        full-opts+specs (intertwingle-decorators decorated opts+specs)]
    `(defrecord ~type-sym ~args
       ~@full-opts+specs)))
