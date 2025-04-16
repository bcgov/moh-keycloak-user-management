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
        List<String> authorizedClients = acp.parse(jwt);

        assertEquals(knownAuthorizedClients.size(), authorizedClients.size());
        for (String authorizedClient : authorizedClients) {
            assertTrue(knownAuthorizedClients.contains(authorizedClient));
        }
    }
    
    @Test
    public void testParseEdit_PLR_readonlyToken() {
        /* JWT includes [plr (read-only-client-plr should not be included in Authorized clients), hcimweb, licence-status, sa-hibc-service-bc-portal, sa-dbaac-portal, pidp-service, prp-service, dmft-webapp, pho-rsc, hcap-fe, umc-e2e-tests, bcer-cp, maid, emcod, sa-sfdc, mspdirect-service, hcimweb_hd2] */
        String jwt = "eyJhbGciOiJSUzI1NiIsInR5cCIgOiAiSldUIiwia2lkIiA6ICJwdG1aOTAxOXFaZU5xdS1kWmQ5YlBBdnlRVm1KMGgtblNDS0djN1J6SXAwIn0.eyJleHAiOjE3NDQ2NzA1MTAsImlhdCI6MTc0NDY3MDIxMCwiYXV0aF90aW1lIjoxNzQ0NjcwMTMwLCJqdGkiOiI2MDU2ZGExNC0yMjBiLTQwYTEtYTBhYS03NGYyMWYwODcyZjIiLCJpc3MiOiJodHRwczovL2NvbW1vbi1sb2dvbi1kZXYuaGx0aC5nb3YuYmMuY2EvYXV0aC9yZWFsbXMvbW9oX2FwcGxpY2F0aW9ucyIsImF1ZCI6IlVTRVItTUFOQUdFTUVOVC1TRVJWSUNFIiwic3ViIjoiY2YzOTg3MDUtZWY3OC00MWE4LTlhZGQtNjU5MDdhNTVhMzVlIiwidHlwIjoiQmVhcmVyIiwiYXpwIjoiVVNFUi1NQU5BR0VNRU5UIiwibm9uY2UiOiIzOTkxNWY4Mi1hMjhmLTRmZTctODMxNC1jMjdhZjJlOTQ4OGQiLCJzZXNzaW9uX3N0YXRlIjoiZmE2YWQxZDItMGI0YS00ZGQyLWIyNjUtYmQ1Y2Y3NzA3MTJkIiwiYWxsb3dlZC1vcmlnaW5zIjpbIioiXSwicmVzb3VyY2VfYWNjZXNzIjp7IlVTRVItTUFOQUdFTUVOVCI6eyJyb2xlcyI6WyJ1c2VyLW1hbmFnZW1lbnQtYWRtaW4iXX0sIlVTRVItTUFOQUdFTUVOVC1TRVJWSUNFIjp7InJvbGVzIjpbInJlYWQtb25seS1wbHIiLCJ2aWV3LWNsaWVudC1oY2ltd2ViIiwidmlldy1jbGllbnQtbGljZW5jZS1zdGF0dXMiLCJ2aWV3LWNsaWVudC1zYS1oaWJjLXNlcnZpY2UtYmMtcG9ydGFsIiwidmlldy1jbGllbnQtc2EtZGJhYWMtcG9ydGFsIiwidmlldy1jbGllbnQtcGlkcC1zZXJ2aWNlIiwidmlldy1ncm91cHMiLCJ2aWV3LXVzZXJzIiwibWFuYWdlLXVzZXItZGV0YWlscyIsInZpZXctY2xpZW50LXBycC1zZXJ2aWNlIiwidmlldy1tZXRyaWNzIiwidmlldy1jbGllbnQtZG1mdC13ZWJhcHAiLCJ2aWV3LWNsaWVudC1waG8tcnNjIiwibWFuYWdlLW9yZyIsInZpZXctY2xpZW50LWhjYXAtZmUiLCJjcmVhdGUtdXNlciIsInZpZXctY2xpZW50LXVtYy1lMmUtdGVzdHMiLCJ2aWV3LWNsaWVudC1iY2VyLWNwIiwidmlldy1ldmVudHMiLCJ2aWV3LWNsaWVudC1tYWlkIiwidmlldy1jbGllbnQtZW1jb2QiLCJidWxrLXJlbW92YWwiLCJtYW5hZ2UtdXNlci1yb2xlcyIsInZpZXctY2xpZW50LXNhLXNmZGMiLCJ2aWV3LWNsaWVudHMiLCJtYW5hZ2UtYWxsLWdyb3VwcyIsInZpZXctY2xpZW50LW1zcGRpcmVjdC1zZXJ2aWNlIiwidmlldy1jbGllbnQtaGNpbXdlYl9oZDIiXX19LCJzY29wZSI6Im9wZW5pZCBtb2hfaWRwIHByb2ZpbGUgZW1haWwgaWRpcl9hYWQiLCJzaWQiOiJmYTZhZDFkMi0wYjRhLTRkZDItYjI2NS1iZDVjZjc3MDcxMmQiLCJlbWFpbF92ZXJpZmllZCI6ZmFsc2UsIm5hbWUiOiJNZWhkaSBTYWRpayIsImdyb3VwcyI6WyJDR0kgQWNjZXNzIE1hbmFnZW1lbnQgVGVhbSJdLCJwcmVmZXJyZWRfdXNlcm5hbWUiOiJtZXNhZGlrQGlkaXIiLCJnaXZlbl9uYW1lIjoiTWVoZGkiLCJmYW1pbHlfbmFtZSI6IlNhZGlrIiwiZW1haWwiOiJtZWhkaS5zYWRpa0BjZ2kuY29tIn0.MeAzSXmXLiiQ9niaEeI2O6W-Nrxz4c_-QKikC_bvsTHi8aZgsFaP9KowTU8209xfhh_h6_kELJQMATrzgCWZ4pvD_VrZUUvXZeRanAPJftOa3BhmHqJ-ny98RZ-RfjsPKzcEgBJD9HexEqWTWTApj3Qz_SvkGJcLAcPkttgL8E-Hxd4HGW-vTdi6szn8IyJJyORk-YfADrPq9dXIim7KJ9LMH7XDE6JfKmHcdOcvfMic5GOhVMA4DnfrVYMAjx7K_d4iHIJMB21_UE42h7Z5byXGVhn_eEQzbZwJTikksCOlt-RaLoPF9lqFWPHUMeTccE6YJMP0pUGh_lawxDnpUQ";
		List<String> knownAuthorizedClients = Arrays.asList("hcimweb", "licence-status",
				"sa-hibc-service-bc-portal", "sa-dbaac-portal", "pidp-service", "prp-service", "dmft-webapp", "pho-rsc",
				"hcap-fe", "umc-e2e-tests", "bcer-cp", "maid", "emcod", "sa-sfdc", "mspdirect-service", "hcimweb_hd2");
        AuthorizedClientsParser acp = new AuthorizedClientsParser();
        List<String> authorizedClients = acp.parseToEdit(jwt);

        assertEquals(knownAuthorizedClients.size(), authorizedClients.size());
        for (String authorizedClient : authorizedClients) {
            assertTrue(knownAuthorizedClients.contains(authorizedClient));
        }
    }
    
    @Test
    public void testParseEdit_PLR_readonlyAndViewClientToken() {
        /* JWT includes [plr (read-only-client-plr should not be included in Authorized clients), hcimweb, licence-status, sa-hibc-service-bc-portal, sa-dbaac-portal, pidp-service, prp-service, dmft-webapp, pho-rsc, hcap-fe, umc-e2e-tests, bcer-cp, maid, emcod, sa-sfdc, mspdirect-service, hcimweb_hd2] */
        String jwt = "eyJhbGciOiJSUzI1NiIsInR5cCIgOiAiSldUIiwia2lkIiA6ICJwdG1aOTAxOXFaZU5xdS1kWmQ5YlBBdnlRVm1KMGgtblNDS0djN1J6SXAwIn0.eyJleHAiOjE3NDQ2OTIzODcsImlhdCI6MTc0NDY5MjA4NywiYXV0aF90aW1lIjoxNzQ0Njg5NjI4LCJqdGkiOiI4MjdhYTA2Yy03ZDI0LTQ1ZmEtOWE5Ni1hMDI4MDc5MjM0YzgiLCJpc3MiOiJodHRwczovL2NvbW1vbi1sb2dvbi1kZXYuaGx0aC5nb3YuYmMuY2EvYXV0aC9yZWFsbXMvbW9oX2FwcGxpY2F0aW9ucyIsImF1ZCI6IlVTRVItTUFOQUdFTUVOVC1TRVJWSUNFIiwic3ViIjoiY2YzOTg3MDUtZWY3OC00MWE4LTlhZGQtNjU5MDdhNTVhMzVlIiwidHlwIjoiQmVhcmVyIiwiYXpwIjoiVVNFUi1NQU5BR0VNRU5UIiwibm9uY2UiOiIyOTc2MGFmZC03MTE1LTQxYzktODRlMS1jMGY1N2Q1ZWY4NTciLCJzZXNzaW9uX3N0YXRlIjoiOWQzOGE3YjItMWE0Ni00ZjU0LWI0ZjctYmI2MGU3OWI0MjE5IiwiYWxsb3dlZC1vcmlnaW5zIjpbIioiXSwicmVzb3VyY2VfYWNjZXNzIjp7IlVTRVItTUFOQUdFTUVOVCI6eyJyb2xlcyI6WyJ1c2VyLW1hbmFnZW1lbnQtYWRtaW4iXX0sIlVTRVItTUFOQUdFTUVOVC1TRVJWSUNFIjp7InJvbGVzIjpbInJlYWQtb25seS1wbHIiLCJ2aWV3LWNsaWVudC1oY2ltd2ViIiwidmlldy1jbGllbnQtbGljZW5jZS1zdGF0dXMiLCJ2aWV3LWNsaWVudC1zYS1oaWJjLXNlcnZpY2UtYmMtcG9ydGFsIiwidmlldy1jbGllbnQtc2EtZGJhYWMtcG9ydGFsIiwidmlldy1jbGllbnQtcGlkcC1zZXJ2aWNlIiwidmlldy1ncm91cHMiLCJ2aWV3LXVzZXJzIiwibWFuYWdlLXVzZXItZGV0YWlscyIsInZpZXctY2xpZW50LXBycC1zZXJ2aWNlIiwidmlldy1tZXRyaWNzIiwidmlldy1jbGllbnQtZG1mdC13ZWJhcHAiLCJ2aWV3LWNsaWVudC1waG8tcnNjIiwibWFuYWdlLW9yZyIsInZpZXctY2xpZW50LWhjYXAtZmUiLCJjcmVhdGUtdXNlciIsInZpZXctY2xpZW50LXVtYy1lMmUtdGVzdHMiLCJ2aWV3LWNsaWVudC1wbHIiLCJ2aWV3LWNsaWVudC1iY2VyLWNwIiwidmlldy1ldmVudHMiLCJ2aWV3LWNsaWVudC1tYWlkIiwidmlldy1jbGllbnQtZW1jb2QiLCJidWxrLXJlbW92YWwiLCJtYW5hZ2UtdXNlci1yb2xlcyIsInZpZXctY2xpZW50LXNhLXNmZGMiLCJ2aWV3LWNsaWVudHMiLCJtYW5hZ2UtYWxsLWdyb3VwcyIsInZpZXctY2xpZW50LW1zcGRpcmVjdC1zZXJ2aWNlIiwidmlldy1jbGllbnQtaGNpbXdlYl9oZDIiXX19LCJzY29wZSI6Im9wZW5pZCBtb2hfaWRwIHByb2ZpbGUgZW1haWwgaWRpcl9hYWQiLCJzaWQiOiI5ZDM4YTdiMi0xYTQ2LTRmNTQtYjRmNy1iYjYwZTc5YjQyMTkiLCJlbWFpbF92ZXJpZmllZCI6ZmFsc2UsIm5hbWUiOiJNZWhkaSBTYWRpayIsImdyb3VwcyI6WyJDR0kgQWNjZXNzIE1hbmFnZW1lbnQgVGVhbSJdLCJwcmVmZXJyZWRfdXNlcm5hbWUiOiJtZXNhZGlrQGlkaXIiLCJnaXZlbl9uYW1lIjoiTWVoZGkiLCJmYW1pbHlfbmFtZSI6IlNhZGlrIiwiZW1haWwiOiJtZWhkaS5zYWRpa0BjZ2kuY29tIn0.XJraPf3cTbkCgVwHyl8A4iCkNO3MNVNlrCA0bAHoDi57PpxvurfZAJXwSWu_kR7tS6WHIKm3V-YmrokDm7TBqw1oJ5erJQKCaXUWXf3l16MW5W4__FfqGAMe4Pnbo7riSuZXu5qHcERPyM1mY1C51zHIbPI3inBNE_oKJn3d1-5Z_kH2ADSKlLZUHCNQXQ-KeKq-D2NY-bUaJ5gVleIkwt0o42mMqGHndPPlotiJsEqaskDPFHyWB2lEIXAfXsNgiclFN_nysAmlmP9m9_FgQ0VyYJb7ToxtnHwiZHgIfC9F5pT-2xfU5vCBXttHUwZ5J4Ca_6XXO5BdeIwW7evz-Q";
		List<String> knownAuthorizedClients = Arrays.asList("hcimweb", "licence-status",
				"sa-hibc-service-bc-portal", "sa-dbaac-portal", "pidp-service", "prp-service", "dmft-webapp", "pho-rsc",
				"hcap-fe", "umc-e2e-tests", "bcer-cp", "maid", "emcod", "sa-sfdc", "mspdirect-service", "hcimweb_hd2","plr");
        AuthorizedClientsParser acp = new AuthorizedClientsParser();
        List<String> authorizedClients = acp.parseToEdit(jwt);

        assertEquals(knownAuthorizedClients.size(), authorizedClients.size());
        for (String authorizedClient : authorizedClients) {
            assertTrue(knownAuthorizedClients.contains(authorizedClient));
        }
    }
    

}
