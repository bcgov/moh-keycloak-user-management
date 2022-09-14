package ca.bc.gov.hlth.mohums.model;

import com.fasterxml.jackson.annotation.JsonIgnore;
import lombok.Builder;
import lombok.Value;

import java.util.List;
import java.util.Map;

@Builder
@Value
public class Group {

    String id;
    String name;
    String path;
    String description;
    @JsonIgnore
    List<String> umsRoles;

}
