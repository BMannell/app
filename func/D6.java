package ec.app.func;
import ec.app.MultiplexerProblem;
import ec.app.BooleanData;
import ec.*;
import ec.gp.*;
import ec.util.*;

public class D6 extends GPNode
{
    public String toString() { return "D6"; }

    public int expectedChildren() { return 0; }

    public void eval(final EvolutionState state,
       final int thread,
       final GPData input,
       final ADFStack stack,
       final GPIndividual individual,
       final Problem problem)
    {
        ProjectData rd = ((ProjectData)(input));
        rd.b= ((SyRegProblem)problem).currentD6;
    }
}