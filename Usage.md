# Installation #
Copy downloaded jar into After download copy this into $RAPIDMINER/lib/plugins.

# Usage #
## Learning ##
USe either HMM Learner, HMM Baum-Welch Learner or HMM KMeans Learner. The first one is suitable if you have sequence of both observations and hidden states, the later two are usable when you have only observations and you want to fit structure of the HMM to these observations.

## Inference ##
Use standard RapidMiner operator "Apply Model". There are two algorithms that can be used for labeling sequence of observations with hidden state - Viterbi and Forward-Backward. Use the "application parameters" property of the "Apply Model" operator to switch between them. You can do this by adding key "algorithm" with value VITERBI or FORWARD\_BACKWARD.