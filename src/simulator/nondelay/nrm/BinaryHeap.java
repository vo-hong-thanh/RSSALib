package simulator.nondelay.nrm;

import java.util.Hashtable;

/**
 * BinaryHeap: Binary heap for NRM
 * @author Vo Hong Thanh
 * @version 1.0
*/
public class BinaryHeap {
    //heap data
    private NRMNode[] data;
    private int heapSize;

    //indexing reaction index
    private Hashtable<Integer, NRMNode> indexTable;
    
    //constructor
    public BinaryHeap(int size) {
        heapSize = size;
        data = new NRMNode[size];
        
        indexTable = new Hashtable<Integer, NRMNode>();
    }

    public BinaryHeap(NRMNode[] items) {
        heapSize = items.length;
        data = new NRMNode[items.length];
        
        indexTable = new Hashtable<Integer, NRMNode>();
        
        //push data into heap
        for (int i = 0; i < items.length; i++) {
            data[i] = items[i];
            
            indexTable.put(items[i].getReactionIndex(), items[i]);
        }
        
        //reconstruct heap
        for(int i = (data.length - 1) / 2; i >= 0; i--)
        {
            shiftDown(i);
        }        
    }
    
    //insert new node
    public void insert(NRMNode value) {
        if (heapSize == data.length) {
            doubleArray();
        } else {
            heapSize++;
            data[heapSize - 1] = value;
            
            indexTable.put(value.getReactionIndex(), value);
            
            shiftUp(heapSize - 1);
        }
    }
 
    public boolean isEmpty() {
        return (heapSize == 0);
    }
    
    public NRMNode getMin() {
//        if (isEmpty()) {
//            throw new RuntimeException("Heap is empty");
//        } else {
            return data[0];
//        }
    }

    //get node by reaction index
    public NRMNode getNodeByReactionIndex(int reactionIndex) {
        return indexTable.get(reactionIndex);
    }
    
    //get node by reaction index
    public NRMNode getNodeByPosition(int nodeIndex) {
        return data[nodeIndex];
    }
    
//     public void removeMin() {
//        if (isEmpty()) {
//            throw new RuntimeException("Heap is empty");
//        } else {
//            data[0] = data[heapSize - 1];
//            heapSize--;
//            if (heapSize > 0) {
//                shiftDown(0);
//            }
//        }
//    }    
    
    public void reconstruct(int nodeIndex) {
        int parentIndex = getParentIndex(nodeIndex);
//        HeapNode tmp;
        
        if (data[parentIndex].compareTo(data[nodeIndex]) > 0) {
//            tmp = data[parentIndex];
//            
//            data[parentIndex] = data[nodeIndex];
//            data[parentIndex].setHeapNodeIndex(parentIndex);
//            
//            data[nodeIndex] = tmp;
//            data[nodeIndex].setHeapNodeIndex(nodeIndex);
//            
            doExchangeNode(parentIndex, nodeIndex);
            reconstruct(parentIndex);
        }else{
            shiftDown(nodeIndex);
        }
    }
    
    public void printHeap() {
//        doPrintHeap(0);
        for (int i = 0; i < heapSize; i++) {
            System.out.println(i + ": " + data[i]);
        }
    }
    
//    private void doPrintHeap(int nodeIndex)
//    {
//        System.out.println(nodeIndex + ": " + data[nodeIndex]);
//        int leftChild = getLeftChildIndex(nodeIndex);
//        int rightChild = getRightChildIndex(nodeIndex);
//        if(leftChild < heapSize)
//            doPrintHeap(leftChild);
//        if(rightChild < heapSize)
//            doPrintHeap(rightChild);
//    }
    
    private int getLeftChildIndex(int nodeIndex) {
        return 2 * nodeIndex + 1;
    }

    private int getRightChildIndex(int nodeIndex) {
        return 2 * nodeIndex + 2;
    }

    private int getParentIndex(int nodeIndex) {
        return (nodeIndex - 1) / 2; //truncation using floor
    }
    
    private void doubleArray() {
        NRMNode[] newData;

        newData = new NRMNode[data.length * 2];
        for (int i = 0; i < data.length; i++) {
            newData[i] = data[i];
        }
        data = newData;
    }
    
    private void shiftUp(int nodeIndex) {
        int parentIndex;
//        HeapNode tmp;
        if (nodeIndex != 0) {
            parentIndex = getParentIndex(nodeIndex);
            if (data[parentIndex].compareTo(data[nodeIndex]) > 0) {
//                tmp = data[parentIndex];
//                
//                data[parentIndex] = data[nodeIndex];
//                data[parentIndex].setHeapNodeIndex(parentIndex);
//                
//                data[nodeIndex] = tmp;
//                data[nodeIndex].setHeapNodeIndex(nodeIndex);
//                
                doExchangeNode(parentIndex, nodeIndex);
                shiftUp(parentIndex);
            }
        }
    }
   
    private void shiftDown(int nodeIndex) {
        int leftChildIndex, rightChildIndex, minIndex;
//        HeapNode tmp;
        leftChildIndex = getLeftChildIndex(nodeIndex);
        rightChildIndex = getRightChildIndex(nodeIndex);
        if (rightChildIndex >= heapSize) {
            if (leftChildIndex >= heapSize) {
                return;
            } else {
                minIndex = leftChildIndex;
            }
        } else {
            if (data[leftChildIndex].compareTo(data[rightChildIndex]) <= 0) {
                minIndex = leftChildIndex;
            } else {
                minIndex = rightChildIndex;
            }
        }
        if (data[nodeIndex].compareTo(data[minIndex]) > 0) {
//            tmp = data[minIndex];
//            
//            data[minIndex] = data[nodeIndex];
//            data[minIndex].setHeapNodeIndex(minIndex);
//
//            data[nodeIndex] = tmp;
//            data[nodeIndex].setHeapNodeIndex(nodeIndex);
            
            doExchangeNode(minIndex, nodeIndex);        
            shiftDown(minIndex);
        }
    }
    
    private void doExchangeNode(int nodeIndexFrom, int nodeIndexTo){
        NRMNode tmp = data[nodeIndexFrom];
            
        data[nodeIndexFrom] = data[nodeIndexTo];
        data[nodeIndexFrom].setNodeIndex(nodeIndexFrom);

        data[nodeIndexTo] = tmp;
        data[nodeIndexTo].setNodeIndex(nodeIndexTo);
    }
}