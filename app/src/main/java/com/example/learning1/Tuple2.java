package com.example.learning1;

public class Tuple2<K, V>{
    private K first;
    private V second;
    public Tuple2(K first,V second){
        this.first=first;
        this.second=second;
    }

    public K getFirst(){
        return first;
    }
    public V getSecond(){
        return second;
    }
}