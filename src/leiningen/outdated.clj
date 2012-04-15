(ns leiningen.outdated
  (:require [leiningen.search :as search]
            [leiningen.core.project :as project]
            [leiningen.core.classpath :as classpath]))

(defn ^:internal parse-result [{:keys [u d]}]
  (let [[group artifact version classifier] (.split u "\\|")
        group (if (not= group artifact) group)
        identifier [(symbol group artifact) version]]
    (if d
      [identifier d]
      [identifier])))

(defn- search-all [repo query]
  (->> (map #(search/search-repository repo query %) (range 1 10))
       (take-while identity)
       (apply concat)))

(defn- latest-version [project dep]
  (let [group-id (or (namespace dep) (name dep))
        query (str "g:" group-id " AND a:" (name dep)
                   " AND NOT v:SNAPSHOT")]
    (->> (:repositories project (:repositories project/defaults))
         (mapcat #(search-all % query))
         (last))))

(defn outdated
  "List dependencies which have new releases available."
  [project & args]
  (doseq [[dep version & _] (:dependencies project)]
    (when-let [result (latest-version project dep)]
      (let [[[dep2 version2] desc] (parse-result result)]
        (when (not= version version2)
          (println (pr-str [dep2 version2])
                   "is available but we use"
                   (pr-str version)))))))