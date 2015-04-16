package ec.app.func;
import ec.*;
import ec.app.*;
import ec.gp.*;
import ec.util.*;



public class DevSeven extends GPNode
{
    public String toString() { return "Dev7"; }

    public int expectedChildren() { return 0; }

    public void eval(final EvolutionState state,
        final int thread,
        final GPData input,
        final ADFStack stack,
        final GPIndividual individual,
        final Problem problem)
    {
        ProjectData rd = ((ProjectData)(input));
        rd.x = ((ProjectProblem)problem).currentDev7;
    }
}
