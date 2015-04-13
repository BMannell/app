package ec.app;

import ec.app.PicData;
import ec.util.*;
import ec.*;
import ec.gp.*;
import ec.gp.koza.*;
import ec.simple.*;

import java.lang.*;
import java.io.File;
import java.awt.image.BufferedImage;
import java.awt.image.DataBufferByte;
import javax.imageio.ImageIO;
import java.io.IOException;
import java.util.Random;
import java.util.ArrayList;
import java.awt.Color;

public class PicProblem extends GPProblem implements SimpleProblemForm
{
    private static final long serialVersionUID = 1;

    //image data
    public ArrayList<int[][]> images = new ArrayList<int[][]>();

    public int numOfImages = 1;

    public int goodImage = -1;

    public int sampleSize = -1;

    public int testLog;

    public int[] outputFiles = new int[4];
    //spatial stats for corrisponding pixels in image
    public ArrayList<double[][]> avg3 = new ArrayList<double[][]>(), avg5= new ArrayList<double[][]>(), avg7= new ArrayList<double[][]>();     //average
    public ArrayList<double[][]> std3= new ArrayList<double[][]>(), std5= new ArrayList<double[][]>(), std7= new ArrayList<double[][]>();     //standard deviation
    public ArrayList<double[][]> dev3= new ArrayList<double[][]>(),dev5= new ArrayList<double[][]>(),dev7= new ArrayList<double[][]>();  //deviation from mean
    public ArrayList<ArrayList<double[][]>> devList = new ArrayList<ArrayList<double[][]>>();
    public ArrayList<double[][]> edge= new ArrayList<double[][]>(), sharpen= new ArrayList<double[][]>();

    //terminals for computation
    public int currentPixel;
    public double currentAvg3, currentAvg5, currentAvg7;       //average
    public double currentStd3, currentStd5, currentStd7;       //standard deviation
    public double currentDev3, currentDev5, currentDev7;    //deviation
    public double currentEdge, currentSharpen;              //edge/sharpen

    //locations of positive testing pixels
    public ArrayList<ArrayList<int[]>> testingCoords = new ArrayList<ArrayList<int[]>>();
    public boolean[][][] sampleReference = new boolean[4][256][256];


    public void setup(final EvolutionState state, final Parameter base){
        super.setup(state,base);

        // verify our input is the right class (or subclasses from it)
        if (!(input instanceof PicData))
            state.output.fatal("GPData class must subclass from " + PicData.class,
                base.push(P_DATA), null);

                //section of image that testing data is to be taken from
        goodImage = state.parameters.getInt(base.push("goodimage"),null) - 1;
        System.out.println("Good Image: " + goodImage);
                //number of pixels to be used in each testing array
        sampleSize = state.parameters.getInt(base.push("samplesize"), null);


        numOfImages = state.parameters.getInt(base.push("numberofimages"),null);


        for(int x = 0; x < numOfImages; x++){
                    //image to use
            String name = "imagefile" + (x+1);
            File file = state.parameters.getFile(base.push(name),null);
            addImageToList(file, x);
            File outFile = state.parameters.getFile(base.push("confusionimagefile" + (x+1)),null);
            if(outFile!=null)try{
                outputFiles[x] = state.output.addLog(outFile,true);
            }
            catch (IOException i){
                state.output.fatal("An IOException occurred while trying to create the image " + 
                    x );
            }


        }

        //compute spatial stats
        computeAverage();
        computeStdDev();
        computeEdge();
        computeSharpen();

                //choose random pixel for testing
        Random rand = new Random();
        int samp;
        
        for(int i = 0; i < 4; i++){
            ArrayList<int[]> temp = new ArrayList<int[]>();
            if(i==goodImage)  samp = 999;
            else  samp = 333;
            for(int j = 0; j < samp; j++){
                int y = rand.nextInt(256);
                int x = rand.nextInt(256);
                temp.add(new int[]{y,x});
                sampleReference[i][y][x] = true;
            }
            testingCoords.add(temp);
        }

        File infoFile = state.parameters.getFile(
            base.push("testingfile"),null);
        if (infoFile!=null) try
        {
            testLog = state.output.addLog(infoFile,true);
        }
        catch (IOException i)
        {
            state.output.fatal("An IOException occurred while trying to create the log " + 
                infoFile + ":\n" + i);
        }

        System.out.println("Begining Training");

    }

