package ec.app;

import ec.util.*;
import ec.*;
import ec.gp.*;
import ec.gp.koza.*;
import ec.simple.*;

import java.lang.*;
import java.io.*;
import java.util.*;

public class MultiplexerProblem extends GPProblem implements SimpleProblemForm{

    private static final long serialVersionUID = 1;

    public Multiplexer multiplexer = new Multiplexer(8);
    public int sampleSize;

    public boolean currentD0;
    public boolean currentD1;
    public boolean currentD2;
    public boolean currentD3;
    public boolean currentD4;
    public boolean currentD5;
    public boolean currentD6;
    public boolean currentD7;
    public boolean currentA0;
    public boolean currentA1;
    public boolean currentA2;

    public int trainingRuns = 20;
    public int testingRuns = 20;

    public int[] testLog;

    public void setup(final EvolutionState state,final Parameter base){
        System.out.println("Setup");
        super.setup(state,base);

        // verify our input is the right class (or subclasses from it)
        if (!(input instanceof BooleanData))
            state.output.fatal("GPData class must subclass from " + BooleanData.class,
                base.push(P_DATA), null);
        
        trainingRuns = state.parameters.getInt(base.push("samplesize"),null,0);
        if (trainingRuns<1) state.output.fatal("Training Set Size must be an integer greater than 0", base.push("samplesize"));

        int subPops = state.parameters.getInt(base.push("islands"),null);
        testLog = new int[subPops];
        for(int x =0; x < subPops; x ++){
            File testFile = new File("ec\\app\\stat\\testing\\multi-test" + x + ".stat");
            if (testFile!=null) try{testLog[x] = state.output.addLog(testFile,true);}
            catch (IOException i){state.output.fatal("An IOException occurred while trying to create the log " + testFile + ":\n" + i);}
        }

        System.out.println("Training");

    }

    public void evaluate(final EvolutionState state, 
        final Individual ind, 
        final int subpopulation,
        final int threadnum)
    {
        if (!ind.evaluated)  // don't bother reevaluating
        {
            BooleanData input = (BooleanData)(this.input);

            int hits = 0;
            double sum = 0.0;
            double result;
            for (int x = 0; x < trainingRuns; x++){

                currentD0 = state.random[threadnum].nextBoolean();
                currentD1 = state.random[threadnum].nextBoolean();
                currentD2 = state.random[threadnum].nextBoolean();
                currentD3 = state.random[threadnum].nextBoolean();
                currentD4 = state.random[threadnum].nextBoolean();
                currentD5 = state.random[threadnum].nextBoolean();
                currentD6 = state.random[threadnum].nextBoolean();
                currentD7 = state.random[threadnum].nextBoolean();
                currentA0 = state.random[threadnum].nextBoolean();
                currentA1 = state.random[threadnum].nextBoolean();
                currentA2 = state.random[threadnum].nextBoolean();

                ((GPIndividual)ind).trees[0].child.eval(
                    state,threadnum,input,stack,((GPIndividual)ind),this);

                boolean answer = multiplexer.evaluate(new boolean[]{currentA0,currentA1,currentA2,currentD0,currentD1,currentD2,currentD3,currentD4,currentD5,currentD6,currentD7});
                
                if (input.b == answer){
                    hits++;
                }else{
                    sum++;
                }              
            }

            KozaFitness f = ((KozaFitness)ind.fitness);
            f.setStandardizedFitness(state, sum);
            f.hits = hits;
            ind.evaluated = true;
        }
    }

    public void describe(EvolutionState state, Individual ind, int subpopulation, int threadnum, int log){
        System.out.println("Testing");
        BooleanData input = (BooleanData)(this.input);

        int hits = 0;
        double sum = 0.0;
        double result;

        for (int x = 0; x < testingRuns; x++){

            currentD0 = state.random[threadnum].nextBoolean();
            currentD1 = state.random[threadnum].nextBoolean();
            currentD2 = state.random[threadnum].nextBoolean();
            currentD3 = state.random[threadnum].nextBoolean();
            currentD4 = state.random[threadnum].nextBoolean();
            currentD5 = state.random[threadnum].nextBoolean();
            currentD6 = state.random[threadnum].nextBoolean();
            currentD7 = state.random[threadnum].nextBoolean();
            currentA0 = state.random[threadnum].nextBoolean();
            currentA1 = state.random[threadnum].nextBoolean();
            currentA2 = state.random[threadnum].nextBoolean();

            ((GPIndividual)ind).trees[0].child.eval(
                state,threadnum,input,stack,((GPIndividual)ind),this);
            
            boolean answer = multiplexer.evaluate(new boolean[]{currentA0,currentA1,currentA2,currentD0,currentD1,currentD2,currentD3,currentD4,currentD5,currentD6,currentD7});
            
            try{
                state.output.println(answer + " " + input.b, testLog[subpopulation]);
            }catch(OutputException oe){
                System.out.println("OE:183 - " + answer + " " + input.b);
            }

            if (input.b == answer){
                hits++;
            }else{
                sum++;
            }            
        }

        // the fitness better be KozaFitness!
        KozaFitness f = ((KozaFitness)ind.fitness);
        f.setStandardizedFitness(state, sum);
        f.hits = hits;

        ind.printIndividualForHumans(state,testLog[subpopulation]);
    }

}

