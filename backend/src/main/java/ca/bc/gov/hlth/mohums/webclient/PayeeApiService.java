package ca.bc.gov.hlth.mohums.webclient;

import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

@Service
public class PayeeApiService {

    private final String payeePath = "/payee-mapping";

    private final ExternalApiCaller payeeApiCaller;

    public PayeeApiService(@Qualifier("payeeApiCaller") ExternalApiCaller payeeApiCaller) {
        this.payeeApiCaller = payeeApiCaller;
    }
    
    public ResponseEntity<Object> getPayee(String userId) {
    	String path = String.format("%s/%s", payeePath, userId);
    	
    	return payeeApiCaller.get(path, null);
    }

}
