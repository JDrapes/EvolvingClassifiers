package datafile3;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.PrintWriter;
import java.io.UnsupportedEncodingException;
import java.util.Random;
import java.util.Scanner;

/**
 *
 * @author jordandraper
 */
public class file3 {

    static int N = 65; //Length of 1s and 0s
    static int P = 400; //population
    static int generations = 10000; //Amount of generations to complete
    static int mutationChance = 110; //This is a 1 in x for the mutation chance
    static int mutationJump = 200000; //This is the range + or - that the mutation can jump... imagine there is a "0." in front of the value 
    static int conL = 6; //Length of the int from textfile 1 and 2
    static int numR = 5; //Amount of rules within the gene length 70/7 = 10.
    static int ruleLength = (N / numR) - 1; //Rule length, EXCLUDES the action bit.
    Population population = new Population(); //Creates the population of individuals
    Rulebase rulebase = new Rulebase(); //Creates the population of individuals
    int indI = 0;
    static int file1Len = 1000; //Length of file 1

    int indexWorstOffspring;

    public static void main(String[] args) throws FileNotFoundException, UnsupportedEncodingException {

        PrintWriter writer = new PrintWriter("/Users/jordandraper/Desktop/Files/University/Year 3/BioComp/algorithmGA/src/datafile3/GAoutput.txt", "UTF-8");
        writer.println("Average fitness, Best fitness");
        file3 demo = new file3();

        //Need to calculate and track best fitness in population and mean fitness in population 
        //Initialize population
        demo.population.initializePopulation(P);
        demo.rulebase.initializeRulebase(numR);
        demo.rulebase.initializeDataFile(file1Len);

        demo.loadDataFile(); //Load data1.txt into array
        //demo.showRulesDataFile(); //Shows rules loaded from data 1 - test purposes..

        demo.calculateFitness();  //call function to calc fitness

        demo.totalPopulationFitness(); //Populations fitness

        demo.fittestIndividual();

        for (int generationCount = 0; generationCount < generations; generationCount++) {
            System.out.println("Generation: " + generationCount);

            //Find index of best parent
            demo.indexBestParent(); //need to copy best parent out somewhere
            //Do selection of parents
            demo.selection(); //Perform selection of parents randomly into offsprings
            //Do crossover (recombine parents)
            demo.crossover();
            //Do mutation and results goes to offspring
            demo.mutation();

            //Find index of worst offspring
            demo.indexWorstOffspring();
            //Swap worst offspring for best parent
            demo.swapBestParentWorstOffspring();
            //Load the offspring to replace parents
            demo.survivorSelection();
            //Recalculate fitness of the population
            demo.calculateFitness();

            //Print average population fitness and top fitness
            writer.println(demo.totalPopulationFitness() + "," + demo.fittestIndividualFitness());
            //Prints fittest individual in this generation
            demo.fittestIndividual();

        }

        //Termination
        writer.println("Fittest individual below: ");
        writer.println(demo.fittestIndividual());
        writer.close(); //Stop writing to file

        //Test last value on unseen data.
        demo.fitnessUnseenData();
    }

    //Class for Individual
    class Individual {

        int[] gene = new int[N]; //This needs to be equal to N 
        int fitness;

        public Individual() { //default construcor setting everything to 0 
            Random rn = new Random();

            //Set genes randomly for each individual
            for (int i = 0; i < N; i++) {
                if ((i + 1) % 13 == 0) {
                    gene[i] = Math.abs(rn.nextInt() % 2);
                } else {
                    gene[i] = Math.abs(rn.nextInt() % 999999); //Changed from 2 to 999999
                }
            }
            fitness = 0;
        }
    }
//Class for population

    class Population {

        Individual[] individuals = new Individual[P];
        Individual[] offspring = new Individual[P];
        Individual[] bestParent = new Individual[P];

        //Initialize population
        public void initializePopulation(int size) {
            for (int i = 0; i < P; i++) {
                individuals[i] = new Individual();
                offspring[i] = new Individual(); //array to store offsprings
                bestParent[i] = new Individual(); //array to store the best parent           
            }

        }

    }

    //Class for Conditions this is used just for the datafile conditions
    class Condition {

