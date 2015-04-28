package ec.app.func;
import ec.*;
import ec.app.*;
import ec.gp.*;
import ec.util.*;

public class Nor extends GPNode
{
  public String toString() { return "NOR"; }

  public int expectedChildren() { return 2; }

  public void eval(final EvolutionState state,
    final int thread,
    final GPData input,
    final ADFStack stack,
    final GPIndividual individual,
    final Problem problem)
  {
    boolean boo;
    ProjectData rd = ((ProjectData)(input));

    children[0].eval(state,thread,input,stack,individual,problem);

    if(rd.b)
      return false;
    else{
      children[1].eval(state,thread,input,stack,individual,problem);
      return rd.b;
    }
  }
}



