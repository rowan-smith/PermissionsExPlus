package ru.tehkode.permissions;

import org.junit.jupiter.api.BeforeEach;

public class RegExpMatcherTest extends PermissionMatcherTest {

    @BeforeEach
    public void setup() {
        this.matcher = new RegExpMatcher();
    }


}
