package main.cre.data.type.db;

import java.util.stream.Stream;

import javafx.beans.property.SimpleIntegerProperty;
import main.cre.data.type.abs.CRCluster;
import main.cre.data.type.abs.CRType;

public class CRCluster_DB extends CRCluster {

	private int size;
	
	public CRCluster_DB(int c1) {
		this.c1 = c1;
		this.c2 = this.c1;
		this.size = 1;
	}

	
	public int getSize() {
		return this.size;
	}
	
	public CRCluster_DB(int c1, int c2, int size) {
		this.c1 = c1;
		this.c2 = c2;
		this.size = size;
	}
	
	public CRCluster_DB (String s) {
		String[] split = s.split ("/");
		this.c1 = Integer.valueOf(split[0]).intValue();
		this.c2 = Integer.valueOf(split[1]).intValue();
	}

	@Override
	public void merge (CRCluster crc) {
		// TODO Auto-generated method stub
			
	}
	
	@Override
	public Stream<CRType> getCR() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public CRType getMainCR() {
		// TODO Auto-generated method stub
		return null;
	}

}
