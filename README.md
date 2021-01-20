## Packages
* models: Simple model objects with getters/setters
* services: Anything with business/implementation logic that you may want to stub out
* cr: Collection Readers
* cc: CAS Consumers
* ae: Analysis Engines

## Attributes
* Confidence: Created by dictionary lookup, not probabalistic algorithm, and is never set.
it will always be 0 with the default dictionary lookup procedure. The default procedure
is a strict lookup against a term in the dictionary and no lookup has any more validity
than any other, so it is pretty much meaningless. 
  * http://mail-archives.apache.org/mod_mbox/ctakes-dev/201801.mbox/browser
* Conditional (bool):Whether the relation is conditional/hypothetical. Example: "If the patient's tumor returns, we can treat it with Oxaliplatin."
                     This instantiates a manages/treats relation which should be marked as conditional=true, with the "if" marked as the cue. 
* Generic (bool): Is the named entity or event used in a generic way or in the context of a person's
history?
* Polarity (-1 | 1): negated=-1
* PartsOfSpeech (string): Comma separated list of the parts of speech tags in the text span
  * https://www.ling.upenn.edu/courses/Fall_2003/ling001/penn_treebank_pos.html
* preferredText (string): preferredText is the preferred term. normally his is
 the UMLS preferred name.
* refsem (string): 
* codingScheme (string): 
* score (int?):Word Sense disambiguation: if this named entity is assigned multiple 
ontologyConcepts, the score represents how similar this sense is to surrounding senses
 (higher scores = more likely to be the correct sense) 
* subject ("patient"|"other"...): Who does the event or entity refer to?
* trueText (string): The actual text in the note (between beginIndex and endIndex).
* tui (string): 
* uncertainty (0|1): Is the event or entity uncertain? 1=uncertain
* documentId (string): 
* beginIndex (int): 
* endIndex (int):

