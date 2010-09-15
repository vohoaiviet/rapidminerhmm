package cz.cuni.amis.rapidminer.operator.learner.meta;

import com.rapidminer.example.Attribute;
import com.rapidminer.example.Attributes;
import com.rapidminer.example.ExampleSet;
import com.rapidminer.example.table.AttributeFactory;
import com.rapidminer.operator.Operator;
import com.rapidminer.operator.OperatorDescription;
import com.rapidminer.operator.OperatorException;
import com.rapidminer.operator.UserError;
import com.rapidminer.operator.learner.PredictionModel;
import com.rapidminer.operator.ports.InputPort;
import com.rapidminer.operator.ports.InputPortExtender;
import com.rapidminer.operator.ports.OutputPort;
import com.rapidminer.operator.ports.metadata.ExampleSetMetaData;
import com.rapidminer.operator.ports.metadata.PredictionModelMetaData;
import com.rapidminer.operator.ports.metadata.SimplePrecondition;
import com.rapidminer.parameter.ParameterType;
import com.rapidminer.parameter.ParameterTypeInt;
import com.rapidminer.tools.Ontology;
import java.util.List;

/**
 *
 * @author ik
 */
public class LayeredModelLearner extends Operator {
//Ports<InputPort> ports = new InputPortsImpl(null)

    static final String INPUT_ATTR = Attributes.LABEL_NAME;
    protected InputPortExtender classificationModelsExtender = new InputPortExtender("base model", getInputPorts(), new PredictionModelMetaData(PredictionModel.class, new ExampleSetMetaData()), 2);

    /* public LayeredModelLearner(OperatorDescription description) {//, String... subprocessNames) {
    super(description);//, "L1", "L2");
    classificationModelsExtender.start();
    }
     */
    private final InputPort exampleSetInput = getInputPorts().createPort("unlabelled data");
    private final OutputPort exampleSetOutput = getOutputPorts().createPort("labelled data");
    //private final OutputPort modelOutput = getOutputPorts().createPort("model");

    public LayeredModelLearner(OperatorDescription description) {
        super(description);
        classificationModelsExtender.start();
        exampleSetInput.addPrecondition(new SimplePrecondition(exampleSetInput, new ExampleSetMetaData()));
        //getTransformer().addRule(new ModelApplicationRule(exampleSetInput, exampleSetOutput, modelInput, false));
        //getTransformer().addRule(new PassThroughRule(modelInput, modelOutput, false));
    }

    /**
     * Applies the operator and labels the {@link ExampleSet}. The example set
     * in the input is not consumed.
     */
    @Override
    public void doWork() throws OperatorException {
        ExampleSet inputExampleSet = exampleSetInput.getData();
        //Model model = modelInput.getData();

        List<PredictionModel> models = classificationModelsExtender.getData(false);
        int i = 0;
        for (PredictionModel model : models) {
            Attribute predictedAttr = PredictionModel.createPredictedLabel(inputExampleSet, AttributeFactory.createAttribute("Layer " + i, Ontology.POLYNOMINAL));
            model.performPrediction(inputExampleSet, predictedAttr);

            // make the predicted label input to the next iteration
            changeAttrRole(inputExampleSet, predictedAttr, INPUT_ATTR);
            i++;
        }
        /*
        log("Set parameters for " + model.getClass().getName());
        List<String[]> modelParameters = getParameterList(PARAMETER_APPLICATION_PARAMETERS);
        Iterator<String[]> i = modelParameters.iterator();
        while (i.hasNext()) {
        String[] parameter = i.next();
        model.setParameter(parameter[0], parameter[1]);
        }

        // handling PreprocessingModels: extra treatment for views
        if (getParameterAsBoolean(PARAMETER_CREATE_VIEW)) {
        model.setParameter(PreprocessingOperator.PARAMETER_CREATE_VIEW, true);
        }
         */
       
        exampleSetOutput.deliver(inputExampleSet);

    }

    /*
    @Override
    public Process getExperiment() {
    return super.getExperiment();
    }

    @Override
    public ExecutionUnit getSubprocess(int index) {
    return super.getSubprocess(index);
    }


    @Override
    public boolean areSubprocessesExtendable() {
    return true;
    }
     */
    @Override
    public List<ParameterType> getParameterTypes() {
        List<ParameterType> types = super.getParameterTypes();
        types.add(new ParameterTypeInt("A", "Number of states for each discriminative HMM", 1, Integer.MAX_VALUE, 5, true));


        return types;

    }

    protected void changeAttrRole(ExampleSet exampleSet, Attribute attribute, String targetRole) throws UserError {
        exampleSet.getAttributes().remove(attribute);
        if ((targetRole == null) || (targetRole.trim().length() == 0)) {
            throw new UserError(this, 205);
        }
        if (targetRole.equals("regular")) {
            exampleSet.getAttributes().addRegular(attribute);
        } else {
            exampleSet.getAttributes().setSpecialAttribute(attribute, targetRole);
        }
    }
}
