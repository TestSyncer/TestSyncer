# TestSyncer
Here is the relevant code and data for TestSyncer. Data in master branch.

## AnalysisTool
This project is used to analyze Change Types and extract Dependency Contexts and Caller Contexts to build prompt. In AnalysisTool\src\main\java\experiment\Usage.java, there is a specific example demonstrating how to use this project for prompt construction. You can run AnalysisTool\src\main\java\experiment\MakePrompt.java to experiment.<br /><br />
**configuration.** Modify your custom configuration in AnalysisTool\src\main\java\experiment\MakePrompt.java:
* testsetPath: The path to the Projects folder. (Projects folder is provided in master branch, it contains only two samples. You need to run run_setup.py to supplement the remaining samples)
* outputPath: The path to the output folder where the generated prompt are stored. (We provided our expiremental results in PromptAndResult folder in master branch)
* jsonFilePath: The path to test.json. (test.json is provided in master branch)

## Evaluate
This folder contains some Python programs for evaluating the experimental results.<br /><br />
**configuration.** Modify your custom configuration:
* base_path: The path to the output folder where the generated test method are stored. (We provided our expiremental results in PromptAndResult folder in master branch)

## run_setup.py
Before running the experiment, you need to run this program to download the data needed for the experiment.<br /><br />
**configuration.** Modify your custom configuration:
* output_folder: The path to the Projects folder. (Projects folder is provided in master branch, it contains only two samples. You need to run run_setup.py to supplement the remaining samples)
* json_path: The path to test.json. (test.json is provided in master branch)
