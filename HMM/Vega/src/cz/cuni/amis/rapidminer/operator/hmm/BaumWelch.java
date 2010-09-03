package cz.cuni.amis.rapidminer.operator.hmm;

import be.ac.ulg.montefiore.run.jahmm.Hmm;
import be.ac.ulg.montefiore.run.jahmm.ObservationInteger;
import be.ac.ulg.montefiore.run.jahmm.learn.BaumWelchLearner;
import com.rapidminer.example.ExampleSet;
import com.rapidminer.operator.Model;
import com.rapidminer.operator.OperatorCapability;
import com.rapidminer.operator.OperatorDescription;
import com.rapidminer.operator.OperatorException;
import com.rapidminer.operator.ports.InputPort;
import com.rapidminer.operator.ports.metadata.ExampleSetMetaData;
import com.rapidminer.operator.ports.metadata.ModelMetaData;
import com.rapidminer.operator.ports.metadata.SimplePrecondition;
import java.util.List;


/**
 * Learns a HMM using BaumWelch algorithm.
 * @author ik
 */
public class BaumWelch extends NonLabeledAbstractLearner {

    private InputPort hmmModelInput = getInputPorts().createPort("HMM model");

    public BaumWelch(OperatorDescription od) {
        super(od);
        hmmModelInput.addPrecondition(new SimplePrecondition(hmmModelInput, new ModelMetaData(HMMModel.class, new ExampleSetMetaData())));
    }

    @Override
    public Model learn(ExampleSet exampleSet) throws OperatorException {
        HMMModel hmm = hmmModelInput.getData(HMMModel.class);
        BaumWelchLearner bwl = new BaumWelchLearner();

        // translate RapidMiner data into JAHMM data format
        List<? extends List<ObservationInteger>> sequences = Utils.exampleSetToObsList(exampleSet);
        
        // perform learning using the BaumWelch alg
        Hmm<?> learntHmm = bwl.learn(hmm.getHmm(), sequences);

        return new HMMModel(learntHmm, exampleSet, hmm.getStateNameMapping());
    }

    @Override
    public boolean supportsCapability(OperatorCapability capability) {
        // TODO
        return true;
    }
}
