(ns sdos-site.utils)

(defn assoc-if
  [m cond k v]
  (let [assoc-fn (if (vector? k) assoc-in assoc)]
    (if cond
      (assoc-fn m k v)
      m)))
