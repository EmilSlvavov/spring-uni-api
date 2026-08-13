package org.chud.springuniapi.dto.request;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;

import java.util.List;

//used for the put request to replace contacts
//ElementCollection has values, but they are tied to the parent and have no id
//therefore you cannot address them indivudually
public record ReplaceContactsRequest(
        //Valid here so it cascades into the elements of the list
        @NotNull @Valid List<ContactInfoRequest> contacts
) { }
