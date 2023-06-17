package icapa.assertion.attributes.generic;

import org.apache.ctakes.assertion.attributes.generic.GenericAttributeClassifier;
import org.apache.uima.jcas.tcas.Annotation;

import java.util.HashMap;

public class GenericAttributeDiscussionContextClassifier extends GenericAttributeClassifier {

    // Method Hiding to set whether an Annotation is in a discussion context
    // https://coderanch.com/wiki/659959/Overriding-Hiding
    private static boolean isDiscussionContext(Annotation arg) {
        //System.out.println("Checking if \"" + arg.getCoveredText() + "\" (" + arg.getBegin() + ", " + arg.getEnd() + ") " + " isDiscussionContext()");
		
		// Define regexes that capture discussion context.
        String oneTokenDiscussionContextRegex = "(discuss|ask|understand|understood|tell|told|mention|talk|speak|spoke|address|agree|explore|recommend|report(s|ed|edly)?|review|request(ed)?|considering|state|consent(ed)?|decided?).*";
        String multiTokenDiscussionContextRegex = ".*(ask(ed)? about|(agreeable|agree(d)?|consent(ed)?) to|inform(ed)? about|(would be )?interested in|recommendation is|referral for|reportedly completed|would like to( engage in)?).*";
		
		// isDiscussion = true if Annotation `arg` text matches one of the above regexes.
        boolean isDiscussion = arg.getCoveredText().toLowerCase().matches(oneTokenDiscussionContextRegex) || arg.getCoveredText().toLowerCase().matches(multiTokenDiscussionContextRegex);

		// Define regexes that capture a temporal or status ("paused", "completed") context.
		// This is not quite conceptually the same as a "generic" context, but I don't know 
		// a better place to set this attribute for now. This is also a hacky way
		// to capture negated temporal contexts.
        // TODO: Find more appropriate location for this.
        String oneTokenTemporalContextRegex = "(paused?|resumed?|start(ed)?|suspend(ed)?).*";
        String multiTokenTemporalContextFutureRegex = ".*((plan(ned)? to|will) ((likely|promptly) )?(begin|complete|continue|implement|start|transition|undergo)).*";
        String multiTokenTemporalContextOtherRegex = ".*(still on (temporary )?pause|recently completed|scheduled to complete).*";
        String multiTokenTemporalContextNegatedRegex = ".*((((does not want|chose not) )to continue( with)?)|had not followed through with|not able to tolerate|not (been )?referred (for|to)?).*";
        
		// isTemporal = true if Annotation `arg` text matches one of the above regexes.
		boolean isTemporal = arg.getCoveredText().toLowerCase().matches(oneTokenTemporalContextRegex) ||
                             arg.getCoveredText().toLowerCase().matches(multiTokenTemporalContextFutureRegex) ||
                             arg.getCoveredText().toLowerCase().matches(multiTokenTemporalContextOtherRegex) ||
                             arg.getCoveredText().toLowerCase().matches(multiTokenTemporalContextNegatedRegex);

		// If isDiscussion or isTemporal is true,
		// Return true to indicate Annotation `arg` is in a discussion context
		// This will be used as one feature in the final determination of whether 
		// the generic attribute should be set to true
        return isDiscussion || isTemporal;
    }


}
