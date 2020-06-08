package algorithmga;

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
public class AlgorithmGA {

    static int N = 35; //Length of 1s and 0s
    static int P = 200; //population    
    static int generations = 100; //Amount of generations to complete
    static int mutationRate = 70; //This is a 1 in x for the mutation rate
    static int conL = 6; //Length of the int from textfile 1 and 2
    static int numR = 5; //Amount of rules within the gene length 70/7 = 10.
    Population population = new Population(); //Creates the population of individuals
    Rulebase rulebase = new Rulebase(); //Creates the population of individuals
    int indI = 0;
    static int file1Len = 60; //Length of file 1
    static int wildCard = 2; //Wildcard 2

    int indexWorstOffspring;

    public static void main(String[] args) throws FileNotFoundException, UnsupportedEncodingException {
        PrintWriter writer = new PrintWriter("/Users/jordandraper/Desktop/Files/University/Year 3/BioComp/algorithmGA/src/algorithmga/GAoutput.txt", "UTF-8");
        writer.println("Average fitness, Best fitness");
        AlgorithmGA demo = new AlgorithmGA();

        //Need to calculate and track best fitness in population and mean fitness in population 
        //Initialize population
        demo.population.initializePopulation(P);
        demo.rulebase.initializeRulebase(numR);
        demo.rulebase.initializeDataFile(file1Len);

        demo.loadDataFile(); //Load data1.txt into array
        //demo.showRulesDataFile(); //Shows rules loaded from data 1 - test purposes.

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
    }

    //Class for Individual
    class Individual {

        int[] gene = new int[N]; //This needs to be equal to N 
        int fitness;

