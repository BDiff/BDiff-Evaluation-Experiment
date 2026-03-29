This repository hosts the experimental dataset, scripts, and results for the paper BDiff \(submitted to ASE 2026\), which proposes a practical code differencing tool supporting both line\-level and block\-level edit detection\.

All materials provided here are for academic research and reproduction of the paper\&\#39;s experimental results\.

## Repository Structure

The repository is organized into three core directories, with the following structure:

```plain text
├── dataset/                # Experimental dataset (real-world code change cases)
│   ├── GhJava/             # Real code change cases for Java language
│   ├── GhPython/           # Real code change cases for Python language
│   └── GhXML/              # Real code change cases for XML language
├── experiment_scripts/     # Scripts to run BDiff and reproduce experiments
│   ├── BDiff.py            # Core implementation of the BDiff tool
│   ├── LLM_prompt.txt      # Prompt templates for LLM-based baseline tools
│   ├── bdiff_visualization_exporter.py  # Script to run BDiff and export results
│   └── data_process.py     # Auxiliary scripts for experimental data processing
├── experiment_results/     # Experimental results reported in the paper
│   ├── manual_evaluation_results/  # Results of manual evaluation experiments
│   └── mutation_experiment_results/ # Results of mutation-based experiments
└── README.md               # Repository documentation (this file)
```

## Directory \&amp; File Details

### 1\. dataset/

This directory contains all real\-world code change cases used in the paper\&\#39;s experiments, collected from open\-source projects to reflect authentic developer editing behaviors \(including line\-level and block\-level edits such as move, copy, update, merge, split, etc\.\)\.

- **GhJava/**: Java language code change cases, covering various common edit scenarios in Java projects\.

- **GhPython/**: Python language code change cases, focusing on edit patterns specific to Python syntax and development practices\.

- **GhXML/**: XML language code change cases, including edits in configuration files and markup documents\.

### 2\. experiment\_scripts/

This directory provides all executable scripts to run the BDiff tool, reproduce the paper\&\#39;s experiments, and process experimental data\.

- **BDiff\.py**: The core implementation of the BDiff code differencing tool, which supports both line\-level and block\-level edit detection\.

- **LLM\_prompt\.txt**: Prompt templates used for LLM\-based baseline tools in the paper\&\#39;s comparative experiments, ensuring consistency in baseline evaluation\.

- **bdiff\_visualization\_exporter\.py**: A dedicated script to run the BDiff tool on the dataset and export structured experimental results \(e\.g\., Edit Scripts \(ES\), Edit Actions \(EA\)\)\.

- **data\_process\.py**: Auxiliary scripts for experimental data preprocessing, metric calculation, result cleaning, and statistical analysis\.

### 3\. experiment\_results/

This directory stores all experimental results reported in the paper, organized by experiment type for easy reference and reproduction\.

- **manual\_evaluation\_results/**: Results of the manual evaluation experiment, including human\-judged accuracy, recall, and F1\-score of BDiff and baseline tools\.

- **mutation\_experiment\_results/**: Results of the mutation\-based experiment, including mutation score and edit detection performance metrics for all compared tools\.

## Usage Guide

To reproduce the paper\&\#39;s experiments, follow these steps:

1. Ensure the dataset is placed in the `dataset/` directory \(the repository provides the complete dataset by default\)\.

2. Run BDiff and export results using the visualization exporter:
        `python experiment\_scripts/bdiff\_visualization\_exporter\.py`

3. Process experimental data and calculate evaluation metrics:
        `python experiment\_scripts/data\_process\.py`

4. Refer to the `experiment\_results/` directory to compare your reproduced results with the paper\&\#39;s reported results\.

## Key Terminology

- **EA \(Edit Action\)**: The basic edit operation performed by developers \(e\.g\., line adding, block moving, code copying\)\.

- **ES \(Edit Script\)**: A sequence of Edit Actions that describes the complete transformation between two code versions\.

- **Baselines**: Comparative tools including text\-based, AST\-based, and LLM\-based code differencing methods\.

## License

The dataset, scripts, and experimental results in this repository are for **academic research use only**\. For commercial use or further inquiries, please contact the paper\&\#39;s authors\.

> （注：文档部分内容可能由 AI 生成）
