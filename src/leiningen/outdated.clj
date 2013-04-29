(ns leiningen.outdated
  (:require [leiningen.search :as search]
            [leiningen.core.project :as project]
            [clojure.java.io :as io])
  (:import (org.apache.lucene.search BooleanQuery BooleanClause$Occur)
           (org.apache.maven.index ArtifactInfo IteratorSearchRequest MAVEN)
           (org.apache.maven.index.creator
            JarFileContentsIndexCreator MavenPluginArtifactInfoIndexCreator
            MinimalArtifactInfoIndexCreator)
           (org.apache.maven.index.expr SourcedSearchExpression)))

;;;; <copied from leiningen.search>

(def ^:private default-indexers [(MinimalArtifactInfoIndexCreator.)
                                 (JarFileContentsIndexCreator.)
                                 (MavenPluginArtifactInfoIndexCreator.)])

(defn- add-context [[id {:keys [url]}]]
  (.addIndexingContextForced search/indexer id url nil
                             (search/index-location url)
                             url nil default-indexers))

(defn- remove-context [context]
  (.removeIndexingContext search/indexer context false))

(defn- refresh? [url project]
  (if-not (:offline? project)
    (< (.lastModified (io/file (search/index-location url) "timestamp"))
       (- (System/currentTimeMillis) 86400000))))

(defn- update-indices [project contexts]
  (doseq [context contexts]
    (when (refresh? (.getRepositoryUrl context) project)
      (search/update-index context))))

;;;; </copied from leiningen.search>

(defn- construct-subquery [[field expression]]
  (let [search-expression (SourcedSearchExpression. expression)]
    (.constructQuery search/indexer field search-expression)))

(defn- construct-query [& field-expr-pairs]
  (let [query (BooleanQuery.)]
   (doseq [subquery (map construct-subquery (partition 2 field-expr-pairs))]
     (.add query subquery BooleanClause$Occur/MUST))
   query))

(defn- artifact-search [context dep]
  (let [q (construct-query MAVEN/GROUP_ID (or (namespace dep) (name dep))
                           MAVEN/ARTIFACT_ID (name dep))
        request (IteratorSearchRequest. q context)]
    (.searchIterator search/indexer request)))

(defn- latest-artifact [contexts project dep]
  (with-open [response (artifact-search contexts dep)]
    (letfn [(not-snapshot? [artifact]
              (not (.endsWith (str (.getArtifactVersion artifact)) "-SNAPSHOT")))]
      (first (sort ArtifactInfo/VERSION_COMPARATOR
                   (filter not-snapshot? (seq response)))))))

(defn- latest-version [contexts project dep]
  (str (.getArtifactVersion (latest-artifact contexts project dep))))

(defn- get-repos [project]
  (:repositories project (:repositories project/defaults)))

(defn outdated
  "List dependencies which have newer versions available."
  [project & args]
  (let [contexts (doall (map add-context (:repositories project)))]
    (try
      (update-indices project contexts)
      (doseq [[dep version & _] (:dependencies project)]
        (when-let [latest (latest-version contexts project dep)]
          (when (not= version latest)
            (println (pr-str [dep latest])
                     "is available but we use"
                     (pr-str version)))))
      (finally
       (doall (map remove-context contexts))))))
