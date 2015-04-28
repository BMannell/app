package ec.app;
import ec.gp.*;

public class BooleanData extends GPData
    {
    public boolean b;

    public void copyTo(final GPData gpd) 
        { 
        	((BooleanData)gpd).b = b;
        }
    }
