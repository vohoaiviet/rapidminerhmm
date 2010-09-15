package cz.cuni.amis.rapidminer.gui.renderer.hmm;

import com.rapidminer.gui.graphs.GraphCreator;
import com.rapidminer.gui.renderer.AbstractGraphRenderer;
import com.rapidminer.operator.IOContainer;
import cz.cuni.amis.rapidminer.operator.hmm.model.HMMModel;

/**
 *
 * @author ik
 */
public class HMMGraphRenderer extends AbstractGraphRenderer {

    @Override
    public GraphCreator<String, String> getGraphCreator(Object renderable, IOContainer ioContainer) {
        HMMModel hmmModel = (HMMModel) renderable;
        return new HmmGraphCreator(hmmModel);
    }

    @Override
    public String getDefaultLayout() {
        return LAYOUT_KK_LAYOUT;
    }


}
