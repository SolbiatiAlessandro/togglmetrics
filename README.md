## togglmetrics

A web-app to analyise your track.toggl.com metrics.

The first commit is also a good starter app for the tech stack below.

## tech stack

This is a clojurescript app that uses `lein figwheel`.
This app compiles clojurescript to javascript, and runs node.js with the express.js framework.

The benefit of this tech stack is that you can write clojure (like on luminus) but using express.js that has a bigger community, and you can do so by the interop with cljs and js

To run the app

`npm install`
`lein figwheel`

and in a different terminal

`node dev/togglmetrics.js`

## production setup on ubuntu

I spin up a digitial ocean box at http://161.35.34.143/

```
# get java 8 (lein figwheel doesn't work with java > 8), see https://github.com/bhauman/lein-figwheel/issues/612
apt install openjdk-8-jre-headless
# follow linux installation of clojure
# https://clojure.org/guides/getting_started#_installation_on_linux
# follow linux installation of lein
# https://stackoverflow.com/a/36940738/7067655
# now follow instruction above "to run the app"
```
