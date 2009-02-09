import apb.metadata.Project;

public class ApbAll
    extends Project
{
    //~ Instance initializers ................................................................................

    {
        components(new Apb(), new ApbAnt());
    }
}
