package com.github.jmkgreen.morphia;

import java.io.Serializable;

import org.junit.Assert;

import org.bson.types.ObjectId;
import org.junit.Test;

import com.github.jmkgreen.morphia.annotations.Entity;
import com.github.jmkgreen.morphia.annotations.Id;
import com.github.jmkgreen.morphia.annotations.PostLoad;
import com.github.jmkgreen.morphia.annotations.Property;
import com.github.jmkgreen.morphia.annotations.Reference;
import com.github.jmkgreen.morphia.mapping.lazy.LazyFeatureDependencies;
import org.junit.Ignore;

/**
 * Tests mapper functions; this is tied to some of the internals.
 *
 * @author scotthernandez
 *
 */
public class TestMapper extends TestBase {
	public static class A {
		static int loadCount = 0;
		@Id ObjectId id;

		String getId() {
			return id.toString();
		}

		@PostLoad
		protected void postConstruct() {
			if (A.loadCount > 1) {
				throw new RuntimeException();
			}

			A.loadCount++;
		}
	}

	@Entity("holders")
	public static class HoldsMultipleA {
		@Id ObjectId id;
		@Reference
		A a1;
		@Reference
		A a2;
	}

	@Entity("holders")
	public static class HoldsMultipleALazily {
		@Id ObjectId id;
		@Reference(lazy = true)
		A a1;
		@Reference
		A a2;
		@Reference(lazy = true)
		A a3;
	}

	public static class CustomId implements Serializable {

		private static final long serialVersionUID = 1L;

		@Property("v")
		ObjectId id;
		@Property("t")
		String type;

		/* (non-Javadoc)
		 * @see java.lang.Object#hashCode()
		 */
		@Override
		public int hashCode() {
			final int prime = 31;
			int result = 1;
			result = prime * result + ((id == null) ? 0 : id.hashCode());
			result = prime * result + ((type == null) ? 0 : type.hashCode());
			return result;
		}

		/*
		 * (non-Javadoc)
		 *
		 * @see java.lang.Object#equals(java.lang.Object)
		 */
		@Override
		public boolean equals(Object obj) {
			if (this == obj) {
				return true;
			}
			if (obj == null) {
				return false;
			}
			if (!(obj instanceof CustomId)) {
				return false;
			}
			CustomId other = (CustomId) obj;
			if (id == null) {
				if (other.id != null) {
					return false;
				}
			} else if (!id.equals(other.id)) {
				return false;
			}
			if (type == null) {
				if (other.type != null) {
					return false;
				}
			} else if (!type.equals(other.type)) {
				return false;
			}
			return true;
		}

		/*
		 * (non-Javadoc)
		 *
		 * @see java.lang.Object#toString()
		 */
		@Override
		public String toString() {
			StringBuilder builder = new StringBuilder();
			builder.append("CustomId [");
			if (id != null) {
				builder.append("id=").append(id).append(", ");
			}
			if (type != null) {
				builder.append("type=").append(type);
			}
			builder.append("]");
			return builder.toString();
		}
	}

	public static class UsesCustomIdObject {
		@Id CustomId id;
		String text;
	}

	public static class ReferencesCustomId {
		@Id String id;
		@Reference UsesCustomIdObject child;
	}

    /**
     * Should only load one instance of A.
     * Ensure {@link A#id A's id} is zero at start of each test
     * using it!
     * @throws Exception
     */
	@Test
	public void SingleLookup() throws Exception {
		A.loadCount = 0;
		A a = new A();
		HoldsMultipleA holder = new HoldsMultipleA();
		holder.a1 = a;
		holder.a2 = a;
		ds.save(a, holder);
		holder = ds.get(HoldsMultipleA.class, holder.id);
		Assert.assertEquals(1, A.loadCount);
		Assert.assertTrue(holder.a1 == holder.a2);
	}

	@Test
	public void SingleProxy() throws Exception {
        // TODO us: exclusion does not work properly with maven + junit4
        if (!LazyFeatureDependencies.testDependencyFullFilled())
        {
            return;
        }

		A.loadCount = 0;
		A a = new A();
		HoldsMultipleALazily holder = new HoldsMultipleALazily();
		holder.a1 = a;
		holder.a2 = a;
		holder.a3 = a;
		ds.save(a, holder);
		Assert.assertEquals(0, A.loadCount);
		holder = ds.get(HoldsMultipleALazily.class, holder.id);
		Assert.assertNotNull(holder.a2);
		Assert.assertEquals(1, A.loadCount);
		Assert.assertFalse(holder.a1 == holder.a2);
		// FIXME currently not guaranteed:
		// Assert.assertTrue(holder.a1 == holder.a3);

		// A.loadCount=0;
		// Assert.assertEquals(holder.a1.getId(), holder.a2.getId());

	}

	@Test
	public void SerializableId() throws Exception {
		CustomId cId = new CustomId();
		cId.id = new ObjectId();
		cId.type = "banker";

		UsesCustomIdObject ucio = new UsesCustomIdObject();
		ucio.id = cId;
		ucio.text = "hllo";
		ds.save(ucio);
	}

    /**
     * Fix me. Referenced id field may need marshaling.
     * @throws Exception
     */
	@Test
	public void ReferenceCustomId() throws Exception {
		CustomId cId = new CustomId();
		cId.id = new ObjectId();
		cId.type = "banker";

		UsesCustomIdObject objWithCustomId = new UsesCustomIdObject();
		objWithCustomId.id = cId;
		objWithCustomId.text = "hello world";

		ReferencesCustomId obj = new ReferencesCustomId();
		obj.id = "testId";
		obj.child = objWithCustomId;

		ds.save(objWithCustomId);
		ds.save(obj);

		ReferencesCustomId reObj = ds.get(ReferencesCustomId.class, obj.id);

		Assert.assertEquals(reObj.child.text, objWithCustomId.text);
		Assert.assertEquals(reObj.child.id, objWithCustomId.id);
	}

}
