/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package cz.cuni.amis.rapidminer.operator.hmm.model;

import be.ac.ulg.montefiore.run.jahmm.ObservationInteger;
import com.rapidminer.example.Attribute;
import com.rapidminer.example.Example;
import com.rapidminer.example.ExampleSet;
import com.rapidminer.example.table.AttributeFactory;
import com.rapidminer.example.table.PolynominalAttribute;
import com.rapidminer.operator.OperatorException;
import com.rapidminer.operator.learner.PredictionModel;
import com.rapidminer.tools.Ontology;
import cz.cuni.amis.jahmm.HMMEnsamble;
import cz.cuni.amis.rapidminer.operator.hmm.Utils;
import java.util.Iterator;
import java.util.List;

/**
 *
 * @author ik
 */
public class HMMEnsambleModel extends PredictionModel {

    HMMEnsamble hmmEnsamble = null;

    public HMMEnsambleModel(HMMEnsamble hmmEnsamble, ExampleSet es) {
        super(es);
        this.hmmEnsamble = hmmEnsamble;
    }

    @Override
    public ExampleSet performPrediction(ExampleSet exampleSet, Attribute predictedLabel) throws OperatorException {
        PolynominalAttribute observationAtr = (PolynominalAttribute) exampleSet.getAttributes().getCluster();
        List<? extends List<ObservationInteger>> lists = Utils.exampleSetToObsList(exampleSet, observationAtr);
        for (List list : lists) {

            HMMEnsamble.EnsambleClassificationResult result = hmmEnsamble.classify(list);

            // add atributes to schema
            Attribute[] extraAttrs = new Attribute[hmmEnsamble.getNumberOfHmms()];
            for (int i = 0; i < hmmEnsamble.getNumberOfHmms(); i++) {
                Attribute singleProbAtr = AttributeFactory.createAttribute("prob(" + result.getClasses().get(i) + ")", Ontology.REAL);
                exampleSet.getAttributes().addRegular(singleProbAtr);
                exampleSet.getExampleTable().addAttribute(singleProbAtr);
                extraAttrs[i] = singleProbAtr;
            }

            // fill computed values into exampleSet
            int t = 0;
            for (Example example : exampleSet) {
                // add predicted class
                example.setValue(predictedLabel, result.getPredictedClass(t));

                // add attributes with prediction probability for each HMM
                // TODO remove
                for (int i = 0; i < hmmEnsamble.getNumberOfHmms(); i++) {
                    example.setValue(extraAttrs[i], result.getProbTab()[i][t]);
                    example.setConfidence((String) result.getClasses().get(i), result.getProbTab()[i][t]);
                }

                t++;
            }
        }
        return exampleSet;
    }

    @Override
    public String toString() {
        return hmmEnsamble.toString();
    }
}