        int[] cond = new int[conL]; //This has length 6 for the first 6 bits

        int out; //Fitness is the 7th bit

        public Condition() { //default construcor setting everything to 0 
            Random rn = new Random();

            //Set genes randomly for each individual
            for (int i = 0; i < conL; i++) {
                cond[i] = Math.abs(rn.nextInt() % 999999); //random between 0 to 999999
            }
            out = 0;
        }
    }

    //This is used to put into conditions the individuals etc that I randomly create
    class upperLowerCondition {

        int[] cond = new int[ruleLength]; //This has length 12 for the first 12 bits

        int out; //Fitness is the 13th bit
        int fitness;

        public upperLowerCondition() { //default construcor setting everything to 0 
            Random rn = new Random();

            //Set genes randomly for each individual
            for (int i = 0; i < ruleLength; i++) {
                cond[i] = Math.abs(rn.nextInt() % 999999);
            }
            out = 0;
            fitness = 0;
        }
    }
//Class for Rulebase

    class Rulebase {

        upperLowerCondition[] conditions = new upperLowerCondition[numR]; //Creates 10 rule spaces
        Condition[] dataFile1 = new Condition[file1Len]; //Creates an array

        //Initialize Rulebase
        public void initializeRulebase(int size) {
            for (int i = 0; i < numR; i++) {
                conditions[i] = new upperLowerCondition();
            }
        }

        public void initializeDataFile(int size) {
            for (int i = 0; i < file1Len; i++) {
                dataFile1[i] = new Condition();
            }
        }
    }

    public void calculateFitness() {

        for (int i = 0; i < P; i++) {
            population.individuals[i].fitness = 0; //Reset fitness to 0
            population.offspring[i].fitness = 0; //Reset fitness sum to 0 
            population.bestParent[i].fitness = 0; //Reset fitness sum to 0 

            parentToRulebase(i);
            fitnessOfRulesInd(i);

            offSpringToRulebase(i);
            fitnessOfRulesOffspring(i);

            bestParentToRulebase(i);
            fitnessOfBestParent(i);
        }

    }

    public int totalPopulationFitness() {
        calculateFitness();
        int totalFitness = 0;
        int populationFitness = 0;
        for (int i = 0; i < P; i++) { //Cycling population
            totalFitness = totalFitness + population.individuals[i].fitness;
        }

        populationFitness = totalFitness / P;

        System.out.println("Average population fitness: " + populationFitness);
        return populationFitness;
    }

    public void selection() {
        for (int i = 0; i < P; i++) { //Will create same amount of offspring as there is parents
            int parent1 = (int) (Math.random() * P); //Randomly select 2 parents
            int parent2 = (int) (Math.random() * P);
            if (population.individuals[parent1].fitness >= population.individuals[parent2].fitness) {
                //Whichever parent is more fit is added to an array called offspring
                for (int x = 0; x < N; x++) {
                    population.offspring[i].gene[x] = population.individuals[parent1].gene[x];
                }
            } else {
                for (int x = 0; x < N; x++) {
                    population.offspring[i].gene[x] = population.individuals[parent2].gene[x];
                }
            }
        }
        calculateFitness();
    }

    public void crossover() { //This is an explorative function, makes a big jump between parent areas
        //Choose a random point on the two parents, Split parents at this crossover point
        int crossoverPoint = (int) (Math.random() * N);
        //Create children by exchanging tails
        //For now just crossing over between 2 parents randomly - can change this to cycle all parents if needed
        int parent1 = (int) (Math.random() * P); //Randomly select 2 parents
        int parent2 = (int) (Math.random() * P);
        //int parent1 = 0;
        //int parent2 = 1;
        //System.out.println("CROSSOVER " + crossoverPoint);
        //Create an N digit temp array 
        for (int i = N - 1; i > crossoverPoint; i--) {

            //Put parent array 1 genes up to crossover the temp array  
            int temp = population.offspring[parent1].gene[i];
            //Put parent array 2 genes up to crossover point into parent array 1
            population.offspring[parent1].gene[i] = population.offspring[parent2].gene[i];
            //Put temp array genes up to the crossover into parent array 2
            population.offspring[parent2].gene[i] = temp;
        }
        calculateFitness();
    }

