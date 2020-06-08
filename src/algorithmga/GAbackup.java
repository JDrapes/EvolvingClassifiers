package algorithmga;

import java.util.Random;

/**
 *
 * @author jordandraper
 */
public class GAbackup {

    static int N = 50; //Length of 1s and 0s
    static int P = 50; //population
    static int generations = 50; //Amount of generations to complete
    Population population = new Population(); //Creates the population of individuals
    int indexWorstOffspring;

    public static void main(String[] args) {

        AlgorithmGA demo = new AlgorithmGA();

        //Need to calculate and track best fitness in population and mean fitness in population 
        //Initialize population
        demo.population.initializePopulation(P);

        demo.calculateFitness();  //call function to calc fitness
        //demo.printIndividuals(); //Prints individuals that were created
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

            //Print total population
            demo.totalPopulationFitness(); //Populations fitness

            //Prints fittest individual in this generation
            demo.fittestIndividual();

        }

        //Termination
    }

    //Class for Individual
    class Individual {

        int[] gene = new int[N]; //This needs to be equal to N 
        int fitness;

        public Individual() { //default construcor setting everything to 0 
            Random rn = new Random();

            //Set genes randomly for each individual
            for (int i = 0; i < N; i++) {
                gene[i] = Math.abs(rn.nextInt() % 2);

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

    public void calculateFitness() {
        for (int i = 0; i < P; i++) { //Cycling population
            population.individuals[i].fitness = 0; //Reset fitness sum to 0 
            for (int j = 0; j < N; j++) { //Cycling the individual
                if (population.individuals[i].gene[j] == 1) { //Check if the gene is equal to 1

                    population.individuals[i].fitness++; //If gene is == 1 then we +1 fitness
                }
            }
        }

        for (int i = 0; i < P; i++) { //Cycling population
            population.offspring[i].fitness = 0; //Reset fitness sum to 0 
            for (int j = 0; j < N; j++) { //Cycling the individual
                if (population.offspring[i].gene[j] == 1) { //Check if the gene is equal to 1

                    population.offspring[i].fitness++; //If gene is == 1 then we +1 fitness
                }
            }
        }

        for (int i = 0; i < P; i++) { //Cycling population
            population.bestParent[i].fitness = 0; //Reset fitness sum to 0 
            for (int j = 0; j < N; j++) { //Cycling the individual
                if (population.bestParent[i].gene[j] == 1) { //Check if the gene is equal to 1

                    population.bestParent[i].fitness++; //If gene is == 1 then we +1 fitness
                }
            }
        }

    }

    public void totalPopulationFitness() {
        calculateFitness();
        int populationFitness = 0;
        for (int i = 0; i < P; i++) { //Cycling population
            for (int j = 0; j < N; j++) { //Cycling the individual
                if (population.individuals[i].gene[j] == 1) { //Check if the gene is equal to 1

                    populationFitness++; //If gene is == 1 then we +1 fitness
                }
            }
        }
        populationFitness=populationFitness/N;
        System.out.println("Average population fitness: " + populationFitness);
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

                boolean probability = new Random().nextInt(N) == 0; //Probability 1/chromosome_length applied here 

                if (probability) {

                    if (population.offspring[i].gene[x] == 0) {
                        population.offspring[i].gene[x] = 1;
                    } else if (population.offspring[i].gene[x] == 1) {
                        population.offspring[i].gene[x] = 0;
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

    public void indexBestParent() { //Find the parent with best fitness
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

        if (population.bestParent[0].fitness > population.offspring[indexWorstOffspring].fitness) {
            for (int x = 0; x < N; x++) {
                population.offspring[indexWorstOffspring].gene[x] = population.bestParent[0].gene[x];
            }
        }
    }

    public void fittestIndividual() {
        int index;
        int topFitness = 0;
        for (int i = 0; i < P; i++) {
            if (population.individuals[i].fitness > topFitness) {
                index = i;
                topFitness = population.individuals[i].fitness;
            }

        }
        System.out.println("The fittest individuals fitness is " + topFitness);
    }

} //end 

