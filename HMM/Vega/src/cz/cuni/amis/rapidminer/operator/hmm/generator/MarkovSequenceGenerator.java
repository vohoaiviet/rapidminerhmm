package cz.cuni.amis.rapidminer.operator.hmm.generator;

import be.ac.ulg.montefiore.run.jahmm.ObservationInteger;
import be.ac.ulg.montefiore.run.jahmm.toolbox.MarkovGenerator;
import com.rapidminer.example.ExampleSet;
import com.rapidminer.example.set.SimpleExampleSet;
import com.rapidminer.example.table.AbstractDataRowReader;
import com.rapidminer.example.table.DataRow;
import com.rapidminer.example.table.DataRowReader;
import com.rapidminer.example.table.ExampleTable;
import com.rapidminer.example.table.IntArrayDataRow;
import com.rapidminer.example.table.ListDataRowReader;
import com.rapidminer.example.table.MemoryExampleTable;
import com.rapidminer.operator.OperatorDescription;
import com.rapidminer.operator.OperatorException;
import com.rapidminer.operator.io.AbstractExampleSource;
import com.rapidminer.operator.ports.InputPort;
import com.rapidminer.operator.ports.metadata.ExampleSetMetaData;
import com.rapidminer.operator.ports.metadata.ModelMetaData;
import com.rapidminer.operator.ports.metadata.SimplePrecondition;
import com.rapidminer.parameter.ParameterType;
import com.rapidminer.parameter.ParameterTypeInt;
import cz.cuni.amis.rapidminer.operator.hmm.HMMModel;
import java.util.List;

/**
 *
 * @author ik
 */
public class MarkovSequenceGenerator extends AbstractExampleSource {

    public static final String SEQ_LENGHT_KEY = "sequence_length";
    private InputPort hmmModelInput = getInputPorts().createPort("HMM model");

    public MarkovSequenceGenerator(OperatorDescription od) {
        super(od);
        hmmModelInput.addPrecondition(new SimplePrecondition(hmmModelInput, new ModelMetaData(HMMModel.class, new ExampleSetMetaData())));
    }

    @Override
    public ExampleSet createExampleSet() throws OperatorException {
        HMMModel model = hmmModelInput.getData(HMMModel.class);
        /*List<Attribute> atrs = new LinkedList<Attribute>();
        Iterator<Attribute> it = model.getTrainingHeader().getAttributes().allAttributes();
        while (it.hasNext()) {
        atrs.add(it.next());
        }

        ExampleTable exampleTable = new MemoryExampleTable(atrs);
         * */
        ExampleTable exampleTable = new MemoryExampleTable(model.getTrainingHeader().getExampleTable().getAttributes());

        ExampleSet es = new SimpleExampleSet(exampleTable);


        DataRowReader reader = new ListDataRowReader(null);
        // generate the sequence
        MarkovGenerator<ObservationInteger> mg = new MarkovGenerator<ObservationInteger>(model.getHmm());
        List<ObservationInteger> seq = mg.observationSequence(getParameterAsInt(SEQ_LENGHT_KEY));

        // TODO fill the ES with generated sequence
        return es;
    }

    @Override
    public List<ParameterType> getParameterTypes() {
        List<ParameterType> types = super.getParameterTypes();
        types.add(new ParameterTypeInt(SEQ_LENGHT_KEY, "Length of the generated sequence.", 1, Integer.MAX_VALUE, 5));
        return types;
    }
}
