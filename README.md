# TestSyncer
Here is the relevant code and data for TestSyncer. Data in master branch.

## AnalysisTool
This project is used to analyze Change Types and extract Dependency Contexts and Caller Contexts to build prompt. In AnalysisTool\src\main\java\experiment\Usage.java, there is a specific example demonstrating how to use this project for prompt construction. You can run AnalysisTool\src\main\java\experiment\MakePrompt.java to experiment.<br />
**configuration:**
* testsetPath：The path to the Projects folder(Projects folder is provided in master branch)


## Evaluate
This folder contains some Python programs for evaluating the experimental results.

## run_setup.py
Before running the experiment, you need to run this program to download the data needed for the experiment.
