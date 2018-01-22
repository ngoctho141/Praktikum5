/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package praktikum5;

import java.io.IOException;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 *
 * @author s-nnguy1
 */
public class Processors {

    static Response createNewMap(String[] resource, DataStore dataStore) {
        try {
            dataStore.newMap(resource[0]);
        } catch (IOException ex) {
            ex.printStackTrace();
        } catch (IllegalArgumentException e) {
            e.printStackTrace();
            return new Response(HttpResponseCode.CONFLICT, HttpResponseCode.CONFLICT.getCode() + "");
        }
        return new Response(HttpResponseCode.CREATED, HttpResponseCode.CREATED.getCode() + "");
    }

    ;
    static Response getValue(String[] resource, DataStore dataStore) {
        try {
            dataStore.getValue(resource[0], resource[1]);
        } catch (IllegalArgumentException e) {
            return new Response(HttpResponseCode.NOT_FOUND, HttpResponseCode.NOT_FOUND.getCode() + "");
        }
        return new Response(HttpResponseCode.OK, HttpResponseCode.OK.getCode() + "");
    }

    static Response putValue(String[] resource, DataStore dataStore) {
        try {
            dataStore.putValue(resource[0], resource[1], resource[3]);
        } catch (IllegalArgumentException e) {
            return new Response(HttpResponseCode.NOT_FOUND, HttpResponseCode.NOT_FOUND.getCode() + "");
        } catch (IOException ex) {
            ex.printStackTrace();
        }
        return new Response(HttpResponseCode.OK, HttpResponseCode.OK.getCode() + "");
    }

    static Response delete(String[] resource, DataStore dataStore) {
        return null;
    }

    static Response deleteEntry(String[] resource, DataStore dataStore) {
        try {
            dataStore.deleteEntry(resource[0], resource[1]);
        } catch (IllegalArgumentException e) {
            return new Response(HttpResponseCode.NOT_FOUND, HttpResponseCode.NOT_FOUND.getCode() + "");
        } catch (IOException ex) {
            ex.printStackTrace();
        }
        return new Response(HttpResponseCode.NO_CONTENT, HttpResponseCode.NO_CONTENT.getCode() + "");
    }

    static Response clearMap(String[] resource, DataStore dataStore) {
        return null;
    }

    static Response deleteMap(String[] resource, DataStore dataStore) {
        return null;
    }

}
