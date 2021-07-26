/*******************************************************************************
 * Copyright (c) 2021 Stan Verstappen. All Rights Reserved.
 *
 * This work is licensed under the terms of the MIT license.
 * For a copy, see <https://opensource.org/licenses/MIT>.
 ******************************************************************************/

package tools;

//Add imports

public class Queue<T> {

	private final static int RESIZE_THRESHOLD = 10;
	
	private Object[] queue; 
	private int size = 0;
	
	public Queue(){
		init();
	}
	
	private void init(){
		size = 0;
		queue = new Object[0];
	}
	
	/**
	 * Voegt een element toe aan het einde van de queue en vergroot de size met 1
	 * @param t
	 */
	public void add(T t){
		extend();
		assert queue.length == size : "Queue length does not match size:  \r\n Queue: " + queue.length + "\r\n Size: " + size;
		queue[size-1] = t;
	}
	
	
	/**
	 * Returns the first object of the queue and advances to the next object.
	 * @return The first object in the queue.
	 */
	@SuppressWarnings("unchecked")
	public T accept(){
		if(size == 0) return null;
		T t = (T)queue[0];
		drop(0);
		return t;
	}
	
	/** 
	 * Roept de drop methode aan (die verkleint de array), en geeft de nieuwe 0 object aan.
	 * Dit is dus de volgende.
	 * @return
	 */
	@SuppressWarnings("unchecked")
	public T next(){
		if(size < 2){
			init();
			return (T)null;
		}
		else{
			drop(0);
		}
		return (T)queue[0];
	}
	
	@SuppressWarnings("unchecked")
	public T current(){
		T t = (T)queue[0];
		return t;
	}
	
	@SuppressWarnings("unchecked")
	public void remove(T t){
		if(size == 0){ return; }
		for(int i = 0; i < size; i++){
			if(queue[i]!= null && ((T)queue[i]).equals(t)){
				queue[i] = null; //Clear variable;
				drop(i);
				break;
			}
		}
	}
	
	/**
	 * This function checks whether the given object is already in the queue
	 * @param t
	 * @return
	 */
	public boolean inQueue(T t){
		for(int i = 0; i < queue.length ; i++){
			if(queue[i].equals(t)){
				return true;
			}
		}
		return false;
	}
	
	public boolean contains(T t) {
		return inQueue(t);
	}
	
	/**
	 * Checks whether the queue is empty
	 * Also cleans up memory space whenever the queue actually is empty!
	 * @return
	 */
	public boolean isEmpty(){
		for(int i = 0; i < queue.length; i++){
			if(queue[i] != null) return false;
		}
		init();
		return true;
	}
	
	public int size(){
		return size;
	}
	
	private void extend(){
		if(queue.length>size) {
			size++;
			return;
		}
		assert size == queue.length : "Size: " + size + ", arraylength: " + queue.length;
		Object[] temp = queue;
		queue = new Object[++size];
		System.arraycopy(temp, 0, queue, 0, size-1);
	}
	
	/**
	 * Laat alle object 1 plaats zakken vanaf index. Index is dus een lege plek of de nulde plek anders gaat er data verloren.
	 * size wordt direct verlaagt.
	 */
	private void drop(int index){
		if(index>size||index<0) return;
		assert index==0 || queue[index] == null : "Dropping from index: " + index + ": " + queue[index];
		size--;
		for(;index<size;index++){
			queue[index] = queue[index+1];
		}
		if(queue.length-size>RESIZE_THRESHOLD){
			if(DebugTool.DEBUG_MODE)System.out.println("Resizing the Queue...");
			Object[] temp = queue;
			queue = new Object[size];
			System.arraycopy(temp, 0, queue, 0, size);
		}
	}
	
	public void print(){
		System.out.println("Size: "+ size);
		for(int i = 0; i < size; i++){
			System.out.println(i+1+": " + queue[i]);
		}
	}
	
}
