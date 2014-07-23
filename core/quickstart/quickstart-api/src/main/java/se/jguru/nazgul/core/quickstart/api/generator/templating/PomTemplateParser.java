package se.jguru.nazgul.core.quickstart.api.generator.templating;

import org.apache.commons.lang3.Validate;
import se.jguru.nazgul.core.parser.api.TokenParser;
import se.jguru.nazgul.core.parser.api.agent.ParserAgent;

import java.util.ArrayList;
import java.util.List;

/**
 *
 * @author <a href="mailto:lj@jguru.se">Lennart J&ouml;relid</a>, jGuru Europe AB
 */
public class PomTemplateParser implements TokenParser {

    // Internal state
    private List<ParserAgent> parseAgents = new ArrayList<ParserAgent>();

    /**
     * Adds a parserAgent to the list of known AbstractParserAgents.
     *
     * @param parserAgent the parserAgent to add.
     * @throws IllegalArgumentException if the parserAgent argument was <code>null</code>.
     */
    @Override
    public final void addAgent(final ParserAgent parserAgent) throws IllegalArgumentException {

        Validate.notNull(parserAgent, "Cannot handle null parserAgent.");

        if (!parseAgents.contains(parserAgent)) {
            parseAgents.add(parserAgent);
        }
    }
}
