package main.cre.data.type.abs;

import java.util.function.Function;
import java.util.stream.Collectors;

import javafx.beans.property.SimpleIntegerProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.value.ObservableValue;

public class PubType_ColumnView {

	public static enum PubColumn {
		
		ID 	("ID", "ID", CRType_ColumnView.ColDataType.INT, pub -> new SimpleIntegerProperty(pub.getID())),
		PT 	("PT", "PT", CRType_ColumnView.ColDataType.STRING, pub -> new SimpleStringProperty(pub.getPT())),
		
		AU 	("AU", "AU", CRType_ColumnView.ColDataType.STRING, pub -> new SimpleStringProperty(pub.getAU().collect(Collectors.joining("; ")))),
		AF 	("AF", "AF", CRType_ColumnView.ColDataType.STRING, pub -> new SimpleStringProperty(pub.getAF().collect(Collectors.joining("; ")))),
		C1 	("C1", "C1", CRType_ColumnView.ColDataType.STRING, pub -> new SimpleStringProperty(String.join ("; ", pub.getC1().map(it -> "["+String.join("; ", it)+"]").collect(Collectors.toList())))),
		EM 	("EM", "EM", CRType_ColumnView.ColDataType.STRING, pub -> new SimpleStringProperty(pub.getEM().collect(Collectors.joining("; ")))),
		AA 	("AA", "AA", CRType_ColumnView.ColDataType.STRING, pub -> new SimpleStringProperty(pub.getAA().collect(Collectors.joining("; ")))),
		
		TI 	("TI", "TI", CRType_ColumnView.ColDataType.STRING, pub -> new SimpleStringProperty(pub.getTI())),
		PY 	("PY", "PY", CRType_ColumnView.ColDataType.INT, pub -> new SimpleObjectProperty<Integer>(pub.getPY())),
		SO 	("SO", "SO", CRType_ColumnView.ColDataType.STRING, pub -> new SimpleStringProperty(pub.getSO())),
		VL 	("VL", "VL", CRType_ColumnView.ColDataType.STRING, pub -> new SimpleStringProperty(pub.getTI())),
		IS 	("IS", "IS", CRType_ColumnView.ColDataType.STRING, pub -> new SimpleStringProperty(pub.getIS())),
		AR 	("AR", "AR", CRType_ColumnView.ColDataType.STRING, pub -> new SimpleStringProperty(pub.getAR())),
		BP 	("BP", "BP", CRType_ColumnView.ColDataType.INT, pub -> new SimpleObjectProperty<Integer>(pub.getBP())),
		EP 	("EP", "EP", CRType_ColumnView.ColDataType.INT, pub -> new SimpleObjectProperty<Integer>(pub.getEP())),
		PG 	("PG", "PG", CRType_ColumnView.ColDataType.INT, pub -> new SimpleObjectProperty<Integer>(pub.getPG())),
		TC 	("TC", "TC", CRType_ColumnView.ColDataType.INT, pub -> new SimpleObjectProperty<Integer>(pub.getTC())),
		
		DI 	("DI", "DI", CRType_ColumnView.ColDataType.STRING, pub -> new SimpleStringProperty(pub.getDI())),
		LI 	("LI", "LI", CRType_ColumnView.ColDataType.STRING, pub -> new SimpleStringProperty(pub.getLI())),
		AB 	("AB", "AB", CRType_ColumnView.ColDataType.STRING, pub -> new SimpleStringProperty(pub.getAB())),
		DE 	("DE", "DE", CRType_ColumnView.ColDataType.STRING, pub -> new SimpleStringProperty(pub.getDE())),
		DT 	("DT", "DT", CRType_ColumnView.ColDataType.STRING, pub -> new SimpleStringProperty(pub.getDT())),
		FS 	("FS", "FS", CRType_ColumnView.ColDataType.STRING, pub -> new SimpleStringProperty(pub.getFS())),
		UT 	("UT", "UT", CRType_ColumnView.ColDataType.STRING, pub -> new SimpleStringProperty(pub.getUT()));
	
		
		public String id;
		public String title;
		public CRType_ColumnView.ColDataType type;
		public Function<PubType<?>, ObservableValue<?>> prop;
		
		PubColumn(String id, String title, CRType_ColumnView.ColDataType type, Function<PubType<?>, ObservableValue<?>> prop) {
			this.id = id;
			this.title = title;
			this.type = type;
			this.prop = prop;
		}
		
		public String getSQLCreateTable() {
			StringBuffer sb = new StringBuffer();
			sb.append("pub_");
			sb.append (this.id);
			sb.append(" ");
			switch (this.type) {
				case INT: sb.append ("int"); break;
				case STRING: sb.append ("varchar"); break;
				default:
			}
			return sb.toString();
		}
		
	}

}
