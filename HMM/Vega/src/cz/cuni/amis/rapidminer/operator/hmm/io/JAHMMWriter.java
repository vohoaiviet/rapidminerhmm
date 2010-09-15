package cz.cuni.amis.rapidminer.operator.hmm.io;

import com.rapidminer.operator.OperatorDescription;
import com.rapidminer.operator.OperatorException;
import com.rapidminer.operator.io.AbstractWriter;
import cz.cuni.amis.rapidminer.operator.hmm.model.HMMModel;

/**
 *
 * @author ik
 */
public class JAHMMWriter extends AbstractWriter<HMMModel> {

    public JAHMMWriter(OperatorDescription od) {
        super(od, HMMModel.class);
    }

    @Override
    public HMMModel write(HMMModel ioobject) throws OperatorException {
        throw new UnsupportedOperationException("Not supported yet.");
    }
}
