package ec.app;
import ec.gp.*;

public class ProjectData extends GPData
    {
    public double x;
    public boolean b;

    public void copyTo(final GPData gpd) 
        { 
        	((ProjectData)gpd).x = x;
        	((ProjectData)gpd).b = b;
        }
    }
