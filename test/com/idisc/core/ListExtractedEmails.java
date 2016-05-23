package com.idisc.core;

import com.bc.jpa.ControllerFactory;
import com.bc.jpa.EntityController;
import com.idisc.pu.entities.external.UnofficialEmails;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;

/**
 * @author poshjosh
 */
public class ListExtractedEmails {

    public ListExtractedEmails() { }

    public static void main(String [] args) {
        try{
            IdiscTestBase init = new IdiscTestBase(Level.FINER);
            Map params = new HashMap();
//            listEmails(init, Extractedemail.class, "emailAddress", null, 2000);
            params.put("emailStatus", 2);
            listEmails(init, UnofficialEmails.class, "emailAddress", params, 2000);
            params.put("emailStatus", 9);
            listEmails(init, UnofficialEmails.class, "emailAddress", params, 2000);
            params.put("emailStatus", 11);
            listEmails(init, UnofficialEmails.class, "emailAddress", params, 2000);
        }catch(Exception e) {
            e.printStackTrace();
        }
    }
    private static <E> void listEmails(
            IdiscTestBase init, Class<E> entityType, 
            String emailAddressColumnName, 
            Map params, int batchSize) {
        ControllerFactory cf = init.getIdiscApp().getControllerFactory();
        EntityController<E, Integer> ec = cf.getEntityController(entityType, Integer.class);
        int offset = 0;
        List selected;
        do{
             selected = ec.selectColumn(emailAddressColumnName, params, null, offset, batchSize);
             if(selected == null || selected.isEmpty()) {
                 break;
             }
            for(Object o:selected) {
System.out.println(o);
            }
            offset += selected.size();
        }while(true);
    }
}
