package de.psi.pjf.hackcracker.annotation;

import static java.lang.annotation.ElementType.ANNOTATION_TYPE;
import static java.lang.annotation.ElementType.FIELD;
import static java.lang.annotation.ElementType.LOCAL_VARIABLE;
import static java.lang.annotation.ElementType.METHOD;
import static java.lang.annotation.ElementType.PACKAGE;
import static java.lang.annotation.ElementType.PARAMETER;
import static java.lang.annotation.ElementType.TYPE;
import static java.lang.annotation.ElementType.TYPE_PARAMETER;
import static java.lang.annotation.ElementType.TYPE_USE;
import java.lang.annotation.Retention;
import static java.lang.annotation.RetentionPolicy.SOURCE;
import java.lang.annotation.Target;

/**
 * This is a containing annotation for {@link FixForIssue}. As of Java 8 one
 * does not need to use it explicitely in most cases, but can simply repeat
 * the {@link FixForIssue} annotation and {@link FixForIssues} will be added
 * automatically under the hood with {@link #needsAllIssuesResolved()} true.
 * Annotation has to be used explicite if one needs the {@link #needsAllIssuesResolved()}
 * false.
 */
@Target(value = {TYPE, FIELD, LOCAL_VARIABLE, METHOD, PACKAGE, PARAMETER, TYPE_PARAMETER, TYPE_USE, ANNOTATION_TYPE})
@Retention(value = SOURCE)
public @interface FixForIssues {

    FixForIssue[] value();

    /**
     * Specifies if build should fail when all issues are resolved (true) or
     * when at least one is (false).
     * Defaults to true.
     * Funny fact: originally I wanted this to be enum - unfortunatelly this 
     * triggers a bug in jdk - I probably should submit it and use the 
     * @FixForIssue annotation here :).
     * @return how issues are connected.
     */
    boolean needsAllIssuesResolved() default true;
    
}
