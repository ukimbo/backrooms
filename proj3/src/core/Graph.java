package core;

import java.util.*;

public class Graph<K, V> {
    private int size;
    private class Node {
        private K id;
        private V value;
        public Node(K id, V value) {
            this.id = id;
            this.value = value;
        }
        public Node(Node copy) {
            this.id = copy.id;
            this.value = copy.value;
        }
    }
    private HashMap<K, List<Node>> arrayList;
    public Graph() {
        arrayList = new HashMap<>();
    }
    public void addNode(K id, V value) {
        if (!arrayList.containsKey(id)) {
            List<Node> newList = new ArrayList<>();
            newList.add(new Node(id, value));
            arrayList.put(id, newList);
            size += 1;
        } else {
            List<Node> newList = new ArrayList<>(arrayList.get(id));
            newList.add(new Node(id, value));
            arrayList.put(id, newList);
        }
    }
    public void addEdge(K originId, K destinationId) {
        for (Node node: arrayList.get(destinationId)) {
            if (destinationId.equals(node.id)) {
                List<Node> newList = new ArrayList<>(arrayList.get(originId));
                //newList.add(node); This option uses less memory but idek if it works
                newList.add(new Node(node)); //This option is way cleaner with the Java visualizer but uses more memory
                arrayList.put(originId, newList);
            }
        }
    }

    public List<Node> getChildren(K id) {
        return arrayList.get(id);
    }
    public List<V> graphTraversal(K id) {
        Set<K> visited = new HashSet<>();
        Set<V> values = new HashSet<>();
        traversalHelper(id, visited, values);
        return List.copyOf(values);
    }

    public void traversalHelper(K id, Set<K> visited, Set<V> values) {
        if (!visited.contains(id)) {
            visited.add(id);
            for (Node node: arrayList.get(id)) {
                if (id.equals(node.id)) {
                    values.add(node.value);
                } else {
                    traversalHelper(node.id, visited, values);
                }
            }
        }

    }
    public List<V> getChildren2(K id) {
        List<V> lst = new ArrayList<>();
        List<Node> nodeLst = arrayList.get(id);
        for (Node node: nodeLst) {
            if (!lst.contains(node.value)) {
                lst.add(node.value);
            }
        }
        return lst;
    }
    public boolean contains(K key) {
        return arrayList.containsKey(key);
    }
    //For Testing
    public List<V> getChildren(K id, Comparator<V> c) {
        List<V> lst = new ArrayList<>();
        List<Node> nodeLst = arrayList.get(id);
        for (Node node: nodeLst) {
            if (!lst.contains(node.value)) {
                lst.add(node.value);
            }
        }
        lst.sort(c);
        return lst;
    }
}