    public void mutation() { //This is an explorative function creating random small perpubations
        //Alter each gene independently with a probability Pm
        //int mutationRate
        //Pm is called the mutation rate. Typically between 1/pop_size and 1/ chromosome_length
        for (int i = 0; i < P; i++) {
            for (int x = 0; x < N; x++) {

                boolean probability = new Random().nextInt(mutationChance) == 0; //Probability 1/chromosome_length applied here 
                if (probability) {

                    if ((x + 1) % 13 == 0) {
                        //Need to edit the ==0 to be the upper/lower bounds of a 0 or a 1 int
                        if (population.offspring[i].gene[x] == 0) {
                            population.offspring[i].gene[x] = 1;
                        } else if (population.offspring[i].gene[x] == 1) {
                            population.offspring[i].gene[x] = 0;
                        }
                    } else {
                        //need mutation here to handle other numbers. 
                        int change = new Random().nextInt(mutationJump); //Gets the amount to mutate by
                        int plusOrMinus = new Random().nextInt(2); //Decides whether we add or minus the value
                        //Need to make sure it stays in bounds of 0.000001 and 0.999999
                        if (plusOrMinus == 0) { //If we got 0 then we add the change
                            population.offspring[i].gene[x] = population.offspring[i].gene[x] + change;
                            if (population.offspring[i].gene[x] > 999999) { //If the value goes above bounds reset it 
                                population.offspring[i].gene[x] = 999999;
                            }
                            if (population.offspring[i].gene[x] < 1) { //If the value goes below bounds reset it
                                population.offspring[i].gene[x] = 1;
                            }
                        } else { //If we got 1 then we are minusing the change
                            population.offspring[i].gene[x] = population.offspring[i].gene[x] - change;
                            if (population.offspring[i].gene[x] > 999999) { //If the value goes above of bounds reset it 
                                population.offspring[i].gene[x] = 999999;
                            }
                            if (population.offspring[i].gene[x] < 1) { //If the value goes below bounds reset it 
                                population.offspring[i].gene[x] = 1;
                            }
                        }

                    }

                }
            }
        }
        calculateFitness();
    }

    public void survivorSelection() {//Replace offspring with parents 
        for (int i = 0; i < P; i++) {
            for (int x = 0; x < N; x++) {
                population.individuals[i].gene[x] = population.offspring[i].gene[x];
            }
        }
    }

    public void indexBestParent() { //Find the parent with best fitness and write it to bestParent[0]
        calculateFitness();
        int actualFitness = 0;
        int indexTopParent = 0;
        for (int i = 0; i < P; i++) {
            if (population.individuals[i].fitness > actualFitness) {
                actualFitness = population.individuals[i].fitness;
                indexTopParent = i; //Can use this index
            }
        }
        for (int x = 0; x < N; x++) {
            population.bestParent[0].gene[x] = population.individuals[indexTopParent].gene[x];
        }
    }

    public int indexWorstOffspring() { //Replace the worst fitness in new gen from best fitness in last gen
        calculateFitness();
        int indexWorstOffspring = 0;
        int actualFitness = N;
        for (int i = 0; i < P; i++) {
            if (population.offspring[i].fitness < actualFitness) {
                actualFitness = population.offspring[i].fitness;
                indexWorstOffspring = i; //Can use this index
            }
        }
        return indexWorstOffspring;
    }

    public void swapBestParentWorstOffspring() {
        int indexWorstOffspring = indexWorstOffspring();

        if (population.bestParent[0].fitness > population.offspring[indexWorstOffspring].fitness) {
            for (int x = 0; x < N; x++) {
                population.offspring[indexWorstOffspring].gene[x] = population.bestParent[0].gene[x];
            }
        }
    }

