package cz.cuni.amis.jahmm;

import be.ac.ulg.montefiore.run.jahmm.ForwardBackwardCalculator;
import be.ac.ulg.montefiore.run.jahmm.Hmm;
import be.ac.ulg.montefiore.run.jahmm.Observation;
import java.util.Arrays;
import java.util.List;

/**
 * Makes it possible to get observation probability for any given point in time.
 * @author ik
 */
public class FBExtended extends ForwardBackwardCalculator {

    public <O extends Observation> FBExtended(List<? extends O> oseq,
            Hmm<O> hmm) {
        super(oseq, hmm);
    }


    double[] probInTimeT = null;

    public double[] probabilityForEachStep() {
        if (probInTimeT == null) {
            probInTimeT = new double[alpha.length];
            Arrays.fill(probInTimeT, 0.);
            // for each time step
            for (int t = 0; t < alpha.length; t++) {
                // sum all values in one row of the alpha array
                for (int i = 0; i < alpha[0].length; i++) {
                    probInTimeT[t] += alpha[t][i];
                }
            }
        }
        return probInTimeT;
    }
}
