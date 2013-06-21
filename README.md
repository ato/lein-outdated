# lein-outdated

A Leiningen plugin that lists newer available versions of dependencies.

It uses the Lucene indexes downloaded by `lein search`.

## Usage

Requires Leiningen 2.0.0-preview8 or later.

Put `[lein-outdated "1.0.2"]` into the `:plugins` vector of your
`:user` profile in ~/.lein/profiles.clj.

Then run `lein outdated` in your project directory:

    $ lein outdated
    [org.clojure/clojure "1.4.0-beta6"] is available but we use "1.3.0"
    [org.clojure/tools.cli "0.2.0"] is available but we use "0.2.1"
    [compojure "1.0.2"] is available but we use "1.0.1"
    [cheshire "4.0.0"] is available but we use "2.2.2"

## License

Copyright © 2012 Alex Osborne and contributors

Parts based on code from Leiningen.

Leiningen Source Copyright © 2009-2012 Phil Hagelberg, Alex Osborne, Dan Larkin and [contributors](https://github.com/technomancy/leiningen/contributors). 

Distributed under the Eclipse Public License, the same as Leiningen and Clojure.

Idea suggested by `guns` on #leiningen.
