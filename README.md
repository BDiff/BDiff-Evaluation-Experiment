This repository hosts the experimental dataset, scripts, and results for the paper ”BDiff: Block-aware and Accurate Text-based Code Differencing" \(submitted to ASE 2026\), which proposes a practical code differencing tool supporting both accurate line\-level and block\-level edit detection\.

All materials provided here are for academic research and reproduction of the paper's experimental results\.

## Repository Structure

The repository is organized into three core directories, with the following structure:

```plain text
├── dataset/                             # Experimental dataset (real-world code change cases)
│   ├── GhJava/                          # Real code change cases for Java language
│   ├── GhPython/                        # Real code change cases for Python language (Produced by Falleri et al.)
│   └── GhXML/                           # Real code change cases for XML language
├── experiment_scripts/                  # Scripts to run BDiff and reproduce experiments
│   ├── BDiff.py                         # Core implementation of BDiff in the experiment
│   ├── LLM_prompt.txt                   # Prompt templates for LLM-based baseline tools
│   ├── bdiff_visualization_exporter.py  # Script to run BDiff and export results
│   └── data_process.py                  # Auxiliary scripts for experimental data processing
├── experiment_results/                  # Experimental results reported in the paper
│   ├── manual_evaluation_results/       # Results of manual evaluation experiments
│   |── mutation_experiment_results/     # Results of mutation-based experiments
│   |── bdiff_histogram.csv              # BDiff results using the histogram diff algorithm in Git as the base diff algorithm
│   |── bdiff_minimal.csv                # BDiff results using the minimal diff algorithm in Git as the base diff algorithm
│   |── bdiff_myers.csv                  # BDiff results using the myers diff algorithm in Git as the base diff algorithm
│   |── bdiff_patience.csv               # BDiff results using the patience diff algorithm in Git as the base diff algorithm
│   |── bdiffeditscripts.csv             # Results produced by BDiff
│   |── copyactions.csv                  # Results of block copies produced by BDiff
│   |── gpt5editscripts.csv              # Results produced by GPT-5-mini
│   |── gumtreeeditscripts.csv           # Results produced by GumTree
│   |── ldiffeditscripts.csv             # Results produced by ldiff
│   |── moveactions.csv                  # Results of block moves produced by BDiff
│   |── projects.csv                     # The GitHub projects used in the experiment
│   |── qweneditscripts.csv              # Results produced by Qwen3-32B
│   |── updateactions.csv                # Results of line updates produced by BDiff
├── bdiff.py                             # Implementation of the BDiff tool
└── README.md                            # Repository documentation (this file)
```

## Directory File Details

### 1\. dataset/

This directory contains all real\-world code change cases used in the paper's experiments, collected from open\-source projects to reflect authentic developer editing behaviors \(including line\-level and block\-level edits such as move, copy, update, merge, split, etc\.\)\.

- **GhJava/**: Java language code change cases extracted from 10 GiHub projects\ (Produced by Falleri et al. for evaluating GumTree).

- **GhPython/**: Python language code change cases extracted from 10 GiHub projects\ (Produced by Falleri et al. for evaluating GumTree).

- **GhXML/**: XML language code change cases extracted from 10 GiHub projects\..

### 2\. experiment\_scripts/

This directory provides executable scripts to run the BDiff tool, reproduce the paper's experiments, and process experimental data\.

- **BDiff\.py**: The core implementation of the BDiff code differencing tool.

- **LLM\_prompt\.txt**: Prompt used for LLM\-based baseline tools in the paper's comparative experiments.

- **bdiff\_visualization\_exporter\.py**: A dedicated script to run the BDiff tool on the dataset and export structured edit script.

- **data\_process\.py**: Auxiliary scripts for experimental data preprocessing, metric calculation, and result cleaning.

### 3\. experiment\_results/

This directory stores all experimental results reported in the paper, organized by experiment type for easy reference and reproduction\.

- **manual\_evaluation\_results/**: Results of the manual evaluation experiment, including human\-judged scores of BDiff and baseline tools\.

- **mutation\_experiment\_results/**: Results of the mutation\-based experiment, including the generated file in the "generated_files" folder and the experimental results.


## License

The dataset, scripts, and experimental results in this repository are for **academic research use only**\. For commercial use or further inquiries, please contact the paper's authors\.
