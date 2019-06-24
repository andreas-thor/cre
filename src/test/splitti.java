package test;

import java.util.Spliterator;
import java.util.function.Consumer;

public class splitti implements Spliterator<String> {

	@Override
	public boolean tryAdvance(Consumer<? super String> action) {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public Spliterator<String> trySplit() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public long estimateSize() {
		// TODO Auto-generated method stub
		return 0;
	}

	@Override
	public int characteristics() {
		// TODO Auto-generated method stub
		return 0;
	}

}
