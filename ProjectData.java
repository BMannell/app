package ec.app;
import ec.gp.*;

public class PicData extends GPData
    {
    public double x;

    public void copyTo(final GPData gpd) 
        { 
        	((PicData)gpd).x = x;
        }
    }
