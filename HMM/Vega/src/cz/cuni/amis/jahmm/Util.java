package cz.cuni.amis.jahmm;

import be.ac.ulg.montefiore.run.jahmm.Hmm;
import be.ac.ulg.montefiore.run.jahmm.Opdf;
import be.ac.ulg.montefiore.run.jahmm.OpdfInteger;

/**
 * Utility class for manipulations with JAHMM data structures.
 * @author ik
 */
public class Util {

    /**
     * Slightly changes probability of HMM state transitions. Transitions that
     * had zero probability will now have probability q. Probability distribution
     * will be flattened by equation P_i_j' = (P_i_j + q) / N * q + 1, where N is number of all states.
     * @param hmm
     * @param q
     */
    public static void flattenTransitionDistribution(Hmm hmm, double q) {
        int N = hmm.nbStates();
        final double factor = N * q + 1;
        for (int j = 0; j < N; j++) {
            for (int i = 0; i < N; i++) {
                hmm.setAij(i, j, (hmm.getAij(i, j) + q) / factor);
            }
        }
    }

        /**
     * Slightly changes probability of HMM state transitions. Transitions that
     * had zero probability will now have probability q. Probability distribution
     * will be flattened by equation P_i_j' = (P_i_j + q) / N * q + 1, where N is number of all states.
     * @param hmm
     * @param q
     */
    /*public static void flattenObservationDistribution(Hmm hmm, double q) {
        int N = hmm.nbStates();
        Opdf o;


           for (int i = 0; i < N; i++) {
        OpdfInteger opdf = (OpdfInteger) hmm.getOpdf(i);
        opdf.
            }

        final double factor = N * q + 1;
        for (int j = 0; j < N; j++) {
         }
    }
     * 
     */
}
