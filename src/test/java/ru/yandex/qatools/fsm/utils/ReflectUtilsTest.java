package ru.yandex.qatools.fsm.utils;

import org.junit.Test;

import java.io.Serializable;
import java.util.List;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static ru.yandex.qatools.fsm.utils.ReflectUtils.collectAllSuperclassesAndInterfaces;
import static ru.yandex.qatools.fsm.utils.ReflectUtils.containsClass;

/**
 * @author: Ilya Sadykov (mailto: smecsia@yandex-team.ru)
 */
public class ReflectUtilsTest {


    interface CommonInterface {

    }

    interface TestBaseInterface extends CommonInterface {

    }

    class TestBaseClass implements TestBaseInterface, Serializable {

    }

    interface TestInterface {

    }

    class TestClass extends TestBaseClass implements TestInterface {

    }

    @Test
    public void testCollectAllSuperclassesAndInterfaces() {
        List<Class> res = collectAllSuperclassesAndInterfaces(TestClass.class);
        Class[] expected = new Class[]{
                TestClass.class, TestInterface.class, TestBaseClass.class, TestBaseInterface.class,
                CommonInterface.class, Serializable.class, Object.class
        };
        for (int i = 0; i < res.size(); ++i) {
            assertEquals("Epected to have " + expected[i] + " in the chain, but have " + res.get(i), expected[i], res.get(i));
        }
    }

    @Test
    public void testContainsClass() {
        Class[] classes = new Class[]{TestInterface.class, TestBaseClass.class, CommonInterface.class};
        assertTrue(containsClass(classes, TestBaseClass.class));
        assertFalse(containsClass(classes, TestClass.class));
    }
}
