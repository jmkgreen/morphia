/**
 * 
 */
package com.github.jmkgreen.morphia;

import java.util.Iterator;
import java.util.List;

import com.github.jmkgreen.morphia.annotations.Entity;
import org.junit.Assert;

import org.junit.Test;

import com.github.jmkgreen.morphia.testutil.TestEntity;

/**
 * @author Uwe Schaefer, (us@thomas-daily.de)
 * 
 */
public class TestGetByKeys extends TestBase {
	@Test
	public final void testGetByKeys() {
		A a1 = new A();
		A a2 = new A();
		
		Iterable<Key<A>> keys = ds.save(a1, a2);
		
		List<A> reloaded = ds.getByKeys(keys);
		
		Iterator<A> i = reloaded.iterator();
		Assert.assertNotNull(i.next());
		Assert.assertNotNull(i.next());
		Assert.assertFalse(i.hasNext());
	}

    @Test
    public final void testNoClassnameStoredGetByKeys() {
        A a = new A();
		B b = new B();

		Iterable<Key<TestEntity>> keys = ds.save(a, b);

        //1.2.2 throws NullPointerException here
        List<TestEntity> reloaded = ds.getByKeys(keys);

		Iterator<TestEntity> i = reloaded.iterator();
		Assert.assertNotNull(i.next());
		Assert.assertNotNull(i.next());
		Assert.assertFalse(i.hasNext());
    }
	
	public static class A extends TestEntity {
		private static final long serialVersionUID = 1L;
		String foo = "bar";
	}

    @Entity(noClassnameStored = true)
    public static class B extends TestEntity {
        String foo = "bar";
    }

}
