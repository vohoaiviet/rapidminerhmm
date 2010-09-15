package cz.cuni.amis.rapidminer.operator.hmm;

import cz.cuni.amis.rapidminer.operator.hmm.model.HMMModel;
import com.rapidminer.operator.Operator;
import com.rapidminer.operator.OperatorDescription;
import com.rapidminer.operator.OperatorException;
import com.rapidminer.operator.ports.InputPort;
import com.rapidminer.operator.ports.OutputPort;
import com.rapidminer.operator.ports.metadata.GenerateModelTransformationRule;
import com.rapidminer.parameter.ParameterType;
import com.rapidminer.parameter.ParameterTypeDouble;
import com.rapidminer.parameter.UndefinedParameterError;
import java.util.List;

/**
 * Modifies probability of state transition or observation of previously learned HMM>
 * @author ik
 */
public class HMMDistributionModifier extends Operator {

    public static final String STATE_TRANS_KEY = "state_transition_modifier";
    private final OutputPort modelOutput = getOutputPorts().createPort("model");
    private final InputPort modelInput = getInputPorts().createPort("model");

    public HMMDistributionModifier(OperatorDescription od) {
        super(od);
        getTransformer().addRule(new GenerateModelTransformationRule(modelInput, modelOutput, HMMModel.class));
    }

    @Override
    public void doWork() throws OperatorException {
        HMMModel hmm = modelInput.getData(HMMModel.class);

        double q = getParameterAsDouble(STATE_TRANS_KEY);
        cz.cuni.amis.jahmm.Util.flattenTransitionDistribution(hmm.getHmm(), q);
        
        modelOutput.deliver(hmm);
    }

    @Override
    public List<ParameterType> getParameterTypes() {
        List<ParameterType> types = super.getParameterTypes();
        types.add(new ParameterTypeDouble(STATE_TRANS_KEY, "Modifies probability of transition between two arbitrary states. Small initial probability means that the transition is unprobable but POSSIBLE. New probability of transition will be P_i_j' = (P_i_j + q) / N * q + 1, where 'N' is number of states and 'q' is value of this parameter.", 0, 1, 0.01, true));
        return types;
    }
}
