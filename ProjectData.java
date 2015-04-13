package ec.app;
import ec.gp.*;

public class ProjectData extends GPData
    {
    public double x;

    public void copyTo(final GPData gpd) 
        { 
        	((ProjectData)gpd).x = x;
        }
    }
