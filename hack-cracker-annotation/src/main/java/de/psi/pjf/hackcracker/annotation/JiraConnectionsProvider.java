package de.psi.pjf.hackcracker.annotation;

import ch.qos.logback.classic.Level;
import ch.qos.logback.classic.Logger;
import com.atlassian.jira.rest.client.api.JiraRestClient;
import com.atlassian.jira.rest.client.api.JiraRestClientFactory;
import com.atlassian.jira.rest.client.auth.AnonymousAuthenticationHandler;
import com.atlassian.jira.rest.client.internal.async.AsynchronousJiraRestClientFactory;
import de.psi.pjf.hackcracker.annotation.configuration.IssueTrackerConfiguration;
import de.psi.pjf.hackcracker.annotation.configuration.IssueTrackerConfiguration.JiraInstance;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import org.slf4j.LoggerFactory;

/**
 *
 * @author akedziora
 */
public class JiraConnectionsProvider
{

    private static final Map<String, JiraRestClient> CONNECTION_CACHE = new HashMap<>();
    private static final JiraRestClientFactory FACTORY = new AsynchronousJiraRestClientFactory();
    private static final IssueTrackerConfiguration CONFIGURATION = IssueTrackerConfiguration.getConfiguration();

    static Optional<JiraRestClient> getConnection(String jiraUrl) throws URISyntaxException
    {
        JiraRestClient toReturn = CONNECTION_CACHE.get(jiraUrl);
        if (toReturn == null)
        {
            toReturn = connectToWithLoggerOff(jiraUrl);
            CONNECTION_CACHE.put(jiraUrl, toReturn);
        }
        return Optional.ofNullable(toReturn);
    }

    /**
     * A dirty hack so that one does not have to use logback.xml to configure logs.
     * I do believe that forcing logging configuration in annotation processor 
     * would be quite stupid, but I just as well might be wrong - after all I'm 
     * not an slf4j expert and maybe it could be done quite efficiently.
     * @param aJiraUrl
     * @return
     * @throws URISyntaxException 
     */
    private static JiraRestClient connectToWithLoggerOff(String aJiraUrl) throws URISyntaxException
    {
        Logger logger = (Logger) LoggerFactory.getLogger("com.atlassian.jira.rest.client.internal.async.AsynchronousHttpClientFactory$MavenUtils");
        Level oldLevel = logger.getLevel();
        logger.setLevel(Level.OFF);
        try {
            return connectTo(aJiraUrl);
        } finally {
            logger.setLevel(oldLevel);
        }
    }

    private static JiraRestClient connectTo(String aJiraUrl) throws URISyntaxException {
        JiraInstance instance = CONFIGURATION.getForName(aJiraUrl);
        return instance == null
                ? connectToAnonymously(aJiraUrl)
                : connectToWithBasicAuthentication(instance.getUrl(), instance.getUser(), instance.getPassword());
    }

    private static JiraRestClient connectToWithBasicAuthentication(String aJiraUrl, String aUser,
                                                                   String aPassword) throws URISyntaxException
    {
        URI uri = new URI(aJiraUrl);
        JiraRestClient client = FACTORY.createWithBasicHttpAuthentication(uri, aUser, aPassword);
        return client;
    }

    private static JiraRestClient connectToAnonymously(String aJiraUrl) throws URISyntaxException
    {
        URI uri = new URI(aJiraUrl);
        JiraRestClient client = FACTORY.create(uri, new AnonymousAuthenticationHandler());
        return client;
    }

}
