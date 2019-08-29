# RPG
For a planning problem in PDDL, a planner constructs a state graph that contains all possible states, and actions that trigger transitions between an initial state and a goal state.

This project is contains the Java implementation of the state graph.

## Additional Features
* Metrics on the state graph
* Produces DOT representation of the graph for visualization

# Modules
* Harness :- Generates test cases (labeled observations) to evaluate the trained model
* TraceGenerator :- Generates observation traces of actions for training and testing the decision tree
* Run :- Generates state graph and computes feature values for actions in the provided observation traces
* Preprocessor :- Generates CSV files from the outputs generated from Run module for WEKA to process.

# Execution Steps
* State space enumeration
    * Training
        - run.runML (change domain name in TrainingConfigs, and mode=0)
    * Testing
        - decisiontree.Preprocessor (change domain, alg=0, mode=0 in Preprocessor.java)
* TopK (repeat 2 times K=50, K=100)
    * Training
        - run.RunTopK (change domain name in TrainingConfigs, mode=0)
    * Testing
        - decisiontree.Preprocessor (change domain, alg=1, mode=1 in Preprocessor.java)
* R&G 2010
    - Run rg.RunRG.java
* Vered 2018
    - Run vered.RunVd.java


* Use WEKA to get the model predictions.
    * run predictor.Preprocess.java
    * run predictor.AccuracyCalculator.java
    