    public String fittestIndividual() {
        String individual = "";
        int index = 0;
        int topFitness = 0;
        for (int i = 0; i < P; i++) {
            if (population.individuals[i].fitness > topFitness) {
                index = i;
                topFitness = population.individuals[i].fitness;
            }

        }
        System.out.println("The best rulebase is: ");

//        System.out.println("L1 " + population.individuals[index].gene[0] + " , U1 " + population.individuals[index].gene[1]);
//        System.out.println("L2 " + population.individuals[index].gene[2] + " , U2 " + population.individuals[index].gene[3]);
//        System.out.println("L3 " + population.individuals[index].gene[4] + " , U3 " + population.individuals[index].gene[5]);
//        System.out.println("L4 " + population.individuals[index].gene[6] + " , U4 " + population.individuals[index].gene[7]);
//        System.out.println("L5 " + population.individuals[index].gene[8] + " , U5 " + population.individuals[index].gene[9]);
//        System.out.println("L6" + population.individuals[index].gene[10] + " , U6 " + population.individuals[index].gene[11]);
        for (int j = 0; j < N; j = j + 13) {
            individual = "";
            individual = "< " + population.individuals[index].gene[j] + "," + population.individuals[index].gene[j + 1] + " > "
                    + " < " + population.individuals[index].gene[j + 2] + "," + population.individuals[index].gene[j + 3] + " > "
                    + " < " + population.individuals[index].gene[j + 4] + "," + population.individuals[index].gene[j + 5] + " > "
                    + " < " + population.individuals[index].gene[j + 6] + "," + population.individuals[index].gene[j + 7] + " > "
                    + " < " + population.individuals[index].gene[j + 8] + "," + population.individuals[index].gene[j + 9] + " > "
                    + " < " + population.individuals[index].gene[j + 10] + "," + population.individuals[index].gene[j + 11] + " > "
                    + "BIT: " + population.individuals[index].gene[j + 12];
            System.out.println(individual);
        }

        System.out.println(" The fittest individuals fitness is " + topFitness);

        return individual;
    }

    public int fittestIndividualFitness() {
        int index = 0;
        int topFitness = 0;
        for (int i = 0; i < P; i++) {
            if (population.individuals[i].fitness > topFitness) {
                index = i;
                topFitness = population.individuals[i].fitness;
            }
        }
        return topFitness;
    }

    //data file 3 structure is 
    //0.803662 0.981136 0.369132 0.498354 0.067417 0.422276 0
    public void loadDataFile() throws FileNotFoundException {
        Scanner scanner = new Scanner(new File("/Users/jordandraper/Desktop/Files/University/Year 3/BioComp/algorithmGA/src/datafile3/data3_training.txt"));
        int i = 0;
        while (scanner.hasNextLine()) {
            String line = scanner.nextLine();
            String[] details = line.split(" ");
            //Take the whole line and split by space, then on the 6 initial floats drop the 0. to make it int
            rulebase.dataFile1[i].cond[0] = Integer.parseInt(details[0].substring(2)); //value1
            rulebase.dataFile1[i].cond[1] = Integer.parseInt(details[1].substring(2)); //value2
            rulebase.dataFile1[i].cond[2] = Integer.parseInt(details[2].substring(2)); //value3
            rulebase.dataFile1[i].cond[3] = Integer.parseInt(details[3].substring(2)); //value4
            rulebase.dataFile1[i].cond[4] = Integer.parseInt(details[4].substring(2)); //value5
            rulebase.dataFile1[i].cond[5] = Integer.parseInt(details[5].substring(2)); //value6
            rulebase.dataFile1[i].out = Integer.parseInt(details[6]);
            i++; //increment data

        }
    }

    public void loadDataFileUnseenData() throws FileNotFoundException {
        Scanner scanner = new Scanner(new File("/Users/jordandraper/Desktop/Files/University/Year 3/BioComp/algorithmGA/src/datafile3/data3_test.txt"));
        int i = 0;
        while (scanner.hasNextLine()) {
            String line = scanner.nextLine();
            String[] details = line.split(" ");
            //Take the whole line and split by space, then on the 6 initial floats drop the 0. to make it int
            rulebase.dataFile1[i].cond[0] = Integer.parseInt(details[0].substring(2)); //value1
            rulebase.dataFile1[i].cond[1] = Integer.parseInt(details[1].substring(2)); //value2
            rulebase.dataFile1[i].cond[2] = Integer.parseInt(details[2].substring(2)); //value3
            rulebase.dataFile1[i].cond[3] = Integer.parseInt(details[3].substring(2)); //value4
            rulebase.dataFile1[i].cond[4] = Integer.parseInt(details[4].substring(2)); //value5
            rulebase.dataFile1[i].cond[5] = Integer.parseInt(details[5].substring(2)); //value6
            rulebase.dataFile1[i].out = Integer.parseInt(details[6]);
            i++; //increment data

        }
    }

