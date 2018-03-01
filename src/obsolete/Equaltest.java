package cre.test;

import java.util.ArrayList;

import javafx.beans.property.SimpleObjectProperty;
import javafx.beans.property.SimpleStringProperty;

public class Equaltest {

	public static void main(String[] args) {
		// TODO Auto-generated method stub

		SimpleStringProperty x = new SimpleStringProperty();
//		x.set("1");
		System.out.println(x.get());
		
		SimpleStringProperty y = new SimpleStringProperty();
//		y.set("1");
		System.out.println(y.get());
		
		System.out.println(x.getValueSafe().equals(y.getValueSafe()));
		
		
		SimpleObjectProperty<Integer> a = new SimpleObjectProperty<Integer>();
//		a.setValue(1);
		
		SimpleObjectProperty<Integer> b = new SimpleObjectProperty<Integer>();
//		b.setValue(2);
		
		System.out.println(a.isEqualTo(b).get());
		
		
//		System.out.println(a.getValue().equals(b.getValue()));
		
		
		ArrayList<String> r = new ArrayList<String>();
		r.add("1");
//		r.add("2");
		System.out.println(r.toString());
		
		ArrayList<String> s = new ArrayList<String>();
		s.add("1");
		
		System.out.println(r.toString().equals(s.toString()));
		System.out.println(r.equals(s));
		
	}

}
