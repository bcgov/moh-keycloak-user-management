package ca.bc.gov.hlth.mohums.util;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import org.junit.jupiter.api.Test;

import java.util.Arrays;
import java.util.List;

public class AuthorizedClientsParserTest {

    @Test
    public void testParse() {
        /* JWT includes view-client-bcer-cp, view-client-fmdb, view-client-gis, view-client-miwt, view-client-webcaps */
        String jwt
                = "eyJhbGciOiJSUzI1NiIsInR5cCIgOiAiSldUIiwia2lkIiA6ICJwdG1aOTAxOXFaZU5xdS1kWmQ5YlBBdnlRVm1KMGgtblNDS0dj"
                + "N1J6SXAwIn0.eyJleHAiOjE2MDU4MDg2ODgsImlhdCI6MTYwNTgwNjg4OCwianRpIjoiMDhhYWNmNmItZDlkNC00ZDg2LWJhNDIt"
                + "MGFlMDgzYjhjNDJhIiwiaXNzIjoiaHR0cHM6Ly9jb21tb24tbG9nb24tZGV2LmhsdGguZ292LmJjLmNhL2F1dGgvcmVhbG1zL21v"
                + "aF9hcHBsaWNhdGlvbnMiLCJhdWQiOiJyZWFsbS1tYW5hZ2VtZW50Iiwic3ViIjoiOGRmNWFiYWEtNzU0NS00MmRhLWE1OTQtOTFj"
                + "YjM2MzU5MDZmIiwidHlwIjoiQmVhcmVyIiwiYXpwIjoidXNlci1tYW5hZ2VtZW50LXNlcnZpY2UiLCJzZXNzaW9uX3N0YXRlIjoi"
                + "OTJkNDBmZGEtOTlmMi00MzlhLTgzODQtNThiOTBjM2JmYTMwIiwiYWNyIjoiMSIsInJlc291cmNlX2FjY2VzcyI6eyJyZWFsbS1t"
                + "YW5hZ2VtZW50Ijp7InJvbGVzIjpbIm1hbmFnZS11c2VycyIsInZpZXctdXNlcnMiLCJ2aWV3LWNsaWVudHMiLCJxdWVyeS1jbGll"
                + "bnRzIiwicXVlcnktZ3JvdXBzIiwicXVlcnktdXNlcnMiXX0sInVzZXItbWFuYWdlbWVudC1zZXJ2aWNlIjp7InJvbGVzIjpbInZp"
                + "ZXctY2xpZW50LWJjZXItY3AiLCJ2aWV3LWNsaWVudC1naXMiLCJ2aWV3LWNsaWVudC1mbWRiIiwidmlldy1jbGllbnQtd2ViY2Fw"
                + "cyIsInZpZXctZ3JvdXBzIiwidmlldy11c2VycyIsInZpZXctY2xpZW50cyIsInZpZXctY2xpZW50LW1pd3QiXX19LCJzY29wZSI6"
                + "InByb2ZpbGUgZW1haWwiLCJjbGllbnRIb3N0IjoiMTQyLjM0LjE0Ny40IiwiY2xpZW50SWQiOiJ1c2VyLW1hbmFnZW1lbnQtc2Vy"
                + "dmljZSIsImVtYWlsX3ZlcmlmaWVkIjpmYWxzZSwicHJlZmVycmVkX3VzZXJuYW1lIjoic2VydmljZS1hY2NvdW50LXVzZXItbWFu"
                + "YWdlbWVudC1zZXJ2aWNlIiwiY2xpZW50QWRkcmVzcyI6IjE0Mi4zNC4xNDcuNCJ9.M6nJsaO1HnU5ypEVA9eAcV-k3DNHOi_8gAS"
                + "oaIw5Ew6ITAa9jiz8l0JA69qOe5YBDiStlJghxEM92arvbblXY7EhK1pvnn5IKPF5D4Q1YMlMbGgX0rP39bMX5lV0hisgscUPAUp"
                + "vqGp8d_dHaEKAD81Q1cLmwMMGLfbK_f5Qf2oaKh5udGuoDpoETZ5WjCnj2AKk40LPaDe1RHr7Zkoo3qSx3kosePX0guF46DuTAmv"
                + "NMfQmR-bxajbOGQNoRewrIxR6nab0gew64VJbPR6bgXLUH5jfbUrp3k3o0mWsxqq8lheye388bzDXoees4rFqh3fgbTbXntX_sA3"
                + "LhsB0Rw";
        List<String> knownAuthorizedClients = Arrays.asList("bcer-cp", "fmdb", "gis", "miwt", "webcaps");
        AuthorizedClientsParser acp = new AuthorizedClientsParser();
        List<String> authorizedClients = acp.parse(jwt);

        assertEquals(knownAuthorizedClients.size(), authorizedClients.size());
        for (String authorizedClient : authorizedClients) {
            assertTrue(knownAuthorizedClients.contains(authorizedClient));
        }
    }

}
