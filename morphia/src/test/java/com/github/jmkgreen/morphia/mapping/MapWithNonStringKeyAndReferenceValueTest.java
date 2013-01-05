package com.github.jmkgreen.morphia.mapping;

import com.github.jmkgreen.morphia.annotations.Id;
import com.github.jmkgreen.morphia.annotations.Reference;
import com.github.jmkgreen.morphia.mapping.lazy.LazyFeatureDependencies;
import com.github.jmkgreen.morphia.mapping.lazy.ProxyTestBase;
import com.github.jmkgreen.morphia.testutil.TestEntity;
import org.bson.types.ObjectId;
import org.junit.Test;

import java.util.HashMap;
import java.util.Map;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

/**
 * User: mutyonok
 * Date: 05.01.13
 * Time: 12:05
 */
public class MapWithNonStringKeyAndReferenceValueTest extends ProxyTestBase {
    @Test
    public void testMapKeyShouldBeInteger() throws Exception {
        morphia.map(ChildEntity.class, ParentEntity.class);

        ChildEntity ce1 = new ChildEntity();
        ce1.value = "first";
        ChildEntity ce2 = new ChildEntity();
        ce2.value = "second";

        ParentEntity pe = new ParentEntity();
        pe.childMap.put(1, ce1);
        pe.childMap.put(2, ce2);

        ds.save(ce1, ce2, pe);

        ParentEntity fetched = ds.get(ParentEntity.class, pe.getId());
        assertNotNull(fetched);
        assertNotNull(fetched.childMap);
        assertEquals(2, fetched.childMap.size());
        //it is really String without fixing the reference mapper
        //so ignore IDE's complains if any
        assertTrue(fetched.childMap.keySet().iterator().next() instanceof Integer);
    }

    @Test
    public void testWithProxy() throws Exception {
        if (!LazyFeatureDependencies.assertDependencyFullFilled())
        {
            return;
        }
        morphia.map(ChildEntity.class, ParentEntity.class);

        ChildEntity ce1 = new ChildEntity();
        ce1.value = "first";
        ChildEntity ce2 = new ChildEntity();
        ce2.value = "second";

        ParentEntity pe = new ParentEntity();
        pe.lazyChildMap.put(1, ce1);
        pe.lazyChildMap.put(2, ce2);

        ds.save(ce1, ce2, pe);

        ParentEntity fetched = ds.get(ParentEntity.class, pe.getId());
        assertNotNull(fetched);
        assertIsProxy(fetched.lazyChildMap);
        assertNotFetched(fetched.lazyChildMap);
        assertEquals(2, fetched.lazyChildMap.size());
        assertFetched(fetched.lazyChildMap);
        //it is really String without fixing the reference mapper
        //so ignore IDE's complains if any
        assertTrue(fetched.lazyChildMap.keySet().iterator().next() instanceof Integer);
    }

    private static class ParentEntity extends TestEntity{
        @Reference
        Map<Integer, ChildEntity> childMap = new HashMap<Integer, ChildEntity>();
        @Reference(lazy = true)
        Map<Integer, ChildEntity> lazyChildMap = new HashMap<Integer, ChildEntity>();
    }
    private static class ChildEntity extends TestEntity{
        String value;

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (o == null || getClass() != o.getClass()) return false;

            ChildEntity that = (ChildEntity) o;

            if (id != null ? !id.equals(that.id) : that.id != null) return false;
            if (value != null ? !value.equals(that.value) : that.value != null) return false;

            return true;
        }

        @Override
        public int hashCode() {
            int result = id != null ? id.hashCode() : 0;
            result = 31 * result + (value != null ? value.hashCode() : 0);
            return result;
        }
    }
}
