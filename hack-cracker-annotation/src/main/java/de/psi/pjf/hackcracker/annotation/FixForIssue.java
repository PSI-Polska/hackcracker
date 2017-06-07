package de.psi.pjf.hackcracker.annotation;

import static java.lang.annotation.ElementType.*;
import java.lang.annotation.Retention;
import static java.lang.annotation.RetentionPolicy.SOURCE;
import java.lang.annotation.Target;
import static de.psi.pjf.hackcracker.annotation.FixForIssue.IssueTrackerType.JIRA;
import java.lang.annotation.Repeatable;

/**
 * This annotation should be placed above an element that has been written only to overcome some issue and
 * which is expected to be removed after the original issue is resolved. During compilation, annotation 
 * processor will call the issuetracker and ask about the issue - if the issuetracker will return that the 
 * issue is already resolved, the processor will mark an compilation error with appropriate description.
 * If there will be any kind of problem when calling the issuetracker, some exceptions may be printed to the 
 * compilation output, but the build will pass.
 * Various issuetracker instances can be configured in a file located in {@code ~/hackcracker/issue_trackers.xml} 
 * where home is resolved with {@code System.getProperty("user.home")}. sample configuration file looks like:
 * {@code 
 * <?xml version="1.0" encoding="UTF-8"?>
 *  <configuration>
 *      <jira>
 *          <url>http://your.jira.host:<port>/</url>
 *          <user>put_your_user_here</user>
 *          <password>put_yeur_user_here</password>
 *      </jira>
 *  </configuration>
 * }. Location of the configuration file can be overriden with system property: {@code hackcracker.configuration}
 * for example in a maven build with {@code properties-maven-plugin}. Currently no stacking of configurations 
 * is supported.
 * Currently only simple authentication is supported.
 * If there won't be a configuration for a particular instance, then anonymous connection will be tried, so for
 * publicly available issuetrackers, no configuration is needed.
 * This is a compiletime only annotation, so it is expected to be referrenced only on compiletime, for example 
 * in maven you get such effect by using the scope provided.
 * {@code 
 *  <dependency>
 *      <groupId>de.psi.pjf</groupId>
 *      <artifactId>hack-cracker-annotation</artifactId>
 *      <version>${project.version}</version>
 *      <scope>provided</scope>
 *  </dependency>
 * }
 * @author akedziora
 */
@Repeatable(FixForIssues.class)
@Target({TYPE,FIELD,LOCAL_VARIABLE,METHOD,PACKAGE,PARAMETER,TYPE_PARAMETER,TYPE_USE,ANNOTATION_TYPE})
@Retention(SOURCE)
public @interface FixForIssue
{
    /**
     * This parameter specifies url of the jira instance to call.
     * @return 
     */
    String url();
    
    /**
     * This parameter specifies issue to check.
     * @return 
     */
    String issue();
    
    /**
     * This parameter specifies what issuetracker type the issue is in. 
     * Currently only atlassian jira is supported.
     * @return 
     */
    IssueTrackerType trackerType() default JIRA;
    
    public static enum IssueTrackerType{
        JIRA;
    }
}
