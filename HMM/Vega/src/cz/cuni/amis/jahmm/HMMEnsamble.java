/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package cz.cuni.amis.jahmm;

import be.ac.ulg.montefiore.run.jahmm.Hmm;
import be.ac.ulg.montefiore.run.jahmm.Observation;
import be.ac.ulg.montefiore.run.jahmm.ObservationInteger;
import be.ac.ulg.montefiore.run.jahmm.Opdf;
import be.ac.ulg.montefiore.run.jahmm.OpdfFactory;
import be.ac.ulg.montefiore.run.jahmm.learn.KMeansLearner;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.SortedMap;
import java.util.TreeMap;

/**
 * Set of discriminative HMMs, each one is trained to classify one class.
 * @author ik
 */
public class HMMEnsamble<O extends ObservationInteger> {

    public class EnsambleClassificationResult {

        List<String> classes = new ArrayList<String>();
        /**
         * probTab[i][t] = probability that o_1:t was generated by HMM i
         */
        double[][] probTab = null;
        /**
         * Index of the best matching HMM at each time step.
         */
        int[] bestHmmIx = null;

        protected EnsambleClassificationResult(int classesNum, int sequenceLength) {
            probTab = new double[classesNum][sequenceLength];
            bestHmmIx = new int[sequenceLength];
        }

        public double[][] getProbTab() {
            return probTab;
        }

        public List<String> getClasses() {
            return classes;
        }

        public int[] getBestHmmIx() {
            return bestHmmIx;
        }

        public String getPredictedClass(int t) {
            return classes.get(bestHmmIx[t]);
        }
        
    }

    public class Tuple<O extends Observation> {

        O observation;
        String clazz;
    }
    /**
     * Map holding mapping between HMM and class it should predict.
     */
    SortedMap<String, Hmm<O>> hmmMap = new TreeMap<String, Hmm<O>>();

    /*
    public void learn(Collection<List<Tuple>> c) {
        // for each sequence in collection of all sequences
        for (List<Tuple> sequence : c) {
            // find continous sequence with one target class
            Iterator<Tuple> it = sequence.iterator();

            //while(it.hasNext())
        }

    }
*/
    public void learn(int nbStates, OpdfFactory<? extends Opdf<O>> opdfFactory, Map<String, List<List<O>>> examples) {
        // for each group of examples
        for (Map.Entry<String, List<List<O>>> entry : examples.entrySet()) {
            // create new HMM using KMeans learner
            KMeansLearner<O> kMeansLearner = new KMeansLearner<O>(nbStates, opdfFactory, entry.getValue());
            Hmm<O> hmm = kMeansLearner.learn();
            // store the learned HMM in map holding one HMM for each class
            hmmMap.put(entry.getKey(), hmm);
        }
    }

    public EnsambleClassificationResult classify(List<O> observations) {
        // compute probability for each HMM
        EnsambleClassificationResult result = new EnsambleClassificationResult(hmmMap.size(), observations.size());
        int i = 0;

        for (Map.Entry<String, Hmm<O>> entry : hmmMap.entrySet()) {
            Hmm<O> hmm = entry.getValue();
            FBExtended fb = new FBExtended(observations, hmm);
            result.probTab[i++] = fb.probabilityForEachStep();
            result.classes.add(entry.getKey());
        }

        // pick HMM with highest probability
        for (int t = 0; t < observations.size(); t++) {
            // find index of the most probable HMM
            double max = Double.MIN_VALUE;
            int maxIndex = -1;

            for (i = 0; i < hmmMap.size(); i++) {
                if (result.probTab[i][t] > max) {
                    max = result.probTab[i][t];
                    maxIndex = i;
                }
            }
            assert (maxIndex >= 0);
            result.bestHmmIx[t] = maxIndex;
        }
        return result;
    }

    @Override
    public String toString() {
        StringBuilder builder = new StringBuilder();
        builder.append("HMM Ensamble\n");
        builder.append("Number of internal HMMs: ").append(hmmMap.size()).append("\n");
        for(Map.Entry<String, Hmm<O>> entry : hmmMap.entrySet()) {
            builder.append("\n");
            builder.append("HMM - ").append(entry.getKey()).append("\n");
            builder.append(entry.getValue().toString());
        }
        return builder.toString();
    }


    public int getNumberOfHmms() {
        return hmmMap.size();
    }
    
}
