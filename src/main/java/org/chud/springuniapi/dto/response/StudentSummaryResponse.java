package org.chud.springuniapi.dto.response;

//dto for course response that doesnt create circular nesting between the entities
public record StudentSummaryResponse(Long id, String name) { }
