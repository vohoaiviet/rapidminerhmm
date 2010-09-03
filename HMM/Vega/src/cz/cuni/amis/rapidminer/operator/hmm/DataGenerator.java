package cz.cuni.amis.rapidminer.operator.hmm;

import com.rapidminer.operator.Operator;
import com.rapidminer.operator.OperatorDescription;
import com.rapidminer.operator.OperatorException;
import com.rapidminer.operator.ports.InputPort;
import com.rapidminer.operator.ports.OutputPort;
import com.rapidminer.operator.ports.metadata.ExampleSetMetaData;
import com.rapidminer.operator.ports.metadata.ModelMetaData;
import com.rapidminer.operator.ports.metadata.SimplePrecondition;
import com.rapidminer.parameter.ParameterType;
import com.rapidminer.parameter.ParameterTypeBoolean;
import com.rapidminer.parameter.ParameterTypeDouble;
import com.rapidminer.parameter.ParameterTypeInt;
import com.rapidminer.parameter.ParameterTypeList;
import java.util.List;

/**
 *
 * @author ik
 */
public class DataGenerator extends Operator {

    private InputPort hmmModelInput = getInputPorts().createPort("hmm model");
    private final OutputPort outputPort = getOutputPorts().createPort("output");

    public DataGenerator(OperatorDescription description) {
        super(description);
        hmmModelInput.addPrecondition(new SimplePrecondition(hmmModelInput, new ModelMetaData(HMMModel.class, new ExampleSetMetaData())));
    }

    @Override
	public List<ParameterType> getParameterTypes() {
		List<ParameterType> types = super.getParameterTypes();
		types.add(new ParameterTypeInt("number_of_values", "Defines the number of values which should be used for the sinus signal.", 1,
				Integer.MAX_VALUE, 8000));
		types.add(new ParameterTypeDouble("error", "Defines the variance of randomly added gaussian error.", 0.0d, Double.POSITIVE_INFINITY, 0.0d));
		types.add(new ParameterTypeBoolean("incremental_disp",
				"Indicates if the displacement values are between 0 and 1 (false) or incremental numbered (true).", false));
		types.add(new ParameterTypeList("frequency", "A list of sinus waveforms for superpositioning",
				new ParameterTypeDouble("frequency", "The frequency of the sinus curve", 0.0d, Double.POSITIVE_INFINITY),
				new ParameterTypeDouble("amplitude", "The amplitude of the waveform.", 0.0d, Double.POSITIVE_INFINITY)));
		return types;
	}

    @Override
    public void doWork() throws OperatorException {
        // TODO
        throw new OperatorException("not supported");
       /* Attribute atrs[] = new Attribute[]{new NumericalAttribute
        ExampleTable exampleTable = new MemoryExampleTable(, null, size)
        ExampleSet es = new SimpleExampleSet
        outputPort.deliver(null);*/
    }


}
