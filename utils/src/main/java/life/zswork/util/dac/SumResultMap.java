package life.zswork.util.dac;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

public class SumResultMap {

    private final Map<Integer, Object> sumResultMap;

    private boolean notNull;

    private SumResultMap(Map<Integer, Object> sumResultMap) {
        this.sumResultMap = sumResultMap;
    }

    protected static SumResultMap build() {
        return new SumResultMap(new ConcurrentHashMap<>());
    }

    public SumResultMap notNullable() {
        notNull = true;
        return this;
    }

    @SuppressWarnings("unchecked")
    /**
     * index start from 0
     */
    public <K, V> Map<K, V> getMapResult(int index, Class<K> kClass, Class<V> vClass) {
        Object sumResult = sumResultMap.get(index);
        if (Objects.isNull(sumResult)) {
            return notNull ? new HashMap<>() : null;
        }
        return (Map<K, V>) sumResult;
    }

    @SuppressWarnings("unchecked")
    /**
     * index start from 0
     */
    public <K, V> Map<K, List<V>> getMapListResult(int index, Class<K> kClass, Class<V> vClass) {
        Object sumResult = sumResultMap.get(index);
        if (Objects.isNull(sumResult)) {
            return notNull ? new HashMap<>() : null;
        }
        return (Map<K, List<V>>) sumResult;
    }

    @SuppressWarnings("unchecked")
    /**
     * index start from 0
     */
    public <V> V getResult(int index, Class<V> clazz) throws InstantiationException, IllegalAccessException {
        Object sumResult = sumResultMap.get(index);
        if (Objects.isNull(sumResult)) {
            return notNull ? clazz.newInstance() : null;
        }
        return (V) sumResult;
    }

    @SuppressWarnings("unchecked")
    /**
     * index start from 0
     */
    public <V> List<V> getListResult(int index, Class<V> vClass) {
        Object sumResult = sumResultMap.get(index);
        if (Objects.isNull(sumResult)) {
            return notNull ? new ArrayList<>() : null;
        }
        return (List<V>) sumResult;
    }

    protected void put(int index, Object o) {
        sumResultMap.put(index, o);
    }
}
