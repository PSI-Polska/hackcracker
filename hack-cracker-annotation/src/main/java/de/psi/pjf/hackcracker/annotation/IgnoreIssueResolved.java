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
    /**
     * Version of component that your project is using. 
     * Maybe in the future this will be somehow automatically resolved.
     * @return version of dependency used by project.
     */
    String versionUsed();
    
    /**
     * Version of component that the fix is for. 
     * The idea is that this annotation is placed only after the issue actually 
     * has been resolved and one cannot upgrade to the newer version for some 
     * reason so the version is known.
     * Maybe in the future this will be somehow automatically resolved.
     * @return version of dependency that the fix is for.
     */
    String versionResolved();
    
    /**
     * Short description of why the component cannot be upgraded to the version 
     * where the bug is not present and thus the hac one made being not necessary.
     * The idea is that this annotation is placed only after the issue actually 
     * has been resolved and one cannot upgrade to the newer version for some 
     * reason. A meaningful explanation of why it is the case should be provided
     * and currently the algorithm of determining if the explanation is meaningful
     * is simply - is it at least 128 characters long. This is mainly so that 
     * ignoring cannot be done out of habit or at least that is authors hope.
     * @return a meaningful explanation.
     */
    String reasoningWhyHackCannotBeRemoved();
}
