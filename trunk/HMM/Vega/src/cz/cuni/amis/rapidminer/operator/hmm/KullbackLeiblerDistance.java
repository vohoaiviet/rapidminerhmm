package cz.cuni.amis.rapidminer.operator.hmm;

import be.ac.ulg.montefiore.run.jahmm.Hmm;
import be.ac.ulg.montefiore.run.jahmm.ObservationInteger;
import be.ac.ulg.montefiore.run.jahmm.toolbox.KullbackLeiblerDistanceCalculator;
import com.rapidminer.operator.Operator;
import com.rapidminer.operator.OperatorDescription;
import com.rapidminer.operator.OperatorException;
import com.rapidminer.operator.ResultObjectAdapter;
import com.rapidminer.operator.ports.InputPort;
import com.rapidminer.operator.ports.OutputPort;
import com.rapidminer.operator.ports.metadata.ExampleSetMetaData;
import com.rapidminer.operator.ports.metadata.ModelMetaData;
import com.rapidminer.operator.ports.metadata.SimplePrecondition;
import cz.cuni.amis.rapidminer.operator.hmm.model.HMMModel;

/**
 * Measures Kullback-Leibler distance between two HMMs.
 * @author ik
 */
public class KullbackLeiblerDistance extends Operator {
    private InputPort hmmModelInput1 = getInputPorts().createPort("1. HMM model");
    private InputPort hmmModelInput2 = getInputPorts().createPort("2. HMM model");
    private OutputPort distanceOutput = getOutputPorts().createPort("distance");

    public KullbackLeiblerDistance(OperatorDescription od) {
        super(od);
        hmmModelInput1.addPrecondition(new SimplePrecondition(hmmModelInput1, new ModelMetaData(HMMModel.class, new ExampleSetMetaData())));
        hmmModelInput2.addPrecondition(new SimplePrecondition(hmmModelInput2, new ModelMetaData(HMMModel.class, new ExampleSetMetaData())));
    }

    @Override
    public void doWork() throws OperatorException {
        Hmm<ObservationInteger> hmm1 = hmmModelInput1.getData(HMMModel.class).getHmm();
        Hmm<ObservationInteger> hmm2 = hmmModelInput2.getData(HMMModel.class).getHmm();
        KullbackLeiblerDistanceCalculator klDist = new KullbackLeiblerDistanceCalculator();
        double distance = klDist.distance(hmm1, hmm2);
        distanceOutput.deliver(new KLDistanceResult(distance));
    }
}
