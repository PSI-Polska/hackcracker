package de.psi.pjf.hackcracker.annotation;

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

/**
 *
 * @author akedziora
 */
public class JiraConnectionsProvider
{

    private static final Map<String, JiraRestClient> CONNECTION_CACHE = new HashMap<>();
    private static final JiraRestClientFactory FACTORY = new AsynchronousJiraRestClientFactory();
    private static final IssueTrackerConfiguration CONFIGURATION = IssueTrackerConfiguration.getConfiguration();

    static JiraRestClient getConnection(String jiraUrl) throws URISyntaxException
    {
        JiraRestClient toReturn = CONNECTION_CACHE.get(jiraUrl);
        if (toReturn == null)
        {
            toReturn = connectTo(jiraUrl);
            CONNECTION_CACHE.put(jiraUrl, toReturn);
        }
        return toReturn;
    }

    private static JiraRestClient connectTo(String aJiraUrl) throws URISyntaxException
    {
        JiraInstance instance = CONFIGURATION.getForName(aJiraUrl);
        return instance == null ? 
               connectToAnonymously(aJiraUrl) : 
               connectToWithBasicAuthentication(instance.getUrl(),instance.getUser(),instance.getPassword());
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
