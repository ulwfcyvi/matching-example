package org.ssi.collection;

import org.ssi.factory.ObjectFactory;


public class ConcurrentObjectPool<T> {
	
	private static final int MAXIMUM_CAPACITY = 1 << 31 - 10;
	
	private Object[] objectArr;
	private ObjectFactory<T> objectFactory;
	
	//cursor points to position of last item in pool
	private PaddedAtomicInt count = new PaddedAtomicInt(0);
	
	public ConcurrentObjectPool(int size, ObjectFactory<T> obFactory) {
		
		if (size > MAXIMUM_CAPACITY) {
			size = MAXIMUM_CAPACITY;
		}
		
		objectArr = new Object[size];
		objectFactory = obFactory;
	}
	
	
	public void fillPool(int n) {
		int min = Math.min(n, capacity());
		
		for(int i = 0; i < min; i++) {
			objectArr[i] = objectFactory.newInstance();
		}
		
		count.lazySet(min);
	}
	
	public T allocate() {
		if(!hasAvaibleResource()) {
			return null;
		}
		
		@SuppressWarnings("unchecked")
		int size = count.get()-1;
		T ele = (T) objectArr[size];
		count.lazySet(size);
		
		return ele;
	}
	
	public  T allocateOrNew() {
		if(hasAvaibleResource()) {
			int size = count.get();
			@SuppressWarnings("unchecked")
			T ele = (T) objectArr[size - 1];
			count.lazySet(size-1);
			
			return ele;
		}
		
		return objectFactory.newInstance();
	}
	
	public  void returnPool(T order) {
		int size = count.get();
		if(count.get() >= objectArr.length) {
			increaseSize();
		}
		
		objectArr[size] = order;
		count.lazySet(size + 1);
	}
	
	public boolean hasAvaibleResource() {
		return count.get() > 0;
	}
	
	public int capacity() {
		return objectArr.length;
	}
	
	public int size() {
		return count.get();
	}
	
	private void increaseSize() {
		int newCapacity = objectArr.length +  (objectArr.length >> 1);
		Object[] newObjArr = new Object[newCapacity];
		
		System.arraycopy(objectArr, 0, newObjArr, 0, count.get());
		objectArr = newObjArr;
	}


}
