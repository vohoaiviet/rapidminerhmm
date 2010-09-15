package cz.cuni.amis.rapidminer.operator.hmm.generator;

import be.ac.ulg.montefiore.run.jahmm.ObservationInteger;
import be.ac.ulg.montefiore.run.jahmm.toolbox.MarkovGenerator;
import com.rapidminer.example.Attribute;
import com.rapidminer.example.Attributes;
import com.rapidminer.example.ExampleSet;
import com.rapidminer.example.table.AttributeFactory;
import com.rapidminer.example.table.DoubleArrayDataRow;
import com.rapidminer.example.table.IntArrayDataRow;
import com.rapidminer.example.table.MemoryExampleTable;
import com.rapidminer.operator.OperatorDescription;
import com.rapidminer.operator.OperatorException;
import com.rapidminer.operator.io.AbstractExampleSource;
import com.rapidminer.operator.ports.InputPort;
import com.rapidminer.operator.ports.metadata.AttributeMetaData;
import com.rapidminer.operator.ports.metadata.ExampleSetMetaData;
import com.rapidminer.operator.ports.metadata.MetaData;
import com.rapidminer.operator.ports.metadata.ModelMetaData;
import com.rapidminer.operator.ports.metadata.SimplePrecondition;
import com.rapidminer.parameter.ParameterType;
import com.rapidminer.parameter.ParameterTypeInt;
import com.rapidminer.tools.Ontology;
import cz.cuni.amis.rapidminer.operator.hmm.model.HMMModel;
import java.util.LinkedList;
import java.util.List;

/**
 * Generates sequence according to some predefined HMM.
 * @author ik
 */
public class MarkovSequenceGenerator extends AbstractExampleSource {

    public static final String SEQ_LENGHT_KEY = "sequence_length";
    public static final String SEQ_NUM_KEY = "number_of_sequences";
    private InputPort hmmModelInput = getInputPorts().createPort("HMM model");

    public MarkovSequenceGenerator(OperatorDescription od) {
        super(od);
        hmmModelInput.addPrecondition(new SimplePrecondition(hmmModelInput, new ModelMetaData(HMMModel.class, new ExampleSetMetaData())));
    }

    @Override
    public List<ParameterType> getParameterTypes() {
        List<ParameterType> types = super.getParameterTypes();
        types.add(new ParameterTypeInt(SEQ_NUM_KEY, "Number of generated sequences.", 1, Integer.MAX_VALUE, 5));
        types.add(new ParameterTypeInt(SEQ_LENGHT_KEY, "Length of the generated sequence.", 1, Integer.MAX_VALUE, 20));
        return types;
    }

    @Override
    public ExampleSet createExampleSet() throws OperatorException {
        HMMModel model = hmmModelInput.getData(HMMModel.class);

        // init
        int sequenceLenght = getParameterAsInt(SEQ_LENGHT_KEY);
        int sequencesNum = getParameterAsInt(SEQ_NUM_KEY);

        // create table
        List<Attribute> attributes = new LinkedList<Attribute>();

        // observation
        Attribute observation = AttributeFactory.createAttribute("observation", Ontology.NOMINAL);
        attributes.add(observation);
        // batch
        Attribute batch = AttributeFactory.createAttribute(Attributes.BATCH_NAME, Ontology.INTEGER);
        attributes.add(batch);


//observation.getMapping().
        MemoryExampleTable table = new MemoryExampleTable(attributes);

        // create data
        MarkovGenerator<ObservationInteger> mg = new MarkovGenerator<ObservationInteger>(model.getHmm());

        int maxObs = -1;

        // generate data and store it in the table
        for (int i = 0; i < sequencesNum; i++) {
            List<ObservationInteger> observations = mg.observationSequence(sequenceLenght);
            for (ObservationInteger obs : observations) {
                int[] row = new int[2];
                row[0] = obs.value;
                row[1] = i;
                table.addDataRow(new IntArrayDataRow(row));//new DoubleArrayDataRow(row));
                if (row[0] > maxObs) {
                    maxObs = row[0];
                }
            }
        }

        // init mapping
        for (int i = 0; i <= maxObs; i++) {
            observation.getMapping().mapString(Integer.toString(i));
        }
        // create example set and return it
        return table.createExampleSet(observation);
    }

    @Override
    public MetaData getGeneratedMetaData() throws OperatorException {
        int sequenceLenght = getParameterAsInt(SEQ_LENGHT_KEY);
        int sequencesNum = getParameterAsInt(SEQ_NUM_KEY);

        ExampleSetMetaData emd = new ExampleSetMetaData();
        emd.addAttribute(new AttributeMetaData("observation", Attributes.LABEL_NAME));
        emd.setNumberOfExamples(sequenceLenght * sequencesNum);
        return emd;
    }
}
