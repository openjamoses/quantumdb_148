package io.quantumdb.core.utils;

import static com.google.common.base.Strings.isNullOrEmpty;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotEquals;

import com.google.common.collect.Lists;
import io.quantumdb.core.versioning.RefLog;
import io.quantumdb.core.versioning.Version;
import org.junit.Test;

public class RandomHasherTest {

	@Test
	public void testGeneratingRandomHashReturnsNonEmptyString() {
		String hash = RandomHasher.generateHash();
		assertFalse(isNullOrEmpty(hash));
	}

	@Test
	public void testGeneratingRandomHashReturnsDifferentStringsUponMultipleCalls() {
		String hash1 = RandomHasher.generateHash();
		String hash2 = RandomHasher.generateHash();
		assertNotEquals(hash1, hash2);
	}

	@Test
	public void testGeneratingUniqueTableIdWithEmptyTableMapping() {
		RefLog refLog = new RefLog();
		String tableId = RandomHasher.generateTableId(refLog);
		assertFalse(isNullOrEmpty(tableId));
	}

	@Test
	public void testGeneratingUniqueTableIdWithFilledTableMapping() {
		RefLog refLog = new RefLog();
		Version version = new Version(RandomHasher.generateHash(), null);
		refLog.addTable("users", "users", version, Lists.newArrayList());

		String tableId = RandomHasher.generateTableId(refLog);
		assertFalse(isNullOrEmpty(tableId));
		assertNotEquals("users", tableId);
	}

}
