package org.ssi.collection;

import org.ssi.util.SearchUtil;

public class TwoWayLongArray {
	
	private static final int MIN_CAPACITY = 1 << 5;
	private static final int MAX_CAPACITY = 1 << 20;
	private static final int PADDING = 4;
	
	private long[] elements;
	
	//head points to position of first item in array
	private PaddedInt head = new PaddedInt();
	
	//tail points to next position when adding item to tail
	private PaddedInt tail = new PaddedInt();
	
	private int size;	
	
	public TwoWayLongArray() {
		this(MIN_CAPACITY);
	}
	
	public TwoWayLongArray(int intialCap) {
		
		int capacity = Math.max(Math.min(intialCap << 1, MAX_CAPACITY), MIN_CAPACITY);
		
		elements = new long[capacity];
		head.value = tail.value = capacity >> 1;
		size = 0;
	}
	
	public int head() {
		return head.value;
	}
	
	public int tail() {
		return tail.value;
	}
	
	public int size() {
		return size;
	}
	
	public int capacity() {
		return elements.length;
	}
	
	public long at(int pos){
		return elements[head.value + pos];
	}
	
	private boolean shouldIncreaseCapacity() {
		return size >= elements.length - PADDING;
	}
	
	private void increaseCapacity() {
		int newCapacity = elements.length + (elements.length >> 2); // increase 25% of capacity
		if(newCapacity > MAX_CAPACITY) {
			return;
		}
		long[] newElements = new long[newCapacity];
		
		int newHead = (newCapacity - size) >> 1;
		System.arraycopy(elements, head.value, newElements, newHead, size);
		head.value = newHead;
		tail.value = newHead + size;
		elements = newElements;
	}
	
	public void insertBeforeHead(long value) throws Exception {
		if(head.value <= 0) {
			throw new Exception("Array do not have enough space for new element at head");
		}
		
		head.value--;
		elements[head.value] = value;
		size++;
		
		if(shouldIncreaseCapacity()) {
			increaseCapacity();
		}
		
		shiftAllTowardCenter();
	}
	
	public void insertAfterTail(long value) throws Exception {
		if(tail.value >= elements.length) {
			throw new Exception("Array do not have enough space for new element at tail");
		}
		elements[tail.value] = value;
		tail.value++;
		size++;
		
		if(shouldIncreaseCapacity()) {
			increaseCapacity();
		}
		
		shiftAllTowardCenter();
	}
	
	public long peekHead() throws Exception {
		if(size == 0) {
			throw new Exception("Array have no element to peek");
		}
		return elements[head.value];
	}
	
	public long peekTail() throws Exception {
		if(size == 0) {
			throw new Exception("Array have no element to peek");
		}
		return elements[tail.value - 1];
	}
	
	public long removeHead() throws Exception {
		if(size == 0) {
			throw new Exception("Array have no element to remove");
		}
		
		long value = elements[head.value];
		head.value++;
		size--;
		
		return value;
	}	
	
	public long removeTail() throws Exception {
		if(size == 0) {
			throw new Exception("Array have no element to remove");
		}
		
		tail.value--;
		size--;
		return elements[tail.value];
	}
	
	public void insertByAscending(long value) throws Exception {
		int pos = SearchUtil.findPreElementByAscending(this, value);
		insertAt(pos, value);
	}
	
	public void insertByDescending(long value) throws Exception {
		int pos = SearchUtil.findPreElementByDescending(this, value);
		insertAt(pos, value);
	}
	
	//Insert value at position between [pos] and [pos + 1]
	public void insertAt(int pos, long value) throws Exception {
		
		if(size >= elements.length) {
			throw new Exception("Array do not have enough space to add more element");
		}

		if(pos < -1 || pos > size - 1) {
			throw new IndexOutOfBoundsException("Invalid index " + pos + " , valid index from -1 to " + (size - 1));
		}
		
		if(pos == -1) {
			insertBeforeHead(value);
			return;
		}
		
		if(pos == size - 1) {
			insertAfterTail(value);
			return;
		}
		
		int mappedIndex = head.value + pos;
			
		if(pos >= size/2) { //position is close to tail => shift shorter part toward tail
			shiftTowardTail(elements, mappedIndex + 1, tail.value - 1, 1);
			tail.value++;
			elements[mappedIndex + 1] = value;
		} else { //position is close to head => shift shorter part toward head
			shiftTowardHead(elements, head.value, mappedIndex, 1);
			head.value--;
			elements[mappedIndex] = value;
		}
		size++;
		
		if(shouldIncreaseCapacity()) {
			increaseCapacity();
		}
		shiftAllTowardCenter();	
		
	}
	
	public void removeAt(int pos) throws Exception {
		
		if(size == 0) {
			throw new Exception("Array have no element to remove");
		}
		
		if(pos < 0 || pos > size - 1) {
			throw new IndexOutOfBoundsException("Invalid index " + pos + " , valid index from 0 to " + (size - 1));
		}
		
		
		if(pos == 0) {
			removeHead();
			return;
		}
		
		if(pos == size - 1) {
			removeTail();
			return;
		}
		
		int mappedIndex = head.value+ pos;
		
		if(pos >= size/2) { //position is close to tail => shift shorter part toward head
			shiftTowardHead(elements, mappedIndex + 1, tail.value - 1, 1);
			tail.value--;
		} else { //position is close to head => shift shorter part torward tail
			shiftTowardTail(elements, head.value, mappedIndex - 1, 1);
			head.value++;
		}
		size--;
	}
	
	public void removeByAscending(long value) throws Exception {
		int index = SearchUtil.findElementByAscending(this, value);
		
		if(index >= 0) {
			removeAt(index);
		}
	}
	
	public void removeByDescending(long value) throws Exception {
		int index = SearchUtil.findElementByDescending(this, value);
		
		if(index >= 0) {
			removeAt(index);
		}
	}
	
	//Shift segment [start, end] inclusively step toward head
	private static void shiftTowardHead(long[] elements, int start, int end, int step) {
		System.arraycopy(elements, start, elements, start - step, end - start + 1);
	}
	
	//Shift segment [start, end] inclusively step toward tail
	private static void shiftTowardTail(long[] elements, int start, int end, int step) {
		System.arraycopy(elements, start, elements, start + step, end - start + 1);
	}
	
	//Shift whole array toward center
	private void shiftAllTowardCenter() {
		//there is no space to shift toward head => shift whole array toward tail
		if(head.value == 0 && tail.value + PADDING <= elements.length) {  //tail + (elements.length >> 2) <= elements.length
			int step = (elements.length - tail.value) >> 1;
			shiftTowardTail(elements, head.value, tail.value - 1, step);
			head.value += step;
			tail.value += step;
			return;
		}
		
		//there is no space to shift toward tail => shift whole array toward head
		if(tail.value == elements.length && head.value >= PADDING) { //head - (elements.length >> 2) >= 0
			int step = head.value >> 1;
			shiftTowardHead(elements, head.value, tail.value - 1, step);
			head.value -= step;
			tail.value -= step;
			return;
		}
	}
	
	public boolean equals(Object o) {
		
		if(!(o instanceof TwoWayLongArray)) {
			return false;
		}
		
		TwoWayLongArray arr2 = (TwoWayLongArray)o;
		
		if(this == arr2) {
			return true;
		}
		
		int size2 = arr2.size();		
		
		if(size != size2) {
			return false;
		}
		
		try {
			for(int i = 0; i < size; i++) {
				if(at(i) != arr2.at(i)) {
					return false;
				}
			}
		} catch (ArrayIndexOutOfBoundsException ex) {
			return false;
		}
		
		return true;
	}
}
