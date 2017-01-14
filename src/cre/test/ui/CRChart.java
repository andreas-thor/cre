package cre.test.ui;

import javafx.scene.Node;

public interface CRChart {

	public Node getNode ();
	
	public void setVisible (boolean value);
	
	public void setDomainRange (Integer min, Integer max);
	
	public void updateData (int[][] data);
	
}
