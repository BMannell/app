package ec.app;

import ec.app.ProjectData;
import ec.util.*;
import ec.*;
import ec.gp.*;
import ec.gp.koza.*;
import ec.simple.*;

import java.lang.*;
import java.io.*;
import java.util.*;

public class SyRegProblem extends GPProblem implements SimpleProblemForm{

    private static final long serialVersionUID = 1;

    final double HIT_LEVEL = 0.01;
    final double PROBABLY_ZERO = 1.11E-15;
    final double BIG_NUMBER = 1.0e15;  // the same as lilgp uses


    public int sampleSize;

    public double currentFA;
    public double currentVA;
    public double currentCA;
    public double currentRS;
    public double currentCH;
    public double currentFSD;
    public double currentTSD;
    public double currentDE;
    public double currentPH;
    public double currentSU;
    public double currentAL;

    public int trainingSetSize;
    public int testingSetSize;

    public double trainingData[][];
    public double testingData[][];


    public int testLog;

    public void setup(final EvolutionState state,final Parameter base){
        System.out.println("Setup");
        super.setup(state,base);

        // verify our input is the right class (or subclasses from it)
        if (!(input instanceof ProjectData))
            state.output.fatal("GPData class must subclass from " + ProjectData.class,
                base.push(P_DATA), null);
        
        trainingSetSize = state.parameters.getInt(base.push("samplesize"),null,0);
        if (trainingSetSize<1) state.output.fatal("Training Set Size must be an integer greater than 0", base.push("samplesize"));

        //System.out.println("Data Size: " + dataSetSize + " TrainingSize: " + trainingSize + " Training: " + trainingSetSize + " TestingSetSize: "  + testingSetSize);

        //get input file to load data from
        InputStream inputfile = state.parameters.getResource(base.push("datafile"), null);

                //add data to tables
        initDataTables(inputfile, state);


        File testFile =  state.parameters.getFile(base.push("testingfile"), null);

        if (testFile!=null) try{testLog = state.output.addLog(testFile,true);}
        catch (IOException i){state.output.fatal("An IOException occurred while trying to create the log " + testFile + ":\n" + i);}
        System.out.println("Training");

    }

    private void initDataTables(InputStream inputfile, final EvolutionState state){

        ArrayList<double[]> inputs = new ArrayList<double[]>();
        
        try{
            Scanner scan = new Scanner(inputfile);
            while(scan.hasNextLine()){
                double row[] = new double[12];
                for(int y = 0; y < 12; y ++){
                    if (scan.hasNextDouble()) row[y] = scan.nextDouble();
                    else state.output.fatal("Not enough data points in file: expected 12");
                }
                inputs.add(row);
            }
        }
        catch (NumberFormatException e){state.output.fatal("Some tokens in the file were not numbers.");}

        trainingData = new double[trainingSetSize][12];
        testingData = new double[inputs.size()][12];

        //random number genreator to pick random records
        Random rando = new Random();

        //grab a random record from the inputs and add it to the training set
        for(int i = 0; i<trainingSetSize;i++)
            trainingData[i] = inputs.remove(rando.nextInt(inputs.size()-1));
        
        for(int i = 0; i < inputs.size();i++)
            testingData[i] = inputs.get(i);
        
    }

    public void evaluate(final EvolutionState state, 
        final Individual ind, 
        final int subpopulation,
        final int threadnum)
    {
        if (!ind.evaluated)  // don't bother reevaluating
        {
            ProjectData input = (ProjectData)(this.input);

            int hits = 0;
            double sum = 0.0;
            double result;
            for (double[] trainingRow: trainingData){

                currentFA = trainingRow[0];
                currentVA = trainingRow[1];
                currentCA = trainingRow[2];
                currentRS = trainingRow[3];
                currentCH = trainingRow[4];
                currentFSD = trainingRow[5];
                currentTSD = trainingRow[6];
                currentDE = trainingRow[7];
                currentPH = trainingRow[8];
                currentSU = trainingRow[9];
                currentAL = trainingRow[10];

                ((GPIndividual)ind).trees[0].child.eval(
                    state,threadnum,input,stack,((GPIndividual)ind),this);

                result = Math.abs(trainingRow[11] - input.x);

                if (! (result < BIG_NUMBER ) )
                    result = BIG_NUMBER;

                else if (result<PROBABLY_ZERO)
                    result = 0.0;

                if (result <= HIT_LEVEL) hits++;

                sum += result;              
            }

            KozaFitness f = ((KozaFitness)ind.fitness);
            f.setStandardizedFitness(state, sum);
            f.hits = hits;
            ind.evaluated = true;
        }
    }

    public void describe(EvolutionState state, Individual ind, int subpopulation, int threadnum, int log){
        System.out.println("Testing");
        ProjectData input = (ProjectData)(this.input);

        int hits = 0;
        double sum = 0.0;
        double result;

        for (double[] testRow: testingData){

            currentFA = testRow[0];
            currentVA = testRow[1];
            currentCA = testRow[2];
            currentRS = testRow[3];
            currentCH = testRow[4];
            currentFSD = testRow[5];
            currentTSD = testRow[6];
            currentDE = testRow[7];
            currentPH = testRow[8];
            currentSU = testRow[9];
            currentAL = testRow[10];
            ((GPIndividual)ind).trees[0].child.eval(
                state,threadnum,input,stack,((GPIndividual)ind),this);

            try{
                state.output.println(testRow[11] + " " + input.x, testLog);
            }catch(OutputException oe){
                System.out.println("OE:183 - " + testRow[11] + " " + input.x);
            }

            result = Math.abs(testRow[11] - input.x);

            if (! (result < BIG_NUMBER ) ) 
                result = BIG_NUMBER;

            else if (result<PROBABLY_ZERO)
                result = 0.0;

            if (result <= HIT_LEVEL) hits++;

            sum += result;              
        }

        // the fitness better be KozaFitness!
        KozaFitness f = ((KozaFitness)ind.fitness);
        f.setStandardizedFitness(state, sum);
        f.hits = hits;

        ind.printIndividualForHumans(state,testLog);
    }

}

