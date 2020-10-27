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
