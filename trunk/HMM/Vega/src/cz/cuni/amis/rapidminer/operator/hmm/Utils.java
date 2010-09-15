package cz.cuni.amis.rapidminer.operator.hmm;

import be.ac.ulg.montefiore.run.jahmm.ObservationInteger;
import com.rapidminer.example.Attribute;
import com.rapidminer.example.Attributes;
import com.rapidminer.example.Example;
import com.rapidminer.example.ExampleSet;
import com.rapidminer.example.set.SplittedExampleSet;
import com.rapidminer.example.table.PolynominalAttribute;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

/**
 * Utility class, mainly for translating RapidMiner to JAHMM and vice versa.
 * @author ik
 */
public class Utils {

    /**
     * Translates RapidMiner data into JAHMM data format. All entries with the same 'batch'
     * attribute are considered to belong to the same sequence. When there is no batch attr. then
     * only single sequence is read.
     * @param exampleSet example set used as source of list of observations
     * @return list of lists of observations
     */
    static public List<? extends List<ObservationInteger>> exampleSetToObsList(ExampleSet exampleSet, Attribute observationAtr) {
        
        Attribute batchAtr = exampleSet.getAttributes().get(Attributes.BATCH_NAME);
        // map for storing sequences, each sequence is marked by the batch atr
        Map<Object, ArrayList<ObservationInteger>> seqMap = new HashMap<Object, ArrayList<ObservationInteger>>();

        List<ArrayList<ObservationInteger>> sequences = new ArrayList<ArrayList<ObservationInteger>>();
        // sequence used for examples without batch atr
        ArrayList<ObservationInteger> defaultSequence = new ArrayList<ObservationInteger>();

        Iterator<Example> it = exampleSet.iterator();
        ArrayList<ObservationInteger> actual = null;
        while (it.hasNext()) {
            Example example = it.next();
            String valStr = example.getValueAsString(observationAtr);
            ObservationInteger observ = new ObservationInteger(observationAtr.getMapping().mapString(valStr));
            if (batchAtr != null) {
                Object batchId = example.getValue(batchAtr);
                actual = seqMap.get(batchId);
                if (actual == null) {
                    // crete new list for this batchId
                    actual = new ArrayList<ObservationInteger>();
                    seqMap.put(batchId, actual);
                }
            } else {
                // batch attribute is missing, add to the default sequence
                actual = defaultSequence;
            }
            actual.add(observ);
        }

        // add sequences from map to the list of all sequences
        sequences = new ArrayList<ArrayList<ObservationInteger>>(seqMap.values());
        if (!defaultSequence.isEmpty()) {
            sequences.add(defaultSequence);
        }

        return sequences;
    }

    static PolynominalAttribute clusterAtr(ExampleSet exampleSet) {
        return (PolynominalAttribute) exampleSet.getAttributes().get(Attributes.CLUSTER_NAME);
    }

    static public SplittedExampleSet splitByBatch(ExampleSet exampleSet) {
        Attribute batchAtr = exampleSet.getAttributes().get(Attributes.BATCH_NAME);
        return SplittedExampleSet.splitByAttribute(exampleSet, batchAtr);
    }

    static public Map<String, List<List>> computeStateObservationPairs(ExampleSet exampleSet) {
        SplittedExampleSet splittedExampleSet = splitByBatch(exampleSet);
        Attribute labelAtr = exampleSet.getAttributes().get(Attributes.LABEL_NAME);
        Attribute clusterAtr = exampleSet.getAttributes().get(Attributes.CLUSTER_NAME);
        //List<LabeledSequences> sequences = new ArrayList<LabeledSequences>();
        Map<String, List<List>> examples = new HashMap<String, List<List>>();
        for (int i = 0; i < splittedExampleSet.getNumberOfSubsets(); i++) {
            splittedExampleSet.selectSingleSubset(i);
            // read sequence of observations with the same 'label' attribute
            String lastLabel = null;
            List sequence = null;
            for (Example e : splittedExampleSet) {
                String label = e.getValueAsString(labelAtr);
                if (!label.equals(lastLabel)) {
                    // new sequence begins, get the corresponding entry
                    List<List> sequencesOfLabel = examples.get(label);
                    if(sequencesOfLabel == null) {
                        sequencesOfLabel = new LinkedList<List>();
                        examples.put(label, sequencesOfLabel);
                    }
                    lastLabel = label;
                    sequence = new LinkedList();
                    sequencesOfLabel.add(sequence);
                }
                int cluster = (int) e.getValue(clusterAtr);
                sequence.add(new ObservationInteger(cluster));
            }
        }
        return examples;
    }
}