    public void parentToRulebase(int indI) {
        int out = 0;
        int k = 0;

        for (int i = 0; i < numR; i++) {
            for (int j = 0; j < ruleLength; j++) {
                rulebase.conditions[i].cond[j] = population.individuals[indI].gene[k++];
            }
            rulebase.conditions[i].out = population.individuals[indI].gene[k++];
        }
    }

    public void showparentToRulebase() {
        for (int x = 0; x < numR; x++) {

            for (int i = 0; i < ruleLength; i++) {
                System.out.print(rulebase.conditions[x].cond[i]);
            }
            System.out.print(" 13th bit: ");
            System.out.print(rulebase.conditions[x].out);
            System.out.println(" ");
        }
        return;
    }

    public void showRulesDataFile() {
        for (int i = 0; i < file1Len; i++) {

            for (int x = 0; x < conL; x++) {
                System.out.print(rulebase.dataFile1[i].cond[x]);
            }
            System.out.print(" 7th bit: ");
            System.out.print(rulebase.dataFile1[i].out);
            System.out.println(" ");
        }
        return;
    }

    //<L1, U1>, <L2, U2> ... <[0],[1]>, <[2],[3]>...
    public void fitnessOfRulesInd(int indI) {
        population.individuals[indI].fitness = 0;
        for (int i = 0; i < file1Len; i++) { //Cycles through the amount of rules from data file e.g. 60
            for (int j = 0; j < numR; j++) {  //Cycles through amount of rules in rules from the individual e.g. 10

                if (((rulebase.conditions[j].cond[0] < rulebase.dataFile1[i].cond[0]) && (rulebase.dataFile1[i].cond[0] < rulebase.conditions[j].cond[1]))
                        && ((rulebase.conditions[j].cond[2] < rulebase.dataFile1[i].cond[1]) && (rulebase.dataFile1[i].cond[1] < rulebase.conditions[j].cond[3]))
                        && ((rulebase.conditions[j].cond[4] < rulebase.dataFile1[i].cond[2]) && (rulebase.dataFile1[i].cond[2] < rulebase.conditions[j].cond[5]))
                        && ((rulebase.conditions[j].cond[6] < rulebase.dataFile1[i].cond[3]) && (rulebase.dataFile1[i].cond[3] < rulebase.conditions[j].cond[7]))
                        && ((rulebase.conditions[j].cond[8] < rulebase.dataFile1[i].cond[4]) && (rulebase.dataFile1[i].cond[4] < rulebase.conditions[j].cond[9]))
                        && ((rulebase.conditions[j].cond[10] < rulebase.dataFile1[i].cond[5]) && (rulebase.dataFile1[i].cond[5] < rulebase.conditions[j].cond[11]))) { //Matches cond
                    if (rulebase.dataFile1[i].out == rulebase.conditions[j].out) { //Matches output
                        //If all 5 bits are the same and the output is the same then fitness ++
                        population.individuals[indI].fitness++;
                        rulebase.conditions[j].fitness++;
                    }

                    break;
                }
            } //When the rule matches should we move on??? fitness is going above 10 it shouldnt? 
        }

        //System.out.println("Individual fitness based on rules: " + population.individuals[indI].fitness);
    }

    public void offSpringToRulebase(int indI) {
        int out = 0;
        int k = 0;
        for (int i = 0; i < numR; i++) {
            for (int j = 0; j < ruleLength; j++) {
                rulebase.conditions[i].cond[j] = population.offspring[indI].gene[k++];
            }
            rulebase.conditions[i].out = population.offspring[indI].gene[k++];
        }
    }

