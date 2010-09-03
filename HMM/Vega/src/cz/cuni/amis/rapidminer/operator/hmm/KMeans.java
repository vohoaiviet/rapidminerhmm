package cz.cuni.amis.rapidminer.operator.hmm;

import be.ac.ulg.montefiore.run.jahmm.ObservationInteger;
import be.ac.ulg.montefiore.run.jahmm.OpdfIntegerFactory;
import be.ac.ulg.montefiore.run.jahmm.learn.KMeansLearner;
import com.rapidminer.example.ExampleSet;
import com.rapidminer.operator.Model;
import com.rapidminer.operator.OperatorCapability;
import com.rapidminer.operator.OperatorDescription;
import com.rapidminer.operator.OperatorException;
import com.rapidminer.parameter.ParameterType;
import com.rapidminer.parameter.ParameterTypeInt;
import java.util.List;

/**
 * K-Means HMM learner.
 * @author ik
 */
public class KMeans extends NonLabeledAbstractLearner {

    protected static final String NB_STATES_KEY = "nb_states";

    public KMeans(OperatorDescription od) {
        super(od);
    }

    public Model learn(ExampleSet exampleSet) throws OperatorException {
        List<? extends List<ObservationInteger>> sequences = Utils.exampleSetToObsList(exampleSet);
        int M = Utils.clusterAtr(exampleSet).getMapping().size();
        int nbStates = getParameterAsInt(NB_STATES_KEY);
        KMeansLearner kMeansLearner = new KMeansLearner(nbStates, new OpdfIntegerFactory(M), sequences);
        return new HMMModel(kMeansLearner.learn(), exampleSet);
    }

    @Override
    public List<ParameterType> getParameterTypes() {
        List<ParameterType> types = super.getParameterTypes();
        types.add(new ParameterTypeInt(NB_STATES_KEY, "Number of states of the learned HMM.", 1, Integer.MAX_VALUE, 5));
        return types;
    }

    public boolean supportsCapability(OperatorCapability capability) {
        return true;
    }


}
