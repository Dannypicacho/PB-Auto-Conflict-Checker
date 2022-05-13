import java.util.ArrayList;
import java.util.HashMap;

public class TooManyMesException extends Exception{
    public HashMap<String, ArrayList<String>> failures = new HashMap<String, ArrayList<String>>();

    TooManyMesException(HashMap<String, ArrayList<String>> errorList){
        super();
        failures = errorList;
    }
}
