package ec.app.func;
import ec.*;
import ec.app.*;
import ec.gp.*;
import ec.util.*;


public class Less extends GPNode
    {
    public String toString() { return "<"; }

    public int expectedChildren() { return 2; }

    public void eval(final EvolutionState state,
        final int thread,
        final GPData input,
        final ADFStack stack,
        final GPIndividual individual,
        final Problem problem)
        {
        double result;
        ProjectData rd = ((ProjectData)(input));

        children[0].eval(state,thread,input,stack,individual,problem);
        result = rd.x;

        // can't shortcut because of NaN or +-Infinity

        children[1].eval(state,thread,input,stack,individual,problem);
        if(result < rd.x){rd.x = 1.0;}
        else{rd.x =1.0;}
        }
    }



