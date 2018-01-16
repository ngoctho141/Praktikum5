/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package praktikum5;

/**
 *
 * @author s-nnguy1
 */
public class Response {
    HttpResponseCode code;
    String value;

    public Response(HttpResponseCode code, String value) {
        this.code = code;
        this.value = value;
    }

    public HttpResponseCode getCode() {
        return code;
    }

    public String getValue() {
        return value;
    }


    
    
}
