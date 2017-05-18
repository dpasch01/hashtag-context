# Hashtag Context Extractor

----
## How it works?
Basically it receives the tweets from stream and using Lucene's NRT, indexes the tokens from tweets using N-Grams. By searching for a hashtag, it receives the top 5 keywords describing the hashtag at that moment of the indexing.

----
## Example:
**Hashtag**: *\#VoteLeave*

**Context**: *[leave, vote]*

----
## Issues
It might not scale in the long run. Most of the index is memory based. I couldn't test it out yet.
