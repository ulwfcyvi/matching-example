package org.ssi.collection;

import org.ssi.factory.ObjectFactory;

public class ObjectPool<T> {

	private static final int MAXIMUM_CAPACITY = 1 << 31 - 10;
	
	private Object[] objectArr;
	private ObjectFactory<T> objectFactory;

	private PaddedInt count = new PaddedInt();
	
	public ObjectPool(int size, ObjectFactory<T> obFactory) {
		
		if(size <= 0) {
			throw new IllegalArgumentException("Illegal size = " + size);
		}
		
		if (size > MAXIMUM_CAPACITY) {
			size = MAXIMUM_CAPACITY;
		}
		
		objectArr = new Object[size];
		count.value = 0;
		
		objectFactory = obFactory;
	}
	
	
	public void fillPool(int n) {
		int min = Math.min(n, capacity());
		
		for(int i = 0; i < min; i++) {
			objectArr[i] = objectFactory.newInstance();
		}
		
		count.value = min;
	}
	
	public T allocate() {
		if(!hasAvaibleResource()) {
			return null;
		}
		
		@SuppressWarnings("unchecked")
		T ele = (T) objectArr[count.value - 1];
		count.value--;
		
		return ele;
	}
	
	public T allocateOrNew() {
		if(hasAvaibleResource()) {
			@SuppressWarnings("unchecked")
			T ele = (T) objectArr[count.value - 1];
			count.value--;
			
			return ele;
		}
		
		return objectFactory.newInstance();
	}
	
	public void returnPool(T order) {
		if(count.value >= objectArr.length) {
			increaseSize();
		}
		
		objectArr[count.value] = order;
		count.value++;
	}
	
	public boolean hasAvaibleResource() {
		return count.value > 0;
	}

	public int capacity() {
		return objectArr.length;
	}
	
	public int size() {
		return count.value;
	}
	
	private void increaseSize() {
		int newCapacity = objectArr.length +  (objectArr.length >> 1);
		Object[] newObjArr = new Object[newCapacity];
		
		System.arraycopy(objectArr, 0, newObjArr, 0, count.value);
		objectArr = newObjArr;
	}
}
