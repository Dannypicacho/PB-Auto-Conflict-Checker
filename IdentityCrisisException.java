import java.util.ArrayList;

public class IdentityCrisisException extends Exception{
    public ArrayList<String> perpetrators = new ArrayList<String>();

    IdentityCrisisException(ArrayList<String> errorList){
        super();
        perpetrators = errorList;
    }
}
