package cz.cuni.amis.rapidminer.operator.hmm;

import com.rapidminer.example.AttributeWeights;
import com.rapidminer.example.ExampleSet;
import com.rapidminer.example.table.PolynominalAttribute;
import com.rapidminer.operator.Model;
import com.rapidminer.operator.Operator;
import com.rapidminer.operator.OperatorDescription;
import com.rapidminer.operator.OperatorException;
import com.rapidminer.operator.ProcessSetupError.Severity;
import com.rapidminer.operator.UserError;
import com.rapidminer.operator.learner.CapabilityCheck;
import com.rapidminer.operator.learner.Learner;
import com.rapidminer.operator.learner.PredictionModel;
import com.rapidminer.operator.performance.PerformanceVector;
import com.rapidminer.operator.ports.InputPort;
import com.rapidminer.operator.ports.OutputPort;
import com.rapidminer.operator.ports.metadata.GenerateNewMDRule;
import com.rapidminer.operator.ports.metadata.GeneratePredictionModelTransformationRule;
import com.rapidminer.operator.ports.metadata.LearnerPrecondition;
import com.rapidminer.operator.ports.metadata.MetaData;
import com.rapidminer.operator.ports.metadata.MetaDataError;
import com.rapidminer.operator.ports.metadata.PassThroughRule;
import com.rapidminer.operator.ports.metadata.SimpleMetaDataError;
import com.rapidminer.parameter.ParameterType;
import com.rapidminer.parameter.ParameterTypeAttribute;
import com.rapidminer.parameter.ParameterTypeStringCategory;
import com.rapidminer.tools.Tools;
import java.util.List;

/**
 * Slightly modified version of original RM AbstractLearner. This one omits checking
 * presence of Label attribute.
 * 
 * @author ik
 */
public abstract class NonLabeledAbstractLearner extends Operator implements Learner {

    private final InputPort exampleSetInput = getInputPorts().createPort("training set");
    private final OutputPort modelOutput = getOutputPorts().createPort("model");
    private final OutputPort performanceOutput = getOutputPorts().createPort("estimated performance", canEstimatePerformance());
    private final OutputPort weightsOutput = getOutputPorts().createPort("weights", canCalculateWeights());
    private final OutputPort exampleSetOutput = getOutputPorts().createPort("exampleSet");

    /** Creates a new abstract  */
    public NonLabeledAbstractLearner(OperatorDescription description) {
        super(description);
        exampleSetInput.addPrecondition(new LearnerPrecondition(this, exampleSetInput));
        getTransformer().addRule(new GeneratePredictionModelTransformationRule(exampleSetInput, modelOutput, getModelClass()));
        getTransformer().addRule(new GenerateNewMDRule(performanceOutput, new MetaData(PerformanceVector.class)) {

            @Override
            public MetaData modifyMetaData(MetaData unmodifiedMetaData) {
                if (canEstimatePerformance()) {
                    return unmodifiedMetaData;
                } else {
                    return null;
                }
            }
        });
        getTransformer().addRule(new GenerateNewMDRule(weightsOutput, new MetaData(AttributeWeights.class)) {

            @Override
            public MetaData modifyMetaData(MetaData unmodifiedMetaData) {
                if (canCalculateWeights()) {
                    return unmodifiedMetaData;
                } else if (weightsOutput.isConnected()) {
                    weightsOutput.addError(getWeightCalculationError(weightsOutput));
                }
                return null;
            }
        });
        getTransformer().addRule(new PassThroughRule(exampleSetInput, exampleSetOutput, false));
    }

    @Override
    public boolean shouldAutoConnect(OutputPort outputPort) {
        if (outputPort == performanceOutput) {
            return shouldEstimatePerformance();
        } else if (outputPort == weightsOutput) {
            return shouldCalculateWeights();
        } else if (outputPort == exampleSetOutput) {
            return getParameterAsBoolean("keep_example_set");
        } else {
            return super.shouldAutoConnect(outputPort);
        }
    }

    /** Helper method in case this operator is constructed anonymously.
     *  Assigns the example set to the input port and returns the model. */
    public Model doWork(ExampleSet exampleSet) throws OperatorException {
        exampleSetInput.receive(exampleSet);
        doWork();
        return modelOutput.getData();
    }

    /** Returns the weights (if computed, after one of the doWork()} methods has been called.
     * @throws OperatorException */
    public AttributeWeights getWeights() throws OperatorException {
        return weightsOutput.getData();
    }

    /**
     * This method might be overridden from subclasses in order to specify exactly
     * which model class they use. This is to ensure the proper postprocessing of some models like
     * KernelModels (SupportVectorCounter) or TreeModels (Rule generation)
     */
    public Class<? extends PredictionModel> getModelClass() {
        return PredictionModel.class;
    }

