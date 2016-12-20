package sample.code.that.should.compile;

import de.psi.pjf.hackcracker.annotation.FixForIssue;

/**
 *
 * @author akedziora
 */
public class SampleMain
{

    /**
     * @param args the command line arguments
     */
    @FixForIssue(url = "http://jira-bld-ppl.psi.de:8080/", issue = "CMDTF-36")
    public static void main(String[] args)
    {
        System.out.println("Hello world");
    }
    
}
