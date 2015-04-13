package ec.app.func;
import ec.*;
import ec.app.*;
import ec.gp.*;
import ec.util.*;



public class Edge extends GPNode
{
    public String toString() { return "Edge"; }

    public int expectedChildren() { return 0; }

    public void eval(final EvolutionState state,
        final int thread,
        final GPData input,
        final ADFStack stack,
        final GPIndividual individual,
        final Problem problem)
    {
        PicData rd = ((PicData)(input));
        rd.x = ((PicProblem)problem).currentEdge;
    }
}
