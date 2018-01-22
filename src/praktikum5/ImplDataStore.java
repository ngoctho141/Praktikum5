/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package praktikum5;

import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.util.HashMap;
import java.util.Map;

/**
 *
 * @author s-nnguy1
 */
public class ImplDataStore implements DataStore {

    private Map<String, Map<String, String>> idsToMap;

    @Override
    public void clear(String id) throws IOException {
        idsToMap.clear();
    }

    private ImplDataStore() {
        idsToMap = new  HashMap<>();
    }

    @Override
    public void deleteEntry(String id, String key) throws IOException {
        idsToMap.get(id).remove(key);
    }

    @Override
    public void deleteMap(String id) throws IOException {
        if (!idsToMap.containsKey(id)) {
            throw new IllegalArgumentException("Map doesn't contain key " + id);
        } else {
            idsToMap.remove(id);
        }
    }

    @Override
    public String getValue(String id, String key) {
        return idsToMap.get(id).get(key);
    }

    @Override
    public void newMap(String id) throws IOException {
        if (idsToMap.containsKey(id)) {
            throw new IllegalArgumentException("Map has already had key " + id);
        } else {
            idsToMap.put(id, new HashMap<String, String>());
        }
    }

    @Override
    public void putValue(String id, String key, String value) throws IOException{
        if (!idsToMap.containsKey(id)) {
            throw new IllegalArgumentException("No map with id " + id);
        } else {
            idsToMap.get(id).put(key, value);
        }
    }

    @Override
    public void save() throws IOException {
        FileOutputStream fos = new FileOutputStream("file.ser");
        ObjectOutputStream oos = new ObjectOutputStream(fos);
        oos.writeObject(this);
    }
    
    public static DataStore load() {
		try ( FileInputStream fos = new FileInputStream("file.ser");
			  ObjectInputStream oos = new ObjectInputStream(fos); ) {
			return (DataStore) oos.readObject();
                }
		 catch (ClassNotFoundException | IOException e) {
                        return new ImplDataStore();
		}
 
    }

}
