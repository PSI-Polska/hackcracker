package de.psi.pjf.hackcracker.jira;

import ch.qos.logback.classic.Level;
import ch.qos.logback.classic.Logger;
import com.atlassian.jira.rest.client.api.JiraRestClient;
import com.atlassian.jira.rest.client.api.JiraRestClientFactory;
import com.atlassian.jira.rest.client.api.domain.Issue;
import com.atlassian.jira.rest.client.api.domain.Resolution;
import com.atlassian.jira.rest.client.auth.AnonymousAuthenticationHandler;
import com.atlassian.jira.rest.client.internal.async.AsynchronousJiraRestClientFactory;
import com.google.common.base.Preconditions;
import de.psi.pjf.hackcracker.annotation.FixForIssue;
import static de.psi.pjf.hackcracker.annotation.FixForIssue.IssueTrackerType.JIRA;
import de.psi.pjf.hackcracker.annotation.configuration.IssueTrackerConfiguration;
import de.psi.pjf.hackcracker.annotation.configuration.IssueTrackerConfiguration.JiraInstance;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import org.slf4j.LoggerFactory;

/**
 *
 * @author akedziora
 */
public class JiraIssueChecker
{

    private static final Map<String, JiraRestClient> CONNECTION_CACHE = new HashMap<>();
    private static final JiraRestClientFactory FACTORY = new AsynchronousJiraRestClientFactory();
    private static final IssueTrackerConfiguration CONFIGURATION = IssueTrackerConfiguration.CONFIGURATION;
    
    public static boolean checkIssueResolved(FixForIssue issueInformation)
    {
        Preconditions.checkArgument(issueInformation.trackerType().equals(JIRA));
        return internalCheckIssueResolvedWithLoggersOff( 
                issueInformation.url(), 
                issueInformation.issue(), 
                "com.atlassian.jira.rest.client.internal.async.AsynchronousHttpClientFactory$MavenUtils",
                "com.atlassian.httpclient.apache.httpcomponents.cache.FlushableHttpCacheStorageImpl");
    }
    
    /**
     * A dirty hack so that one does not have to use logback.xml to configure logs.
     * I do believe that forcing logging configuration in annotation processor 
     * would be quite stupid, but I just as well might be wrong - after all I'm 
     * not an slf4j expert and maybe it could be done quite efficiently.
     * @param aJiraUrl that will be passed to connectTo
     * @return client that connectTo will return
     * @throws URISyntaxException 
     */
    private static boolean internalCheckIssueResolvedWithLoggersOff(String aJiraUrl, String aIssue, String ... loggers){
        if(loggers.length > 0){
            Logger logger = (Logger) LoggerFactory.getLogger(loggers[0]);        
            Level oldLevel = logger.getLevel();
            logger.setLevel(Level.OFF);
            try {
                return internalCheckIssueResolvedWithLoggersOff(aJiraUrl, aIssue, Arrays.copyOfRange(loggers, 1, loggers.length));
            } finally {
                logger.setLevel(oldLevel);
            }
        }else{
            return internalCheckIssueResolved(aJiraUrl, aIssue);
        }
    }

    private static boolean internalCheckIssueResolved(String aJiraUrl, String aIssue){
        try {
            Optional<Issue> issue = getConnection(aJiraUrl).map((JiraRestClient c) -> c.getIssueClient().getIssue(aIssue).claim());
            return issue.isPresent() && resolutionInResolved(issue.get().getResolution());
        } catch (URISyntaxException ex) {
            throw new RuntimeException(ex.getMessage(),ex);
        }
    }

    private static  boolean resolutionInResolved(Resolution r)
    {
        if (r == null)
        {
            return false;
        }
        switch (r.getName())
        {
            case "Done":
            case "Fixed": return true;
            default: return false;
        }
    }

    private static Optional<JiraRestClient> getConnection(String jiraUrl) throws URISyntaxException
    {
        JiraRestClient toReturn = CONNECTION_CACHE.get(jiraUrl);
        if (toReturn == null)
        {
            toReturn = connectTo(jiraUrl);
            CONNECTION_CACHE.put(jiraUrl, toReturn);
        }
        return Optional.ofNullable(toReturn);
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
