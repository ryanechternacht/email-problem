# ladders

Ryan Echternacht's attempt at the spam problem (see spam-problem.org here). This was a fun transducers project that I extended with some core.async action to do a "live" system with progress reporting. 

## Usage

FIXME: explanation

Run the project directly:

    $ clojure -m emails.ladders

Run the project's tests:

    $ clojure -A:test:runner

### v1

The first version of the problem was completed with transducers and tested using datasets generated from the specs provided. These are found in the `run-simple` function in `ladders.clj` and `process_emails.clj` file. 

This version loads a file of emails, runs them through the transducer chain, and spits out the result. Tests were written for this as well. 

#### Expansions beyond the original scope

1) I configure the transducers using a settings map. The defaults match the problem spec, but new values can be provided that will be merged into the default map, then used. 
2) I extended the "only send 1 email per prospect" requirement to be configurable -- e.g. "send <= 3 emails per prospect".
3) I condensed the global and running mean into the same code. 

### v2

To expand upon the first versoin, I wanted to support progress tracking throughout the runtime. This is found in `run-advanced` in `ladders.clj` and the `process_emails_v2.clj` (with accompanying tests). 

The main approach here was to attach our transducer to a core.async channel, so that I could process emails in batches and report on their progress. To track progress, I flow a atom into each transducer so it can record the reason for each rejected email. 

The main code sets up the channels and atoms, generates emails using the spec, and batch feeds them into the channel, reporting on the progress every so often. All of this is configurable using the `defaults` map. 

## License

Copyright © 2020 Ryan

Distributed under the Eclipse Public License either version 1.0 or (at
your option) any later version.
