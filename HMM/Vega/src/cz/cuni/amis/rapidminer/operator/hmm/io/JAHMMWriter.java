package cz.cuni.amis.rapidminer.operator.hmm.io;

import be.ac.ulg.montefiore.run.jahmm.ObservationInteger;
import be.ac.ulg.montefiore.run.jahmm.Opdf;
import be.ac.ulg.montefiore.run.jahmm.OpdfInteger;
import be.ac.ulg.montefiore.run.jahmm.io.HmmWriter;
import be.ac.ulg.montefiore.run.jahmm.io.OpdfIntegerWriter;
import be.ac.ulg.montefiore.run.jahmm.io.OpdfWriter;
import com.rapidminer.operator.OperatorDescription;
import com.rapidminer.operator.OperatorException;
import com.rapidminer.operator.io.AbstractWriter;
import cz.cuni.amis.rapidminer.operator.hmm.model.HMMModel;
import java.io.FileWriter;
import java.io.IOException;
import java.io.Writer;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 *
 * @author ik
 */
public class JAHMMWriter extends AbstractWriter<HMMModel> {

    public JAHMMWriter(OperatorDescription od) {
        super(od, HMMModel.class);
    }

    @Override
    public HMMModel write(HMMModel hmm) throws OperatorException {
        Writer w = null;
        try {
            w = new FileWriter("hmm.out");
            HmmWriter.write(w, new OpdfIntegerWriter(), hmm.getHmm());
        } catch (IOException ex) {
            throw new OperatorException("Failed saving the file.", ex);
        } finally {
            try {
                w.close();
            } catch (IOException ex) {
                throw new OperatorException("Failed saving the file.", ex);
            }
        }
        return hmm;
    }
}
