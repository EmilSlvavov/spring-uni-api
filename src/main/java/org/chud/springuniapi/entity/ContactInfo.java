package org.chud.springuniapi.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.chud.springuniapi.enums.ContactType;

import java.util.Objects;

//Embeddable class for the @ElementCollection table
@Embeddable
@Getter
@Setter
@NoArgsConstructor
public class ContactInfo {

    @Enumerated(EnumType.STRING)
    @Column(name = "contact_type", nullable = false, length = 20)
    private ContactType type;

    @Column(name = "contact_value", nullable = false, length = 200)
    private String value;

    public ContactInfo(ContactType type, String value) {
        this.type = type;
        this.value = value;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null) return false;

        if (!(o instanceof ContactInfo that)) return false;

        return type == that.type && Objects.equals(value, that.value);
    }

    @Override
    public int hashCode() {
        return Objects.hash(type, value);
    }
}