    public void addImageToList(File file, int imageNumber){
        int imageHeight = 0;
        int imageWidth = 0;
        byte[] imageBytes = null;
        try {
            BufferedImage bufferedImage = ImageIO.read(file);
            //get image dimensions
            imageHeight = bufferedImage.getHeight();
            imageWidth = bufferedImage.getWidth();
            imageBytes = ((DataBufferByte) bufferedImage.getData().getDataBuffer()).getData();

            System.out.println("Image " + imageNumber + " upload success.");
        } catch (IOException ioe) {
            System.out.println(ioe.getMessage());
            System.exit(0);
        }

        int[][] tempImage = new int[imageHeight][imageWidth];
        initArrays(imageHeight, imageWidth);

        //convert to 2D array
        int c = 0; //counter
        for(int y = 0; y < imageHeight; y++){
            for(int x = 0; x < imageWidth; x++){
                tempImage[y][x] = imageBytes[c] & 0xFF;
                //System.out.print(tempImage[y][x] + " ");
                c++;
            }
            //System.out.println();
        }
        images.add(tempImage);

    }

    public void initArrays(int imgHeight, int imgWidth){
        //average arrays
        avg3.add(new double[imgHeight][imgWidth]);
        avg5.add(new double[imgHeight][imgWidth]);
        avg7.add(new double[imgHeight][imgWidth]);

        //std dev arrays 
        std3.add(new double[imgHeight][imgWidth]);
        std5.add(new double[imgHeight][imgWidth]);
        std7.add(new double[imgHeight][imgWidth]);

        //deviation arrays 
        dev3.add(new double[imgHeight][imgWidth]);
        dev5.add(new double[imgHeight][imgWidth]);
        dev7.add(new double[imgHeight][imgWidth]);

        //convolution    
        edge.add(new double[imgHeight][imgWidth]);

        //sharpen
        sharpen.add(new double[imgHeight][imgWidth]);
    }

    public void computeAverage(){
        int sum3 = 0, sum2 = 0, sum1 = 0;
        for(int i = 0; i < numOfImages; i++){
            for(int y = 0; y < images.get(i).length; y++){
                for(int x = 0; x < images.get(i)[0].length; x++){

                //copmute average for pixel[x,y]
                    avg3.get(i)[y][x] = computeAverage(i,y,x,1);
                    avg5.get(i)[y][x] = computeAverage(i,y,x,2);
                    avg7.get(i)[y][x] = computeAverage(i,y,x,3);

                //compute deviation for pixels[x,y]
                    dev3.get(i)[y][x] = Math.pow(images.get(i)[y][x] - avg3.get(i)[y][x],2);
                    dev5.get(i)[y][x] = Math.pow(images.get(i)[y][x] - avg5.get(i)[y][x],2);
                    dev7.get(i)[y][x] = Math.pow(images.get(i)[y][x] - avg7.get(i)[y][x],2);
                }
            }
        }
        devList.add(dev3);
        devList.add(dev5);
        devList.add(dev7);
        System.out.println("Averages and Deviations computed.");
    }

    public double computeAverage(int img, int y, int x, int ring){
        int sum = 0;
        int count = 0;
        int boxSize = (2*ring)+1;
        int startY = y - ring;
        int startX = x - ring;
        int imgHeight = images.get(img).length;
        int imgWidth = images.get(img)[0].length;
        for(int i = startY; i < startY + boxSize; i++){
            for(int j = startX; j < startX + boxSize;j++){
                if(i >= 0 && i < imgHeight && j >= 0 && j < imgWidth){
                    sum += images.get(img)[i][j];
                    count++;
                }
            }
        }
        return (double)(sum/count);
    }

    public void computeStdDev(){
        int sum3 = 0, sum2 = 0, sum1 = 0;
        for(int i = 0; i < numOfImages; i++){
            for(int y = 0; y < images.get(i).length; y++){
                for(int x = 0; x < images.get(i)[0].length; x++){
                    std3.get(i)[y][x] = computeStdDev(i,y,x,1);
                    std5.get(i)[y][x] = computeStdDev(i,y,x,2);
                    std7.get(i)[y][x] = computeStdDev(i,y,x,3);
                }
            }
        }
        System.out.println("StdDev computed.");

    }

