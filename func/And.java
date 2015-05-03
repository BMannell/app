package ec.app.func;
import ec.*;
import ec.app.*;
import ec.gp.*;
import ec.util.*;


public class And extends GPNode
    {
    public String toString() { return "AND"; }

    public int expectedChildren() { return 2; }

    public void eval(final EvolutionState state,
        final int thread,
        final GPData input,
        final ADFStack stack,
        final GPIndividual individual,
        final Problem problem)
        {
        boolean boo;
        BooleanData rd = ((BooleanData)(input));

        children[0].eval(state,thread,input,stack,individual,problem);
        if(rd.b) //if true evaluate the other branch
          children[1].eval(state,thread,input,stack,individual,problem);
        }
    }