    public void fitnessOfRulesOffspring(int indI) {
        population.offspring[indI].fitness = 0;
        for (int i = 0; i < file1Len; i++) { //Cycles through the amount of rules from data file
            for (int j = 0; j < numR; j++) {  //Cycles through amount of rules in rules from the individual

                if (((rulebase.conditions[j].cond[0] < rulebase.dataFile1[i].cond[0]) && (rulebase.dataFile1[i].cond[0] < rulebase.conditions[j].cond[1]))
                        && ((rulebase.conditions[j].cond[2] < rulebase.dataFile1[i].cond[1]) && (rulebase.dataFile1[i].cond[1] < rulebase.conditions[j].cond[3]))
                        && ((rulebase.conditions[j].cond[4] < rulebase.dataFile1[i].cond[2]) && (rulebase.dataFile1[i].cond[2] < rulebase.conditions[j].cond[5]))
                        && ((rulebase.conditions[j].cond[6] < rulebase.dataFile1[i].cond[3]) && (rulebase.dataFile1[i].cond[3] < rulebase.conditions[j].cond[7]))
                        && ((rulebase.conditions[j].cond[8] < rulebase.dataFile1[i].cond[4]) && (rulebase.dataFile1[i].cond[4] < rulebase.conditions[j].cond[9]))
                        && ((rulebase.conditions[j].cond[10] < rulebase.dataFile1[i].cond[5]) && (rulebase.dataFile1[i].cond[5] < rulebase.conditions[j].cond[11]))) { //Matches cond
                    if (rulebase.dataFile1[i].out == rulebase.conditions[j].out) { //Matches output                 
                        //If all 5 bits are the same and the output is the same then fitness ++
                        population.offspring[indI].fitness++;
                    }
                    break; // note it is important to get the next data item after a match
                }
            }
        }
    }

    public void bestParentToRulebase(int indI) {
        int out = 0;
        int k = 0;
        for (int i = 0; i < numR; i++) {
            for (int j = 0; j < ruleLength; j++) {
                rulebase.conditions[i].cond[j] = population.bestParent[indI].gene[k++];
            }
            rulebase.conditions[i].out = population.bestParent[indI].gene[k++];
        }
    }

    public void fitnessOfBestParent(int indI) {
        population.bestParent[indI].fitness = 0;
        for (int i = 0; i < file1Len; i++) { //Cycles through the amount of rules from data file
            for (int j = 0; j < numR; j++) {  //Cycles through amount of rules in rules from the individual

                if (((rulebase.conditions[j].cond[0] < rulebase.dataFile1[i].cond[0]) && (rulebase.dataFile1[i].cond[0] < rulebase.conditions[j].cond[1]))
                        && ((rulebase.conditions[j].cond[2] < rulebase.dataFile1[i].cond[1]) && (rulebase.dataFile1[i].cond[1] < rulebase.conditions[j].cond[3]))
                        && ((rulebase.conditions[j].cond[4] < rulebase.dataFile1[i].cond[2]) && (rulebase.dataFile1[i].cond[2] < rulebase.conditions[j].cond[5]))
                        && ((rulebase.conditions[j].cond[6] < rulebase.dataFile1[i].cond[3]) && (rulebase.dataFile1[i].cond[3] < rulebase.conditions[j].cond[7]))
                        && ((rulebase.conditions[j].cond[8] < rulebase.dataFile1[i].cond[4]) && (rulebase.dataFile1[i].cond[4] < rulebase.conditions[j].cond[9]))
                        && ((rulebase.conditions[j].cond[10] < rulebase.dataFile1[i].cond[5]) && (rulebase.dataFile1[i].cond[5] < rulebase.conditions[j].cond[11]))) { //Matches cond

                    if (rulebase.dataFile1[i].out == rulebase.conditions[j].out) { //Matches output
                        //If all 5 bits are the same and the output is the same then fitness ++
                        population.bestParent[indI].fitness++;
                    }
                    break; // note it is important to get the next data item after a match
                }
            }
        }

    }

    public void fitnessUnseenData() throws FileNotFoundException {
        int index = 0;
        int topFitness = 0;
        for (int i = 0; i < P; i++) {
            if (population.individuals[i].fitness > topFitness) {
                index = i;
                topFitness = population.individuals[i].fitness;
            }

            //Gets index as the top fitness
            parentToRulebase(index);
            //Switch to data file 2 before calculating fitness
            loadDataFileUnseenData();
            //Calculate fitness
            fitnessOfRulesInd(index);
            System.out.println("Fitness on unseen dataset below: " + population.individuals[index].fitness);
        }
    }

}
