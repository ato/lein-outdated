(ns leiningen.outdated
  (:require [leiningen.outdated.utils :as u]
            [leiningen.core.project :as project]))

(defn- get-repository-urls
  "Get Repository URLs from Project."
  [project]
  (->>
    (:repositories project (:repositories project/defaults))
    (map second)
    (map :url)))

(defn- check-packages
  "Check the packages found at the given key in the project map.
   Will check the given repository urls for metadata."
  [repos packages]
  (let [retrieve! (partial u/retrieve-metadata! repos)]
    (doseq [{:keys [group-id artifact-id version] :as dep} 
            (map u/dependency-map packages)]
      (when-let [mta (retrieve! group-id artifact-id)]
        (when-let [latest (u/latest-version mta)]
          (when (u/version-outdated? version latest)
            (println
              (str "[" (:dependency-str dep) " \"" (:version-str latest) "\"]")
              "is available but we use"
              (str "\"" (:version-str version) "\""))))))))

(def ^:private KEYS
  "Mapping of command-line parameter to project map key
   containing package vectors."
  {":dependencies" :dependencies
   ":plugins" :plugins})

(defn outdated
  "List dependencies which have newer versions available."
  [project & args]
  (let [repos (get-repository-urls project)]
    (doseq [^String s (or (seq args) [":dependencies"])]
      (when-let [k (get KEYS s)]
        (check-packages repos (get project k))))))
