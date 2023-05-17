package ca.bc.gov.hlth.mohums.webclient;

import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

import ca.bc.gov.hlth.mohums.model.UserPayee;

@Service
public class PayeeApiService {

    private static final String PAYEE_PATH = "/payee-mapping";

    private final ExternalApiCaller payeeApiCaller;

    public PayeeApiService(@Qualifier("payeeApiCaller") ExternalApiCaller payeeApiCaller) {
        this.payeeApiCaller = payeeApiCaller;
    }
    
    public ResponseEntity<Object> getPayee(String userId) {
    	String path = String.format("%s/%s", PAYEE_PATH, userId);
    	
    	return payeeApiCaller.get(path, null);
    }
    
    public ResponseEntity<Object> addPayee(UserPayee payee) {
    	String path = String.format("%s", PAYEE_PATH);
    	
    	return payeeApiCaller.post(path, payee);
    }
    
    public ResponseEntity<Object> updatePayee(String userId, UserPayee payee) {
    	String path = String.format("%s/%s", PAYEE_PATH, userId);
    	
    	return payeeApiCaller.put(path, payee);
    }
    
    public ResponseEntity<Object> deletePayee(String userId) {
    	String path = String.format("%s/%s", PAYEE_PATH, userId);
    	
    	return payeeApiCaller.delete(path);
    }

}
