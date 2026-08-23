package org.chud.springuniapi.repository.projection;

import org.springframework.beans.factory.annotation.Value;

public interface UserDisplayView {
    String getName();

    //open projection
    @Value("#{target.name + ' <' + target.email + '>'}")
    String getDisplayLabel();
}
