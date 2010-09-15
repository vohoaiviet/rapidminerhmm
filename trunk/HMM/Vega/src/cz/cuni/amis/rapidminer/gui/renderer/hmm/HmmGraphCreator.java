package cz.cuni.amis.rapidminer.gui.renderer.hmm;

import com.rapidminer.gui.graphs.GraphCreatorAdaptor;
import cz.cuni.amis.rapidminer.operator.hmm.model.HMMModel;
import edu.uci.ics.jung.graph.DirectedSparseGraph;
import edu.uci.ics.jung.graph.Graph;
import java.text.DecimalFormat;

/**
 *
 * @author ik
 */
public class HmmGraphCreator extends GraphCreatorAdaptor {

    protected HMMModel hmm;

    public HmmGraphCreator(HMMModel hmm) {
        this.hmm = hmm;
    }

    public Graph<String, String> createGraph() {
        Graph<String, String> graph = new DirectedSparseGraph<String, String>();

        for (int i = 0; i < hmm.getHmm().nbStates(); i++) {
            graph.addVertex(Integer.toString(i));
        }

        for (int i = 0; i < hmm.getHmm().nbStates(); i++) {
            for (int j = 0; j < hmm.getHmm().nbStates(); j++) {
                graph.addEdge(i + "->" + j, Integer.toString(i), Integer.toString(j));
            }
        }
        return graph;
    }

    @Override
    public String getEdgeName(String id) {
        return "";
    }

    @Override
    public String getVertexName(String id) {
        return hmm.getStateName(Integer.parseInt(id));
    }

    @Override
    public double getEdgeStrength(String id) {
        String[] indices = id.split("->");
        int i = Integer.parseInt(indices[0]);
        int j = Integer.parseInt(indices[1]);
        return hmm.getHmm().getAij(i, j);
    }

    @Override
    public int getEdgeShape() {
        return EDGE_SHAPE_QUAD_CURVE;
    }

    DecimalFormat df = new DecimalFormat("#.##");

    @Override
    public String getVertexToolTip(String id) {
        int i = Integer.parseInt(id);
        // get probs of state transitions
        StringBuilder builder = new StringBuilder();
        for (int j =0; j < hmm.getHmm().nbStates(); j++) {
            builder.append(hmm.getStateName(j))
                    .append(":")
                    .append(df.format(hmm.getHmm().getAij(i, j)))
                    .append(", ");
        }
        
        String obsProb = hmm.getHmm().getOpdf(i).toString();
        return builder.toString() + "\n" + obsProb;
    }




/*
    	@Override
	public EdgeLabel<String, String> getEdgeLabelRenderer() {
		return new TreeModelEdgeLabelRenderer<String,String>();
	}

    @Override
	public VertexLabel<String, String> getVertexLabelRenderer() {
        return new TreeModelNodeLabelRenderer<String, String>(this);
    }
 */
}
