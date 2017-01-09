package ua.edu.ucu.stream;

import ua.edu.ucu.function.*;

import java.util.LinkedList;
import java.util.ListIterator;

public class AsIntStream implements IntStream {
    LinkedList<Integer> container;
    LinkedList<Object> pipeline;

    private AsIntStream() {
        this.container = new LinkedList<>();
        this.pipeline = new LinkedList<>();
    }

    private AsIntStream(LinkedList<Integer> container, LinkedList<Object> pipeline) {
        this.container = container;
        this.pipeline = pipeline;
    }

    private AsIntStream IntermediateOperation(Object operation) {
        LinkedList<Integer> newContainer = new LinkedList<>();
        ListIterator<Integer> containerIterator = this.container.listIterator();
        while (containerIterator.hasNext()) {
            newContainer.add(new Integer(containerIterator.next()));
        }

        LinkedList<Object> newPipeline = new LinkedList<>();
        ListIterator<Object> pipelineIterator = this.pipeline.listIterator();
        while (pipelineIterator.hasNext()) {
            newPipeline.add(pipelineIterator.next());
        }
        newPipeline.add(operation);

        return new AsIntStream(newContainer, newPipeline);
    }

    private void PipelineExecution() {
        ListIterator<Object> pipelineIterator = this.pipeline.listIterator();
        while (pipelineIterator.hasNext()) {
            Object op = pipelineIterator.next();
            if(op instanceof IntPredicate) {
                filterExecution((IntPredicate) op);
            } else if(op instanceof IntUnaryOperator) {
                mapExecution((IntUnaryOperator) op);
            } else if(op instanceof IntToIntStreamFunction) {
                flatMapExecution((IntToIntStreamFunction) op);
            }
        }
    }

    private void add(int value){
        this.container.add(value);
    }

    public static IntStream of(int... values) {
        AsIntStream asIntStream = new AsIntStream();
        for(Integer val: values){
            asIntStream.add(val);
        }
        return asIntStream;
    }

    @Override
    public Double average() {
        PipelineExecution();

        if(this.container.isEmpty()) {
            throw new IllegalStateException("Stream is empty");
        }

        return this.sum().doubleValue()/this.container.size();
    }

    @Override
    public Integer max() {
        PipelineExecution();

        if(this.container.isEmpty()) {
            throw new IllegalStateException("Stream is empty");
        }

        Integer max = container.get(0);
        ListIterator<Integer> containerIterator = this.container.listIterator();
        while (containerIterator.hasNext()) {
            Integer val = containerIterator.next();
            if(val.compareTo(max) > 0) {
                max = val;
            }
        }

        return max;
    }

    @Override
    public Integer min() {
        PipelineExecution();

        if(this.container.isEmpty()) {
            throw new IllegalStateException("Stream is empty");
        }

        Integer min = container.get(0);
        ListIterator<Integer> containerIterator = this.container.listIterator();
        while (containerIterator.hasNext()) {
            Integer val = containerIterator.next();
            if(val.compareTo(min) < 0) {
                min = val;
            }
        }

        return min;
    }

    @Override
    public long count() {
        PipelineExecution();

        return this.container.size();
    }

    @Override
    public Integer sum() {
        PipelineExecution();

        Integer sum = 0;
        ListIterator<Integer> containerIterator = this.container.listIterator();
        while (containerIterator.hasNext()){
            sum += containerIterator.next();
        }

        return sum;
    }

    @Override
    public IntStream filter(IntPredicate predicate) {
        return IntermediateOperation(predicate);
    }

    private void filterExecution(IntPredicate predicate) {
        ListIterator<Integer> containerIterator = this.container.listIterator();
        while (containerIterator.hasNext()){
            if(!predicate.test(containerIterator.next())) {
                containerIterator.remove();
            }
        }
    }

    @Override
    public void forEach(IntConsumer action) {
        PipelineExecution();

        for(Integer val : this.container) {
            action.accept(val);
        }
    }

    @Override
    public IntStream map(IntUnaryOperator mapper) {
        return IntermediateOperation(mapper);
    }

    private void mapExecution(IntUnaryOperator mapper) {
        ListIterator<Integer> containerIterator = this.container.listIterator();
        while (containerIterator.hasNext()) {
            Integer val = containerIterator.next();
            containerIterator.set(mapper.apply(val));
        }
    }

    @Override
    public IntStream flatMap(IntToIntStreamFunction func) {
        return IntermediateOperation(func);
    }

    private void flatMapExecution(IntToIntStreamFunction func) {
        LinkedList<Integer> newContainer = new LinkedList<>();
        ListIterator<Integer> containerIterator = this.container.listIterator();
        while (containerIterator.hasNext()) {
            IntStream temp = func.applyAsIntStream(containerIterator.next());
            for (int el : temp.toArray()){
                newContainer.add(el);
            }
        }
        this.container = newContainer;
    }

    @Override
    public int reduce(int identity, IntBinaryOperator op) {
        PipelineExecution();
        ListIterator<Integer> containerIterator = this.container.listIterator();
        while (containerIterator.hasNext()) {
            identity = op.apply(identity, containerIterator.next());
        }
        return identity;
    }

    @Override
    public int[] toArray() {
        PipelineExecution();
        int[] array = new int[this.container.size()];
        for(int i=0; i<container.size(); i++) {
            array[i] = this.container.get(i);
        }
        return array;
    }

}
