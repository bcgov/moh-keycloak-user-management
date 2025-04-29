package ca.bc.gov.hlth.mohums.util;

import org.junit.jupiter.api.Test;

import java.util.Arrays;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class AuthorizedClientsParserTest {

    @Test
    public void testParse() {
        /* JWT includes view-client-bcer-cp, view-client-fmdb, view-client-gis, view-client-miwt, view-client-webcaps */
        String jwt = "eyJhbGciOiJSUzI1NiIsInR5cCIgOiAiSldUIiwia2lkIiA6ICJwdG1aOTAxOXFaZU5xdS1kWmQ5YlBBdnlRVm1KMGgtblNDS0djN1J6SXAwIn0.eyJleHAiOjE2MDY4NTM0MDcsImlhdCI6MTYwNjg1MTYwNywianRpIjoiOTYxYTkwNjEtMjIwYi00ODE4LWJiM2QtZjEyZDM3MWY0NGU1IiwiaXNzIjoiaHR0cHM6Ly9jb21tb24tbG9nb24tZGV2LmhsdGguZ292LmJjLmNhL2F1dGgvcmVhbG1zL21vaF9hcHBsaWNhdGlvbnMiLCJhdWQiOiJyZWFsbS1tYW5hZ2VtZW50Iiwic3ViIjoiN2M1ZGRmMzAtMTc1NC00OTBiLWFlNDMtYmE3MWNkNTQ0ZTZiIiwidHlwIjoiQmVhcmVyIiwiYXpwIjoiVVNFUi1NQU5BR0VNRU5ULVNFUlZJQ0UiLCJzZXNzaW9uX3N0YXRlIjoiMWM5ZGFmZDQtMTMwZC00YWViLTk5NmEtYzhlZTA3NGNmYWQ0IiwiYWNyIjoiMSIsInJlc291cmNlX2FjY2VzcyI6eyJyZWFsbS1tYW5hZ2VtZW50Ijp7InJvbGVzIjpbIm1hbmFnZS11c2VycyIsInZpZXctdXNlcnMiLCJ2aWV3LWNsaWVudHMiLCJxdWVyeS1jbGllbnRzIiwicXVlcnktZ3JvdXBzIiwicXVlcnktdXNlcnMiXX0sIlVTRVItTUFOQUdFTUVOVC1TRVJWSUNFIjp7InJvbGVzIjpbInZpZXctY2xpZW50LWJjZXItY3AiLCJ2aWV3LWNsaWVudC1naXMiLCJ2aWV3LWNsaWVudC1mbWRiIiwidmlldy1jbGllbnQtd2ViY2FwcyIsIm1hbmFnZS11c2VycyIsInZpZXctZ3JvdXBzIiwidmlldy11c2VycyIsInZpZXctY2xpZW50cyIsInZpZXctY2xpZW50LW1pd3QiXX19LCJzY29wZSI6Im9wZW5pZCBwcm9maWxlIGVtYWlsIiwiZW1haWxfdmVyaWZpZWQiOmZhbHNlLCJjbGllbnRJZCI6IlVTRVItTUFOQUdFTUVOVC1TRVJWSUNFIiwiY2xpZW50SG9zdCI6IjE0Mi4zNC4xNDcuNCIsInByZWZlcnJlZF91c2VybmFtZSI6InNlcnZpY2UtYWNjb3VudC11c2VyLW1hbmFnZW1lbnQtc2VydmljZSIsImNsaWVudEFkZHJlc3MiOiIxNDIuMzQuMTQ3LjQifQ.tf5syh86Zvi_vUMzf_i8a0KwKuePXu5v9gftPlpdkzUYCXLxZht3jzhwd3Xx58aWU7Vwl1QAJ1Q9cviEPfqeoSd9KedHcBEm-w_qOXmx7Pl0IV59Ta84-sB6h8AmGcvBh1cJikjX-_ejd9irH1_7qfKyya16_Y7-9jTz-j6IzTYu35aHzyq6GQbwxmITnnPEFWWEwxj88MJWNrTEO6I0N2WqhZaxXneUZ1pLLmCAa5c6cX_BS2UdB_Jgy-68e4PjiG3Rj-xnoBX5zynJU0mQZ39gHXyQxb3zgMEirJyA1MmQcAh9q7PIntQGEQMkURCDx5mLBMebImlvNrugUt4Jug";
        List<String> knownAuthorizedClients = Arrays.asList("bcer-cp", "fmdb", "gis", "miwt", "webcaps");
        AuthorizedClientsParser acp = new AuthorizedClientsParser();
        List<String> authorizedClients = acp.parseToReadOnly(jwt);

        assertEquals(knownAuthorizedClients.size(), authorizedClients.size());
        for (String authorizedClient : authorizedClients) {
            assertTrue(knownAuthorizedClients.contains(authorizedClient));
        }
    }

}