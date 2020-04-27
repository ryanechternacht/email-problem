# Email Problem

Ryan Echternacht's attempt at the spam problem (see spam-problem.org here). This was a fun transducers project that I extended with some core.async action to do a "live" system with progress reporting. 

## Usage

Run the project directly:

    $ clojure -m emails.core

Run the project's tests:

    $ clojure -A:test:runner

### v1

The first version of the problem was completed with transducers and tested using datasets generated from the specs provided. These are found in the `run-simple` function in `core.clj` and `process_emails.clj` file. 

This version loads a file of emails, runs them through the transducer chain, and spits out the result. Tests were written for this as well. 

#### Expansions beyond the original scope

1) I configure the transducers using a settings map. The defaults match the problem spec, but new values can be provided that will be merged into the default map, then used. 
2) I extended the "only send 1 email per prospect" requirement to be configurable -- e.g. "send <= 3 emails per prospect".
3) I condensed the global and running mean into the same code. 

### v2

To expand upon the first versoin, I wanted to support progress tracking throughout the runtime. This is found in `run-advanced` in `core.clj` and the `process_emails_v2.clj` (with accompanying tests). 

The main approach here was to attach our transducer to a core.async channel, so that I could process emails in batches and report on their progress. To track progress, I flow a atom into each transducer so it can record the reason for each rejected email. 

The main code sets up the channels and atoms, generates emails using the spec, and batch feeds them into the channel, reporting on the progress every so often. All of this is configurable using the `defaults` map. 

### Thoughts

1) Performance seems a bit slow. The culprit appears to be the global-mean transduction step. This makes some sense, since it's essentially O(n^2). 
2) I'm surprised that more emails don't get rejected for being too spammy (spam score > 0.3). My tests and general inspection show the transducer works correctly, but if we're generating data between 0 and 1, I would expect that distribution to be even and therefore, that more emails would get rejected.
3) I'm not sure if the atom/tracking approach is idiomatic. I could also have returned 2(+) datasets to represent approved/rejected. This might have been better?

## License

Copyright © 2020 Ryan

Distributed under the Eclipse Public License either version 1.0 or (at
your option) any later version.
