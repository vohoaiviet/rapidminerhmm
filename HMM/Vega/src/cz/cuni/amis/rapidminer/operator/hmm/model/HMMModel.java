package cz.cuni.amis.rapidminer.operator.hmm.model;

import be.ac.ulg.montefiore.run.jahmm.Hmm;
import be.ac.ulg.montefiore.run.jahmm.ObservationInteger;
import com.rapidminer.example.Attribute;
import com.rapidminer.example.Attributes;
import com.rapidminer.example.Example;
import com.rapidminer.example.ExampleSet;
import com.rapidminer.example.table.NominalMapping;
import com.rapidminer.example.table.PolynominalAttribute;
import com.rapidminer.operator.OperatorException;
import com.rapidminer.operator.learner.PredictionModel;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

/**
 *
 * @author ik
 */
public class HMMModel extends PredictionModel {

    /** HMM used for prediction. */
    Hmm<ObservationInteger> hmm;
    /**
     * Maps state indexes to human readable names.
     */
    NominalMapping stateNameMapping = null;

    public HMMModel(Hmm<ObservationInteger> hmm, ExampleSet es) {
        super(es);
        this.hmm = hmm;
        PolynominalAttribute labelAtr = (PolynominalAttribute) es.getAttributes().get(Attributes.LABEL_NAME);
        this.stateNameMapping = labelAtr.getMapping();
    }

    public HMMModel(Hmm<ObservationInteger> hmm, ExampleSet es, NominalMapping stateNameMapping) {
        super(es);
        this.hmm = hmm;
        this.stateNameMapping = stateNameMapping;
    }

    public Hmm<ObservationInteger> getHmm() {
        return hmm;
    }
    HMMAlg currentAlgorithm = HMMAlg.VITERBI;
    protected static final String ALG_KEY = "algorithm";

    @Override
    public void setParameter(String key, Object value) throws OperatorException {
        if (key.equals(ALG_KEY)) {
            try {
                currentAlgorithm = HMMAlg.valueOf(value.toString());
            } catch (IllegalArgumentException e) {
                throw new OperatorException("Algorithm - " + value + " - not supported.", e);
            }
        }
    }

    @Override
    public ExampleSet performPrediction(ExampleSet es, Attribute predictedAtr) throws OperatorException {

        Attribute clusterAtr = es.getAttributes().getCluster();

        switch (currentAlgorithm) {
            case VITERBI:
                return viterbi(es, predictedAtr);
            default:
                throw new OperatorException("No valid algorithm set.");
        }

        //es = decorateWithPredWindow(es, clusterAssignment);

    }

    protected ExampleSet forwardBackward(ExampleSet es) {
        //TODO hmm.probability(null);
        return es;
    }

    protected ExampleSet viterbi(ExampleSet es, Attribute predictedAtr) {
        // translate the cluster values
        List<ObservationInteger> clustersSequence = new ArrayList<ObservationInteger>(es.size());
        PolynominalAttribute observAtr = (PolynominalAttribute) es.getAttributes().get(Attributes.CLUSTER_NAME);
        Iterator<Example> it = es.iterator();
        int observationIndex = -1;
        while (it.hasNext()) {
            observationIndex = observAtr.getMapping().mapString(it.next().getValueAsString(observAtr));
            clustersSequence.add(new ObservationInteger(observationIndex));
        }

        // compute the most likely sequence of hidden states
        int[] hiddenStates = hmm.mostLikelyStateSequence(clustersSequence);

        // set the sequence as predicted label in ES
        Iterator<Example> itES = es.iterator();
        int i = 0;
        while (itES.hasNext()) {
            itES.next().setValue(predictedAtr, hiddenStates[i++]);
        }
        return es;
    }

    ExampleSet decorateWithPredWindow(ExampleSet es, int[] clusterAssignment) {
        // number of observations to take into account
        return es;
        /*        int WINDOW_SIZE = 3;

        if(WINDOW_SIZE <= es.getExampleTable().size()) throw new RuntimeException("ExampleSet shorter than time window.");

        Attribute surpriseAtr = AttributeFactory.createAttribute("surprise", Ontology.REAL);
        es.getAttributes().addRegular(surpriseAtr);
        es.getExampleTable().addAttribute(surpriseAtr);

        
        List<ObservationInteger> obs = new LinkedList<ObservationInteger>();
        Iterator<Example> exampleIt = es.iterator();
        for(int i = 0; i < WINDOW_SIZE; i++) {
        exampleIt.next();
        obs.add(new ObservationInteger(clusterAssignment[i]));
        }
        // init finished

        // computation loop
        while(exampleIt.hasNext()) {
        Example e = exampleIt.next();
        // compute surprise for current time step
        int[] stateSeq = hmm.mostLikelyStateSequence(obs);
        //TODO hmm.getAij(WINDOW_SIZE, WINDOW_SIZE)stateSeq[WINDOW_SIZE-1]


        // advance to the next time step
        }
        return es;
         *
         */
    }

    @Override
    public String toString() {
        return hmm.toString();
    }

    /**
     * @param index Index of the state.
     * @return Name of the state.
     */
    public String getStateName(int index) {
        return stateNameMapping != null
                ? stateNameMapping.mapIndex(index)
                : Integer.toString(index);
    }

    public NominalMapping getStateNameMapping() {
        return stateNameMapping;
    }

    enum HMMAlg {

        VITERBI,
        FORWARD_BACKWARD,
    }
}