    /**
     * Trains a model using an ExampleSet from the input.
     * Uses the method learn(ExampleSet).
     */
    @Override
    public void doWork() throws OperatorException {
        ExampleSet exampleSet = exampleSetInput.getData();

        // some checks
        if (exampleSet.getAttributes().size() == 0) {
            throw new UserError(this, 106);
        }
        if (exampleSet.size() == 0) {
            throw new UserError(this, 117);
        }

        // check capabilities and produce errors if they are not fulfilled
        CapabilityCheck check = new CapabilityCheck(this, Tools.booleanValue(System.getProperty(PROPERTY_RAPIDMINER_GENERAL_CAPABILITIES_WARN), true) || onlyWarnForNonSufficientCapabilities());
        check.checkLearnerCapabilities(this, exampleSet);

        Model model = learn(exampleSet);
        modelOutput.deliver(model);

        // weights must be calculated _after_ learning
        // we are still asking for shouldCalcluate weights since, e.g., SVMWeighting needs an anonymous
        // learner whose weightOutputs is not connected, so only checking for weightsOutput.isConnected()
        // is not sufficient.
        if (canCalculateWeights() && (weightsOutput.isConnected())) { // || shouldCalculateWeights()) {
            AttributeWeights weights = getWeights(exampleSet);
            if (weights != null) {
                weightsOutput.deliver(weights);
            }
        }

        if (canEstimatePerformance() && (performanceOutput.isConnected())) {
            PerformanceVector perfVector = null;
            if (shouldDeliverOptimizationPerformance()) {
                perfVector = getOptimizationPerformance();
            } else {
                perfVector = getEstimatedPerformance();
            }
            performanceOutput.deliver(perfVector);
        }

        exampleSetOutput.deliver(exampleSet);
    }

    /**
     * Returns true if the user wants to estimate the performance (depending on
     * a parameter). In this case the method getEstimatedPerformance() must also
     * be overridden and deliver the estimated performance. The default
     * implementation returns false.
     * @deprecated This method is not used any longer. Performance is estimated iff
     *   {@link #canEstimatePerformance()} returns true and the corresponding port is connected.
     */
    @Deprecated
    @Override
    public boolean shouldEstimatePerformance() {
        return false;
    }

    /** Returns true if this learner is capable of estimating its performance.
     *  If this returns true, a port will be created and {@link #getEstimatedPerformance()}
     *  will be called if this port is connected. */
    public boolean canEstimatePerformance() {
        return false;
    }

    /**
     * Returns true if the user wants to calculate feature weights (depending on
     * a parameter). In this case the method getWeights() must also be overriden
     * and deliver the calculated weights. The default implementation returns
     * false.
     * @deprecated This method is not used any longer. Weights are computed iff
     *   {@link #canCalculateWeights()} returns true and the corresponding port is connected.
     */
    @Deprecated
    @Override
    public boolean shouldCalculateWeights() {
        return false;
    }

    /** Returns true if this learner is capable of computing attribute weights.
     *  If this method returns true, also override {@link #getWeights(ExampleSet)} */
    public boolean canCalculateWeights() {
        return false;
    }

    public MetaDataError getWeightCalculationError(OutputPort weightPort) {
        return new SimpleMetaDataError(Severity.ERROR, weightPort, "parameters.incompatible_for_delivering", "AttributeWeights");
    }

    /**
     * Returns true if the user wants to deliver the performance of the original optimization
     * problem. Since many learners are basically optimization procedures for a certain type
     * of objective function the result of this procedure might also be of interest in some cases.
     */
    public boolean shouldDeliverOptimizationPerformance() {
        return false;
    }

    /**
     * Returns the estimated performance. Subclasses which supports the
     * capability to estimate the learning performance must override this
     * method. The default implementation throws an exception.
     */
    @Override
    public PerformanceVector getEstimatedPerformance() throws OperatorException {
        throw new UserError(this, 912, getName(), "estimation of performance not supported.");
    }

    /**
     * Returns the resulting performance of the original optimization problem.
     * Subclasses which supports the capability to deliver this performance
     * must override this method. The default implementation throws an exception.
     */
    public PerformanceVector getOptimizationPerformance() throws OperatorException {
        throw new UserError(this, 912, getName(), "delivering the original optimization performance is not supported.");
    }

    /**
     * Returns the calculated weight vectors. Subclasses which supports the
     * capability to calculate feature weights must override this method. The
     * default implementation throws an exception.
     */
    @Override
    public AttributeWeights getWeights(ExampleSet exampleSet) throws OperatorException {
        throw new UserError(this, 916, getName(), "calculation of weights not supported.");
    }

    public boolean onlyWarnForNonSufficientCapabilities() {
        return false;
    }

    public InputPort getExampleSetInputPort() {
        return this.exampleSetInput;
    }
    public static final String OBSERVATION_ATR_NAME = "observation_attribute";

    @Override
    public List<ParameterType> getParameterTypes() {
        List<ParameterType> types = super.getParameterTypes();
        types.add(new ParameterTypeAttribute(OBSERVATION_ATR_NAME, "The name of the attribute whose role should be changed.", getExampleSetInputPort(), false, false));
        return types;
    }

    protected PolynominalAttribute getObservationAttribute() throws UserError {
        String obsAtrName = getParameterAsString(OBSERVATION_ATR_NAME);
        return (PolynominalAttribute) exampleSetInput.getData(ExampleSet.class).getAttributes().get(obsAtrName);
    }
}