    public double computeStdDev(int img, int y, int x, int ring){
        int sum = 0;
        int count = 0;
        int boxSize = 2*ring+1;
        int startY = y - ring;
        int startX = x - ring;
        int imgHeight = images.get(img).length;
        int imgWidth = images.get(img)[0].length;
        for(int i = startY; i < startY + boxSize; i++){
            for(int j = startX; j < startX + boxSize;j++){
                if(i >= 0 && i < imgHeight && j >= 0 && j < imgWidth){
                    sum += devList.get(ring-1).get(img)[i][j];
                    count++;
                }
            }
        }
        return Math.sqrt((double)(sum/count));
    }

    public void computeEdge(){
                int[][] mask = new int[][] {
            {-1,-1,-1},
            {-1,8,-1},
            {-1,-1,-1}};
        edge = computeMask(mask);
        System.out.println("Edge computed.");
    }

    public void computeSharpen(){
                int[][] mask = new int[][] {
            {0,-1,0},
            {-1,5,-1},
            {0,-1,0}};
        sharpen = computeMask(mask);
        System.out.println("Sharpen computed.");
    }

    public ArrayList<double[][]> computeMask(int[][] mask){
        ArrayList<double[][]> tempList = new ArrayList<double[][]>();

        for(int i = 0; i < numOfImages; i++){

            //get image dimensions
            int imgHeight = images.get(i).length;
            int imgWidth = images.get(i)[0].length;

            //make array to hold calculated values
            //same size as image
            double[][] tempArray = new double[imgHeight][imgWidth];

            //iterate through pixels
            for(int y = 0; y<imgHeight;y++){
                for(int x= 0; x < imgWidth; x++){
                    tempArray[y][x] = computeMask(i, y, x, mask);
                }
            }
            tempList.add(tempArray);
        }
        return tempList;
    }

    public double computeMask(int img, int y, int x, int[][] mask){
        double sum = 0;
        int count = 0;
        int startY = y - 1;
        int startX = x - 1;
        int imgHeight = images.get(img).length;
        int imgWidth = images.get(img)[0].length;
        for(int i = 0; i < 3; i++){
            for(int j = 0; j < 3;j++){
                int pY = i + startY;
                int pX = j + startX;
                if(pY >= 0 && pY < imgHeight && pX >= 0 && pX < imgWidth){
                    sum += mask[i][j] * images.get(img)[pY][pX];
                }
            }
        }
        return sum;
    }

    public boolean isNaN(double x){return x != x;}

    public void evaluate(final EvolutionState state, 
        final Individual ind, 
        final int subpopulation,
        final int threadnum)
    {
        if (!ind.evaluated)  // don't bother reevaluating
        {
            PicData input = (PicData)(this.input);

            int hits = 0;
            int count = 0;
            double sum = 0.0;
            double result = 0;
            final double PROBABLY_ZERO = 1.11E-15;
            final double BIG_NUMBER = 1.0e15;  // the same as lilgp uses

            for(int[] coords: testingCoords.get(goodImage)){
                int y = coords[0];
                int x = coords[1];
                currentPixel = images.get(goodImage)[y][x];
                currentAvg3 = avg3.get(goodImage)[y][x];
                currentAvg5 = avg5.get(goodImage)[y][x];
                currentAvg7 = avg7.get(goodImage)[y][x];
                currentStd3 = std3.get(goodImage)[y][x];
                currentStd5 = std5.get(goodImage)[y][x];
                currentStd7 = std7.get(goodImage)[y][x];
                currentEdge = edge.get(goodImage)[y][x];
                currentSharpen = sharpen.get(goodImage)[y][x];

                ((GPIndividual)ind).trees[0].child.eval(
                    state,threadnum,input,stack,((GPIndividual)ind),this);
                if(input.x <= 0.0 || isNaN(input.x)){        //if input is negative
                    result = Math.abs(input.x);
                    if (! (result < BIG_NUMBER ) )
                        result = BIG_NUMBER;

                    else if (result<1.0 || isNaN(input.x)) //if result is less that 0 increase the penalty
                        result = 3.0;
						
                    sum += result;
                }else{ 
                    hits++;
                }
                
            }
            for(int i = 0; i < 4; i++){
                if(i!=goodImage){
                    for(int[] coords: testingCoords.get(i)){
                        int y = coords[0];
                        int x = coords[1];
                        currentPixel = images.get(i)[y][x];
                        currentAvg3 = avg3.get(i)[y][x];
                        currentAvg5 = avg5.get(i)[y][x];
                        currentAvg7 = avg7.get(i)[y][x];
                        currentStd3 = std3.get(i)[y][x];
                        currentStd5 = std5.get(i)[y][x];
                        currentStd7 = std7.get(i)[y][x];
                        currentEdge = edge.get(i)[y][x];
                        currentSharpen = sharpen.get(i)[y][x];

                        ((GPIndividual)ind).trees[0].child.eval(
                            state,threadnum,input,stack,((GPIndividual)ind),this);

                        if(input.x > 0.0 || isNaN(input.x)){     //if result is positive when it should be negative
                            result = input.x;
                            if (! (result < BIG_NUMBER ) )
                                result = BIG_NUMBER;

                            else if (result< 1.0 || isNaN(input.x))
                                result = 1.0;

                            sum += result;              
                        }else{                 //its been labelled correctly
                            hits++;
                        } 
                    }
                }
            }

            KozaFitness f = ((KozaFitness)ind.fitness);
            f.setStandardizedFitness(state, sum);
            f.hits = hits;
            ind.evaluated = true;
        }
    }

