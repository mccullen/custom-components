// Usage: <section-header>||<regex>
// - Note: Case insensitive and multi-line (the ^ and $ chars refer to the start and end of the line, not the document)

// ***Examples***
//General Exam||^[\t ]*(?:(?:PE:)|(?:O:)|(?:(?:REVIEW (?:OF )?)?(?:GENERAL(?: PHYSICAL)?|PHYSICAL) (?:EXAM(?:INATION)?|STATUS|APPEARANCE|CONSTITUTIONAL)S?(?: SYMPTOMS?)?[\t ]*:?))[\t ]*$
//Vital Signs||^[\t ]*VITAL(?:S|(?: (?:SIGNS|NOTES)))[\t ]*:?[\t ]*$
//Identifying Data||^[\t ]*IDENTIFYING DATA[\t ]*:?[\t ]*$


// *** Section Headers to match ***

// Matches "findings" at the start of line (not including whitespace). The ^ refers to start of line here, not negation
// Additional matches: findings, finding(s), finding
Finding||^\s*finding(s|\(s\))?\s*
Conclusion||^\s*(conclusion|impression)(s|\(s\))?\s*
History||^\s*(provided |clinical )?(history|indication(s|\(s\))?)\s*
Technique||^\s*technique(s|\(s\))?\s*
Comparison||^\s*comparison(s|\(s\))?\s*
Procedure||^\s*(procedure|exam|examination|study)(s|\(s\))?\s*
Critical_Finding||^\s*notification of critical finding(s|\(s\))?\s*
Information||^\s*(clinical |provided )?information\s*
Recommendation||^\s*recommendation(s|\(s\))?\s*
Addendum||^\s*addendum(s|\(s\))?\s*

// This just resets the segment at the end of a template. For example, if more annotations apprear after a critical
// result template but before the next header (like the ones mentioned above), it will simply be labeled SIMPLE_SEGMENT,
// which is the default that will also be used before the first header is encountered. 
SIMPLE_SEGMENT||\s*\*\*\*END

// *** Old expressions for reference ***
// We are using custom annotators to capture critical/urgent result templates now
//CRITICAL_RESULT_COMMUNICATION||\s*\*\*\*critical result communication\*\*\*[\s\S]*?\*\*\*END CRITICAL RESULT COMMUNICATION\*\*\*\s*
//CRITICAL_RESULT_COMMUNICATION||\s*\*\*\*critical result communication\*\*\*\s*

// This was used to capture finding headers that were not contained within critcal/urgent result templates
// Not necessary now due to the use of our custom regex section annotator
//Finding||^\s*finding(s|\(s\))?\s*(?!(?<=\*\*\*[\s\S]{0,1000}?)[\s\S]{0,1000}?\*\*\*END)


// For,179_20257591 consider NOTE segment
// Consider: BI/LUNG/TI-RAD, Contrast (already under technique, but do not include for now)