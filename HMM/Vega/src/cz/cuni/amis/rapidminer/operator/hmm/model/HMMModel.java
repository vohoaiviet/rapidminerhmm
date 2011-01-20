package cz.cuni.amis.rapidminer.operator.hmm.model;

import be.ac.ulg.montefiore.run.jahmm.ForwardBackwardScaledCalculator;
import be.ac.ulg.montefiore.run.jahmm.Hmm;
import be.ac.ulg.montefiore.run.jahmm.ObservationInteger;
import com.rapidminer.example.Attribute;
import com.rapidminer.example.Attributes;
import com.rapidminer.example.Example;
import com.rapidminer.example.ExampleSet;
import com.rapidminer.example.set.RemappedExampleSet;
import com.rapidminer.example.table.NominalMapping;
import com.rapidminer.example.table.PolynominalAttribute;
import com.rapidminer.operator.OperatorException;
import com.rapidminer.operator.clustering.ClusterModel;
import com.rapidminer.operator.learner.PredictionModel;
import cz.cuni.amis.rapidminer.Util;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

/**
 *
 * @author ik
 */
public class HMMModel extends PredictionModel {

    /** 
     * Model used to preprocess examples. If it is null then it is assumed that the 
     * inputs were already clustered.
     */
    ClusterModel clusterModel = null;
    /** HMM used for prediction. */
    Hmm<ObservationInteger> hmm;
    /**
     * Maps state indexes to human readable names.
     */
    NominalMapping stateNameMapping = null;

    public HMMModel(Hmm<ObservationInteger> hmm, ExampleSet es) {
        this(hmm, null, es);
    }

    public HMMModel(Hmm<ObservationInteger> hmm, ClusterModel clusterModel, ExampleSet es) {
        super(es);
        this.hmm = hmm;
        this.clusterModel = clusterModel;
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
        // perform es clustering when clusterrer was set


        if (clusterModel != null) {
            es = clusterModel.apply(es);
            /*for (Example e : es) {
                clusterModel.
            
            }

            /*    Iterator<Example> it = es.iterator();
            while(it.hasNext()) {
            Example e = it.next();
            }*/
        }

        switch (currentAlgorithm) {
            case VITERBI:
                return viterbi(es, predictedAtr);
            case FORWARD_BACKWARD:
                return forwardBackward(es, predictedAtr);
            default:
                throw new OperatorException("No valid algorithm set.");
        }
    }

    @Override
    public ExampleSet apply(ExampleSet exampleSet) throws OperatorException {
        ExampleSet mappedExampleSet = new RemappedExampleSet(exampleSet, getTrainingHeader());
        checkCompatibility(mappedExampleSet);
		Attribute predictedLabel = createPredictionAttributes(mappedExampleSet, getLabel());
		ExampleSet result = performPrediction(mappedExampleSet, predictedLabel);

		copyPredictedLabel(result, exampleSet);
		copyCluster(result, exampleSet);
        return exampleSet;
	}

    protected ExampleSet forwardBackward(ExampleSet es, Attribute predictedAtr) {
        List<ObservationInteger> clustersSequence = observationSequence(es);
        // compute forward probabilities
        ForwardBackwardScaledCalculator calc = new ForwardBackwardScaledCalculator(clustersSequence, hmm);

        // mapping state index -> confidence attribute
        Map<Integer, Attribute> ixToAtr = new HashMap<Integer, Attribute>();
        for (int i = 0; i < hmm.nbStates(); i++) {
            String state = getStateName(i);
            Attribute confidence = es.getAttributes().getConfidence(state);
            ixToAtr.put(i, confidence);
        }

        // set the forward probs to the ES confidences
        Iterator<Example> itES = es.iterator();
        int t = 0;
        while (itES.hasNext()) {
            Example e = itES.next();
            // for each row
            double maxConfidence = Double.MIN_VALUE;
            int maxStateIndex = -1;
            for (int i = 0; i < hmm.nbStates(); i++) {
                double confidence = calc.alphaElement(t, i);
                e.setValue(ixToAtr.get(i), confidence);
                if (confidence > maxConfidence) {
                    maxConfidence = confidence;
                    maxStateIndex = i;
                }
            }
            // set best state
            e.setValue(predictedAtr, getStateName(maxStateIndex));
            t++;
        }

        return es;
    }

    protected ExampleSet viterbi(ExampleSet es, Attribute predictedAtr) {
        List<ObservationInteger> clustersSequence = observationSequence(es);

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

    protected List<ObservationInteger> observationSequence(ExampleSet es) {
        // translate the cluster values
        List<ObservationInteger> clustersSequence = new ArrayList<ObservationInteger>(es.size());
        PolynominalAttribute observAtr = (PolynominalAttribute) es.getAttributes().get(Attributes.CLUSTER_NAME);
        
        int observationIndex = -1;

        for(Example example : es) {
            observationIndex = Util.getClusterNum(example.getValueAsString(observAtr));//observAtr.getMapping().mapString(example.getValueAsString(observAtr));
            clustersSequence.add(new ObservationInteger(observationIndex));
        }
        
        return clustersSequence;
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

    private void copyCluster(ExampleSet source, ExampleSet destination) {
        Attribute clusterLabel = source.getAttributes().getCluster();
        if (clusterLabel != null) {
        	// TODO removePredictedLabel(destination, true, true);
            destination.getAttributes().setCluster(clusterLabel);
        }
    }

    enum HMMAlg {

        VITERBI,
        FORWARD_BACKWARD,
    }
}