    public void describe(EvolutionState state, Individual ind, int subpopulation, int threadnum, int log)
    {
        System.out.println("Beginning Test");
            PicData input = (PicData)(this.input);

            int hits = 0;
            int count = 0;
            double sum = 0.0;
            double result = 0;
            final double PROBABLY_ZERO = 1.11E-15;
            final double BIG_NUMBER = 1.0e15;  // the same as 'lilgp uses
            
            for(int i = 0; i < 4; i++){
                boolean[][] confResult = new boolean[256][256];
                for(int y = 0; y< 256;y++){
                    for(int x=0; x<256;x++){
                        if(!sampleReference[goodImage][y][x]){
                            currentPixel = images.get(i)[y][x];
                            currentAvg3 = avg3.get(i)[y][x];
                            currentAvg5 = avg5.get(i)[y][x];
                            currentAvg7 = avg7.get(i)[y][x];
                            currentStd3 = std3.get(i)[y][x];
                            currentStd5 = std5.get(i)[y][x];
                            currentStd7 = std7.get(i)[y][x];
                            currentEdge = edge.get(i)[y][x];
                            currentSharpen = sharpen.get(i)[y][x];

                            ((GPIndividual)ind).trees[0].child.eval(
                                state,threadnum,input,stack,((GPIndividual)ind),this);
                            if(i==goodImage){
                                if(input.x <= 0.0 || isNaN(input.x)){        //if input is negative when it should be positive
                                    confResult[y][x] = false;
                                    result = Math.abs(input.x);
                                    if (! (result < BIG_NUMBER ) )
                                        result = BIG_NUMBER;

                                        else if (result<1.0 || isNaN(input.x)) //if result is less that 0 increase the penis
                                            result = 3.0;

                                        sum += result;
                                }else{
                                    confResult[y][x] = true;
                                    hits++;
                                }
                                                
                            }
                            else{
                                if(input.x > 0.0 || isNaN(input.x)){     //if result is positive when it should be negative
                                    confResult[y][x] = false;
                                    result = input.x;
                                    if (! (result < BIG_NUMBER ) )
                                        result = BIG_NUMBER;

                                    else if (result< 1.0 || isNaN(input.x))
                                        result = 1.0;

                                    sum += result;              
                                }else{                 //its been labelled correctly
                                    confResult[y][x] = true;
                                    hits++;
                                }
                            }  
                        }
                    }                    
                }

                BufferedImage bufferImage = new BufferedImage(256,256,BufferedImage.TYPE_INT_RGB);
                int right = 0, wrong = 0;
                for(int y = 0; y < 256; y++){
                    for(int x = 0; x< 256; x++){
                        if(sampleReference[i][y][x])
                            bufferImage.setRGB(x, y,Color.BLACK.getRGB());
                        else if(confResult[y][x]){
                            bufferImage.setRGB(x, y,Color.GREEN.getRGB());
                            right++;
                        }
                        else{
                            bufferImage.setRGB(x, y,Color.RED.getRGB());
                            wrong ++;
                        }
                    }
                }
                state.output.println(right + " " + wrong,testLog);
                try{
                    boolean boo = ImageIO.write(bufferImage, "png", state.output.getLog(outputFiles[i]).filename);
                    if(boo)
                        System.out.println("printed image");
                }catch(IOException exp){
                    System.out.println("Could not print confusion image " + i);
                }
                
            }

            KozaFitness f = ((KozaFitness)ind.fitness);
            f.setStandardizedFitness(state, sum);
            f.hits = hits;
            state.output.print(f.fitnessToStringForHumans(),testLog);
    }

}

