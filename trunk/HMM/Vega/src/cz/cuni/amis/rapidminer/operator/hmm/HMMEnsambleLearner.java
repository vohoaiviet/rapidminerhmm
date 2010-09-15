package cz.cuni.amis.rapidminer.operator.hmm;

import be.ac.ulg.montefiore.run.jahmm.OpdfIntegerFactory;

import com.rapidminer.example.Attributes;
import com.rapidminer.example.ExampleSet;
import com.rapidminer.example.Statistics;
import com.rapidminer.example.table.NominalAttribute;
import com.rapidminer.operator.Model;
import com.rapidminer.operator.OperatorCapability;
import com.rapidminer.operator.OperatorDescription;
import com.rapidminer.operator.OperatorException;
import com.rapidminer.operator.learner.AbstractLearner;
import com.rapidminer.parameter.ParameterType;
import com.rapidminer.parameter.ParameterTypeInt;
import cz.cuni.amis.jahmm.HMMEnsamble;
import cz.cuni.amis.rapidminer.operator.hmm.model.HMMEnsambleModel;

import java.util.List;
import java.util.Map;

/**
 * Learn one HMM for each target class in the input.
 * @author ik
 */
public class HMMEnsambleLearner extends AbstractLearner {

    public static final String STATES_KEY = "number_of_states";

    public HMMEnsambleLearner(OperatorDescription od) {
        super(od);
    }

    @Override
    public Model learn(ExampleSet exampleSet) throws OperatorException {
        Map<String, List<List>> examples = Utils.computeStateObservationPairs(exampleSet);
        HMMEnsamble hmmEnsamble = new HMMEnsamble();
        NominalAttribute clusterAtr = (NominalAttribute) exampleSet.getAttributes().get(Attributes.CLUSTER_NAME);
   
        int observationClassesNum = clusterAtr.getMapping().size();//(int) exampleSet.getStatistics(clusterAtr, Statistics.COUNT);
        hmmEnsamble.learn(getParameterAsInt(STATES_KEY),
                new OpdfIntegerFactory(observationClassesNum),//clusterAtr.getMapping().size()),
                examples);

        return new HMMEnsambleModel(hmmEnsamble, exampleSet);
    }

    @Override
    public boolean supportsCapability(OperatorCapability capability) {
        return true;
    }

    @Override
    public List<ParameterType> getParameterTypes() {
        List<ParameterType> types = super.getParameterTypes();
        types.add(new ParameterTypeInt(STATES_KEY, "Number of states for each discriminative HMM", 1, Integer.MAX_VALUE, 5, true));
        return types;
    }
}
