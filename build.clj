(ns build
  (:refer-clojure :exclude [test])
  (:require [clojure.tools.deps :as t]
            [clojure.tools.build.api :as b]
            [deps-deploy.deps-deploy :as dd]))

(def lib 'com.github.clojusc/colours)
(def version "0.1.0")
(def main 'clojusc.colours)
(def class-dir "target/classes")

(defn test "Run all the tests." [opts]
  (println "\nRunning tests...")
  (let [basis    (b/create-basis {:aliases [:test]})
        combined (t/combine-aliases basis [:test])
        cmds     (b/java-command
                  {:basis basis
                   :java-opts (:jvm-opts combined)
                   :main      'clojure.main
                   :main-args ["-m" "cognitect.test-runner"]})
        {:keys [exit]} (b/process cmds)]
    (when-not (zero? exit) (throw (ex-info "Tests failed" {}))))
  opts)

(defn- pom-template [version]
  [[:description "Another ANSI colour library for Clojure"]
   [:url "https://github.com/clojusc/colours"]
   [:licenses
    [:license
     [:name "Apache License 2.0"]
     [:distribution "repo"]
     [:url "http://www.apache.org/licenses/LICENSE-2.0"]]]
   [:developers
    [:developer
     [:name "oubiwann"]]]
   [:scm
    [:url "https://github.com/clojusc/colours"]
    [:connection "scm:git:https://github.com/clojusc/colours.git"]
    [:developerConnection "scm:git:ssh:git@github.com:clojusc/colours.git"]
    [:tag (str "v" version)]]])

(defn- uber-opts [opts]
  (let [jar-file (format "target/%s-%s.jar" lib version)]
    (assoc opts
           :lib lib
           :main main
           :version version
           :uber-file jar-file
           :jar-file jar-file
           :basis (b/create-basis {})
           :class-dir class-dir
           :src-dirs ["src"]
           :ns-compile [main]
           :pom-data  (pom-template version))))

(defn clean "Clean the build directory." [opts]
  (b/delete {:path "target"})
  (b/delete {:path "pom.xml"})
  opts)

(defn build "Run the CI pipeline of tests (and build the JAR)." [opts]
  (clean opts)
  (let [opts (uber-opts opts)]
    (println "\nWriting pom.xml...")
    (b/write-pom opts)
    (println "\nCopying source...")
    (b/copy-dir {:src-dirs ["resources" "src"] :target-dir class-dir})
    (println "\nBuilding JAR...")
    (b/jar opts))
  opts)

(defn ci "Run the CI pipeline of tests (and build the JAR)." [opts]
  (clean opts)
  (test opts)
  (build opts)
  opts)

(defn install "Install the JAR locally." [opts]
  (let [opts (uber-opts opts)]
    (b/install opts))
  opts)

(defn deploy "Deploy the JAR to Clojars." [opts]
  (let [{:keys [jar-file] :as opts} (uber-opts opts)]
    (dd/deploy {:installer :remote :artifact (b/resolve-path jar-file)
                :pom-file (b/pom-path (select-keys opts [:lib :class-dir]))}))
  opts)
