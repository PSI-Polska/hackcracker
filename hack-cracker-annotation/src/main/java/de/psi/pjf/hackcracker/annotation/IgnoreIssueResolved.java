package de.psi.pjf.hackcracker.annotation;

import static java.lang.annotation.ElementType.*;
import java.lang.annotation.Retention;
import static java.lang.annotation.RetentionPolicy.SOURCE;
import java.lang.annotation.Target;

/**
 * This annotation can be placed on the element annotated with {@link FixForIssue} 
 * or {@link FixForIssues} to make it not fail the build when an issue has been 
 * resolved.
 * This should be used sparsely as the whole point of the @FixForIssue is to make
 * the amount of hacks in the code lesser over time. There is one case when 
 * removing the hack might not be easy and that is if a fix is introduced only 
 * in new version and for some reasons we can't upgrade now. In such case this 
 * annotation can be used with both versions specified and a reasonable explanation 
 * given (arbitrarly at least 128 chars long - so this annotation will be hard 
 * to use - by design).
 * @author akedziora
 */
@Target({TYPE,FIELD,LOCAL_VARIABLE,METHOD,PACKAGE,PARAMETER,TYPE_PARAMETER,TYPE_USE,ANNOTATION_TYPE})
@Retention(SOURCE)
public @interface IgnoreIssueResolved
{
    String versionUsed();
    
    String versionResolved();
    
    String reasoningWhyHackCannotBeRemoved();
}