        public Individual() { //default construcor setting everything to 0 
            Random rn = new Random();

            //Set genes randomly for each individual
            for (int i = 0; i < N; i++) {

                if ((i + 1) % 7 == 0) { //If i is in the action place we must allow it to only be 0 or 1.
                    gene[i] = Math.abs(rn.nextInt() % 2);
                } else {
                    gene[i] = Math.abs(rn.nextInt() % 3); //gene values can be 2 sometimes

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

    //Class for Conditions
    class Condition {

        int[] cond = new int[conL]; //This has length 6 for the first 6 bits
        int out; //Fitness is the 7th bit

        public Condition() { //default construcor setting everything to 0 
            Random rn = new Random();

            //Set genes randomly for each individual
            for (int i = 0; i < conL; i++) {
                cond[i] = Math.abs(rn.nextInt() % 2);
            }
            out = 0;
        }
    }
//Class for Rulebase

    class Rulebase {

        Condition[] conditions = new Condition[numR]; //Creates 10 rule spaces
        Condition[] dataFile1 = new Condition[file1Len]; //Creates an array

        //Initialize Rulebase
        public void initializeRulebase(int size) {
            for (int i = 0; i < numR; i++) {
                conditions[i] = new Condition();
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

                boolean probability = new Random().nextInt(mutationRate) == 0; //Probability 1/chromosome_length applied here 
                if (probability) {
                    if ((x + 1) % 7 == 0) { //If x mod 7 = 0 then it's the 7th bit (action) and must not be edited
                        if (population.offspring[i].gene[x] == 0) {
                            population.offspring[i].gene[x] = 1;
                        } else if (population.offspring[i].gene[x] == 1) {
                            population.offspring[i].gene[x] = 0;
                        }
                    }
                 else {
                    int randomChance = new Random().nextInt(3);
                    switch (randomChance) {
                        case 1:
                            population.offspring[i].gene[x] = 0;
                            break;
                        case 2:
                            population.offspring[i].gene[x] = 1;
                            break;
                        default:
                            population.offspring[i].gene[x] = 2;
                            break;
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
        for (int x = 0; x < N; x++) {
            individual = individual + population.individuals[index].gene[x];
            System.out.print(population.individuals[index].gene[x]);
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
    public void loadDataFile() throws FileNotFoundException {
        Scanner scanner = new Scanner(new File("/Users/jordandraper/Desktop/Files/University/Year 3/BioComp/algorithmGA/src/algorithmga/data2.txt"));
        int i = 0;
        while (scanner.hasNextLine()) {

            String line = scanner.nextLine();

            rulebase.dataFile1[i].cond[0] = Integer.parseInt(String.valueOf(line.charAt(0)));
            rulebase.dataFile1[i].cond[1] = Integer.parseInt(String.valueOf(line.charAt(1)));
            rulebase.dataFile1[i].cond[2] = Integer.parseInt(String.valueOf(line.charAt(2)));
            rulebase.dataFile1[i].cond[3] = Integer.parseInt(String.valueOf(line.charAt(3)));
            rulebase.dataFile1[i].cond[4] = Integer.parseInt(String.valueOf(line.charAt(4)));
            rulebase.dataFile1[i].cond[5] = Integer.parseInt(String.valueOf(line.charAt(5)));

            //We have a space within the string format "xxxxxx x"
            rulebase.dataFile1[i].out = Integer.parseInt(String.valueOf(line.charAt(7))); //7th including space.

            // System.out.println(line); //line
            i++;

            //Should load the file into an array
        }
        //System.out.println("Data file load complete");
    }

    public void parentToRulebase(int indI) {
        int out = 0;
        int k = 0;
        for (int i = 0; i < numR; i++) {
            for (int j = 0; j < conL; j++) {
                rulebase.conditions[i].cond[j] = population.individuals[indI].gene[k++];
            }
            rulebase.conditions[i].out = population.individuals[indI].gene[k++];
        }
    }

    public void showparentToRulebase() {
        for (int x = 0; x < numR; x++) {

            for (int i = 0; i < conL; i++) {
                System.out.print(rulebase.conditions[x].cond[i]);
            }
            System.out.print(" 7th bit: ");
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

    public void fitnessOfRulesInd(int indI) {
        population.individuals[indI].fitness = 0;
        for (int i = 0; i < file1Len; i++) { //Cycles through the amount of rules from data file e.g. 60
            for (int j = 0; j < numR; j++) {  //Cycles through amount of rules in rules from the individual e.g. 10
                if (((rulebase.dataFile1[i].cond[0] == rulebase.conditions[j].cond[0]) || (rulebase.conditions[j].cond[0] == wildCard))
                        && ((rulebase.dataFile1[i].cond[1] == rulebase.conditions[j].cond[1]) || (rulebase.conditions[j].cond[1] == wildCard))
                        && ((rulebase.dataFile1[i].cond[2] == rulebase.conditions[j].cond[2]) || (rulebase.conditions[j].cond[2] == wildCard))
                        && ((rulebase.dataFile1[i].cond[3] == rulebase.conditions[j].cond[3]) || (rulebase.conditions[j].cond[3] == wildCard))
                        && ((rulebase.dataFile1[i].cond[4] == rulebase.conditions[j].cond[4]) || (rulebase.conditions[j].cond[4] == wildCard))
                        && ((rulebase.dataFile1[i].cond[5] == rulebase.conditions[j].cond[5]) || (rulebase.conditions[j].cond[5] == wildCard))) { //Matches cond
                    if (rulebase.dataFile1[i].out == rulebase.conditions[j].out) { //Matches output
                        //If all 5 bits are the same and the output is the same then fitness ++
                        population.individuals[indI].fitness++;
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
            for (int j = 0; j < conL; j++) {
                rulebase.conditions[i].cond[j] = population.offspring[indI].gene[k++];
            }
            rulebase.conditions[i].out = population.offspring[indI].gene[k++];
        }
    }

    public void fitnessOfRulesOffspring(int indI) {
        population.offspring[indI].fitness = 0;
        for (int i = 0; i < file1Len; i++) { //Cycles through the amount of rules from data file
            for (int j = 0; j < numR; j++) {  //Cycles through amount of rules in rules from the individual
                if (((rulebase.dataFile1[i].cond[0] == rulebase.conditions[j].cond[0]) || (rulebase.conditions[j].cond[0] == wildCard))
                        && ((rulebase.dataFile1[i].cond[1] == rulebase.conditions[j].cond[1]) || (rulebase.conditions[j].cond[1] == wildCard))
                        && ((rulebase.dataFile1[i].cond[2] == rulebase.conditions[j].cond[2]) || (rulebase.conditions[j].cond[2] == wildCard))
                        && ((rulebase.dataFile1[i].cond[3] == rulebase.conditions[j].cond[3]) || (rulebase.conditions[j].cond[3] == wildCard))
                        && ((rulebase.dataFile1[i].cond[4] == rulebase.conditions[j].cond[4]) || (rulebase.conditions[j].cond[4] == wildCard))
                        && ((rulebase.dataFile1[i].cond[5] == rulebase.conditions[j].cond[5]) || (rulebase.conditions[j].cond[5] == wildCard))) { //Matches cond
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
            for (int j = 0; j < conL; j++) {
                rulebase.conditions[i].cond[j] = population.bestParent[indI].gene[k++];
            }
            rulebase.conditions[i].out = population.bestParent[indI].gene[k++];
        }
    }

    public void fitnessOfBestParent(int indI) {
        population.bestParent[indI].fitness = 0;
        for (int i = 0; i < file1Len; i++) { //Cycles through the amount of rules from data file
            for (int j = 0; j < numR; j++) {  //Cycles through amount of rules in rules from the individual
                if (((rulebase.dataFile1[i].cond[0] == rulebase.conditions[j].cond[0]) || (rulebase.conditions[j].cond[0] == wildCard))
                        && ((rulebase.dataFile1[i].cond[1] == rulebase.conditions[j].cond[1]) || (rulebase.conditions[j].cond[1] == wildCard))
                        && ((rulebase.dataFile1[i].cond[2] == rulebase.conditions[j].cond[2]) || (rulebase.conditions[j].cond[2] == wildCard))
                        && ((rulebase.dataFile1[i].cond[3] == rulebase.conditions[j].cond[3]) || (rulebase.conditions[j].cond[3] == wildCard))
                        && ((rulebase.dataFile1[i].cond[4] == rulebase.conditions[j].cond[4]) || (rulebase.conditions[j].cond[4] == wildCard))
                        && ((rulebase.dataFile1[i].cond[5] == rulebase.conditions[j].cond[5]) || (rulebase.conditions[j].cond[5] == wildCard))) { //Matches cond
                    if (rulebase.dataFile1[i].out == rulebase.conditions[j].out) { //Matches output
                        //If all 5 bits are the same and the output is the same then fitness ++
                        population.bestParent[indI].fitness++;
                    }
                    break; // note it is important to get the next data item after a match
                }
            }
        }

    }

} //end 

