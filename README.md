# lein-outdated

A Leiningen plugin that lists newer available versions of dependencies.

It uses the Lucene indexes downloaded by `lein search`.

## Usage

This plugin currently requires Leiningen 2.  You could backport it
and send me a pull request.

Put `[lein-outdated "0.1.0"]` into the `:plugins` vector of your
`:user` profile in ~/.lein/profiles.clj.

Then run `lein outdated` in your project directory:

    $ lein outdated
    [org.clojure/clojure "1.4.0-beta6"] is available but we use "1.3.0"
    [org.clojure/tools.cli "0.2.0"] is available but we use "0.2.1"
    [compojure "1.0.2"] is available but we use "1.0.1"
    [cheshire "4.0.0"] is available but we use "2.2.2"

Use `lein search --update` to update the indexes which are searched.

## Known issues

This is a quick proof of concept.  If a dependency is available in
multiple repositories then things won't work right.

We're also relying on the order that search results are returned from
Lucene to figure out what the latest version is.  We should probably
sort the versions ourselves instead.

## License

Copyright © 2012 Alex Osborne

Distributed under the Eclipse Public License, the same as Clojure.

Idea suggested by `guns` on #leiningen.
