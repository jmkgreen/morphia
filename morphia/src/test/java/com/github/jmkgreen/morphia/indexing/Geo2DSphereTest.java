package com.github.jmkgreen.morphia.indexing;

import java.util.List;

import org.bson.types.ObjectId;
import org.junit.Assert;
import org.junit.Test;

import com.github.jmkgreen.morphia.TestBase;
import com.github.jmkgreen.morphia.annotations.Id;
import com.github.jmkgreen.morphia.annotations.Indexed;
import com.github.jmkgreen.morphia.utils.IndexDirection;
import com.mongodb.DBObject;

public class Geo2DSphereTest extends TestBase {
    public static class HasGeo2DSphereIndex {
        @Id
        ObjectId id = new ObjectId();
        @Indexed(IndexDirection.GEO2DSPHERE)
        Object indexed;
    }

    @Test
    public void testIndex() {
        morphia.map(HasGeo2DSphereIndex.class);
        ds.ensureIndexes();
        HasGeo2DSphereIndex e = new HasGeo2DSphereIndex();
        List<DBObject> indexes = ds.getCollection(HasGeo2DSphereIndex.class)
                .getIndexInfo();

        Assert.assertNotNull(indexes);
        Assert.assertEquals(2, indexes.size());
        for (DBObject i : indexes) {
            Assert.assertTrue(i.containsField("key"));
            DBObject key = (DBObject) i.get("key");
            if (key.containsField("indexed")) {
                Object get = key.get("indexed");
                Assert.assertTrue(get instanceof String);
                Assert.assertEquals("2dsphere", get);
                return;
            }
        }
        Assert.fail();
    }
}
