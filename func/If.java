package ec.app.func;
import ec.*;
import ec.app.*;
import ec.gp.*;
import ec.util.*;


public class If extends GPNode
    {
    public String toString() { return "If"; }

    public int expectedChildren() { return 3; }

    public void eval(final EvolutionState state,
        final int thread,
        final GPData input,
        final ADFStack stack,
        final GPIndividual individual,
        final Problem problem)
        {
        ProjectData rd = ((ProjectData)(input));

        children[0].eval(state,thread,input,stack,individual,problem);
        if(rd.b){   
            children[1].eval(state,thread,input,stack,individual,problem);
        }
        else{       
            children[2].eval(state,thread,input,stack,individual,problem);
        }


        }
    }



