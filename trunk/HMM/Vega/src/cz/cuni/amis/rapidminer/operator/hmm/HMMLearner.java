package cz.cuni.amis.rapidminer.operator.hmm;

import cz.cuni.amis.rapidminer.operator.hmm.model.HMMModel;
import be.ac.ulg.montefiore.run.jahmm.Hmm;
import be.ac.ulg.montefiore.run.jahmm.ObservationInteger;
import be.ac.ulg.montefiore.run.jahmm.OpdfInteger;
import be.ac.ulg.montefiore.run.jahmm.OpdfIntegerFactory;
import com.rapidminer.example.Attributes;
import com.rapidminer.example.Example;
import com.rapidminer.example.ExampleSet;
import com.rapidminer.example.table.NominalMapping;
import com.rapidminer.example.table.PolynominalAttribute;
import com.rapidminer.operator.Model;
import com.rapidminer.operator.OperatorCapability;
import com.rapidminer.operator.OperatorDescription;
import com.rapidminer.operator.OperatorException;
import com.rapidminer.operator.learner.PredictionModel;
import com.rapidminer.parameter.ParameterType;
import com.rapidminer.parameter.ParameterTypeBoolean;
import java.util.Arrays;
import java.util.Iterator;
import java.util.List;

/**
 * Learns a HMM model from input data, input is supposed to have one label attribute
 * and one cluster attribute. Label is used as a hidden state, cluster as an observation.
 * @author ik
 */
public class HMMLearner extends NonLabeledAbstractLearner {

    public static final String LAPLACE_MODIF_KEY = "Laplace correction";

    public HMMLearner(OperatorDescription od) {
        super(od);
    }

    /**
     * Label atr. is used as the hidden state. Cluster is the observable state.
     * @param exampleSet
     * @return
     * @throws OperatorException
     */
    public Model learn(ExampleSet exampleSet) throws OperatorException {
        // get the attribute that will be the hidden state used for learning
        PolynominalAttribute stateAtr = (PolynominalAttribute) exampleSet.getAttributes().get(Attributes.LABEL_NAME);

        boolean laplace = getParameterAsBoolean(LAPLACE_MODIF_KEY);
        // count states
        int N = stateAtr.getMapping().size();
        double[][] stateObservProbs = countStateObservationProb(exampleSet, stateAtr, laplace);
        double[][] stateTransitionProbs = countStateTransitionProb(exampleSet, stateAtr, laplace);

        Hmm<ObservationInteger> hmm = initHMM(stateObservProbs, stateTransitionProbs);

        return new HMMModel(hmm, exampleSet);
    }

    public boolean supportsCapability(OperatorCapability capability) {
        return true;
    }

    /**
     * Sets probabilities stored in arrays through the JAHMM API, it is an adaptor.
     * @param obsProbs
     * @param transProbs
     * @return
     */
    protected Hmm<ObservationInteger> initHMM(double[][] obsProbs, double[][] transProbs) {
        int N = obsProbs.length; // states

        Hmm<ObservationInteger> hmm =
                new Hmm<ObservationInteger>(N, new OpdfIntegerFactory(obsProbs[0].length));

        // state transitions
        for (int i = 0; i < N; i++) {
            for (int j = 0; j < N; j++) {
                hmm.setAij(i, j, transProbs[i][j]);
            }
        }

        // observation probability
        for (int i = 0; i < N; i++) {
            hmm.setOpdf(i, new OpdfInteger(obsProbs[i]));
        }

        return hmm;
    }

    /**
     * Counts probability of observing some state given the hidden state.
     * @param es
     * @param M number of observation states
     * @return
     * @throws Exception
     */
    protected double[][] countStateObservationProb(ExampleSet es, PolynominalAttribute stateAtr, boolean laplace) throws OperatorException {
        if (es.size() == 0) {
            throw new OperatorException("Input must be non empty.");
        }
        PolynominalAttribute observAtr = getObservationAttribute();

        int N = stateAtr.getMapping().size();
        int M = observAtr.getMapping().size();

        // i ... state
        // j ... observation
        int[][] quantities = new int[N][M];
        if(laplace) initLaplace(quantities);

        int clusterIndex = -1;
        int stateIndex = -1;
        for (int i = 0; i < es.size(); i++) {
            Example e = es.getExample(i);
            stateIndex = stateAtr.getMapping().mapString(e.getValueAsString(stateAtr));
            clusterIndex = observAtr.getMapping().mapString(e.getValueAsString(observAtr));
            quantities[stateIndex][clusterIndex] += 1;
        }

        return quantToProb(quantities);
    }

    /**
     * Translates quantity of transitions to probability.
     * @param quant
     * @return
     */
    protected double[][] quantToProb(int[][] quantities) {
        int N = quantities.length;
        int M = quantities[0].length;
        double[][] probs = new double[N][M];
        for (int i = 0; i < N; i++) {
            // sum
            int sum = 0;
            for (int j = 0; j < M; j++) {
                sum += quantities[i][j];
            }
            for (int j = 0; j < M; j++) {
                probs[i][j] = ((double) quantities[i][j]) / sum;
            }
        }
        return probs;
    }

    /**
     * Inits matrix with Laplace correction.
     * @param A
     */
    protected void initLaplace(int[][] A) {
        for(int i = 0; i < A.length; i++) {
            Arrays.fill(A[i], 1);
        }
    }

    /**
     * Counts probability of state transitions given the data.
     * @param es data used for estimation
     * @return
     * @throws Exception
     */
    protected double[][] countStateTransitionProb(ExampleSet es, PolynominalAttribute stateAtr, boolean laplace) throws OperatorException {
        if (es.size() == 0) {
            throw new OperatorException("Input must be non empty.");
        }
        // number of transitions from state i to state j
        int N = stateAtr.getMapping().size();
        int[][] quantities = new int[N][N];
        if(laplace) initLaplace(quantities);

        NominalMapping mapping = stateAtr.getMapping();

        Iterator<Example> it = es.iterator();

        int lastStateIx = mapping.getIndex(it.next().getNominalValue(stateAtr));//(int) es.getExample(0).getLabel();
        int stateIx = -1;

        while (it.hasNext()) {
            stateIx = mapping.getIndex(it.next().getNominalValue(stateAtr));//(int) es.getExample(i).getLabel();
            quantities[lastStateIx][stateIx] += 1;
            lastStateIx = stateIx;
        }

        return quantToProb(quantities);
    }

    @Override
    public Class<? extends PredictionModel> getModelClass() {
        return HMMModel.class;
    }

    @Override
    public List<ParameterType> getParameterTypes() {
        List<ParameterType> types = super.getParameterTypes();
        types.add(new ParameterTypeBoolean(LAPLACE_MODIF_KEY, "Uses Laplace correction to both state transition matrix and observation matrix. This means that when the transition and observation frequencies are counted then 1 is added to each a_i,j.", true));
        return types;
    }
}
