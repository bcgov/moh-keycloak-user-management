package ca.bc.gov.hlth.mohums.userSearch.event;

public interface LastLogDate {
     String getUserId();
     //the type is object and not long, because query can return either long or "Over a year ago" string
     Object getLastLogin();

}
