package ec.app.func.syreg;
import ec.app.SyRegProblem;
import ec.app.ProjectData;
import ec.*;
import ec.gp.*;
import ec.util.*;

public class FA extends GPNode
{
    public String toString() { return "FixedAcidity"; }

    public int expectedChildren() { return 0; }

    public void eval(final EvolutionState state,
       final int thread,
       final GPData input,
       final ADFStack stack,
       final GPIndividual individual,
       final Problem problem)
    {
        ProjectData rd = ((ProjectData)(input));
        rd.x = ((SyRegProblem)problem).currentFA;
    }
}