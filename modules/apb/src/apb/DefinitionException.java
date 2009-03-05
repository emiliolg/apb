package apb;
//
// User: emilio
// Date: Mar 4, 2009
// Time: 11:34:45 AM

//
public class DefinitionException extends Throwable {
    private static final long serialVersionUID = 727227781131642345L;

    public DefinitionException(String elementName, Throwable t) {
        super("Cannot load definition for: " + elementName, t);
    }
}
