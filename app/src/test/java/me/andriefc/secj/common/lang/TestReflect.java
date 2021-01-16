package me.andriefc.secj.common.lang;


import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;

import static org.junit.jupiter.api.Assertions.assertEquals;


@TestInstance(TestInstance.Lifecycle.PER_CLASS)
public class TestReflect {

    @Test
    public void testFindObject() {
        Object dummyObject = Reflect.tryAsKotlinSingleton(DummyObject.class);
        assertEquals(DummyObject.INSTANCE, dummyObject);
    }
}
