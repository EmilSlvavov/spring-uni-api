package org.chud.springuniapi.dto.response;

//used for projection, no from() because its source
// isnt an entity and mapping in service
public record StudentDisplayResponse(String name, String displayLabel) { }
